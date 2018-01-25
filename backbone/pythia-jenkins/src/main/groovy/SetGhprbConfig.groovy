/*

TODO: This entire thing is broken and needs to be reimagined. The configuration values
are solid, but there are typos and inefficient patterns in use. I'd like to see importing
YAML as an object, manipulating that object and then exporting that PO(G/J)O to JSON via Gson.

*/

@Grab('org.yaml:snakeyaml:1.17')
@Grab('org.mockito:mockito-all:1.10.19')

import java.util.logging.Logger
import javax.servlet.http.HttpServletRequestWrapper
import java.lang.reflect.Field
import jenkins.*
import jenkins.model.*
import hudson.model.*
import hudson.util.Secret
import net.sf.json.JSONObject
import org.kohsuke.github.GHCommitState
import org.kohsuke.stapler.*
import org.jenkinsci.plugins.ghprb.*
import org.jenkinsci.plugins.ghprb.extensions.*
import org.jenkinsci.plugins.ghprb.extensions.comments.*
import org.jenkinsci.plugins.ghprb.extensions.status.*
import org.yaml.snakeyaml.Yaml
import org.mockito.Mockito


Logger logger = Logger.getLogger("")
Jenkins jenkins = Jenkins.getInstance()
Yaml yaml = new Yaml()

if (jenkins.isQuietingDown()) {
    logger.info('No action taken. Shutdown mode enabled.')
    System.exit(0)
} else {
    String configPath = System.getenv("JENKINS_CONFIG_PATH")
    String configText

    try {
        configText = new File("${configPath}/ghprb.yml").text
    } catch (FileNotFoundException e) {
        logger.severe("Cannot find config file path @ ${configPath}/ghprb.yml")
        jenkins.doSafeExit(null)
        System.exit(1)
    }

    Map ghprbConfig = yaml.load(configText)
    def descriptor = Jenkins.instance.getDescriptorByType(
        org.jenkinsci.plugins.ghprb.GhprbTrigger.DescriptorImpl.class
        )
    
    String blackList = ghprbConfig.whitelistLabels
    String whiteList = ghprbConfig.blacklistLabels
    String blacklistCommitAuthors = ghprbConfig.blacklistCommitAuthors
    def commitState
    def resultCommitState

    try {
        commitState = (GHCommitState) ghprbConfig.unstableAs.toUpperCase()
    } catch (IllegalArgumentException e) {
        logger.severe('Unable to cast unstableAs into GHCommitState.')
        logger.severe('Make sure it is one the following values: PENDING, FAILURE, ERROR')
        jenkins.doSafeExit(null)
        System.exit(1)
    }

    if (blackList) {
        blackList = backList.join(', ')
    } else {
        blackList = ''
    }

    if (whiteList) {
        whiteList = whiteList.join(', ')
    } else {
        whiteList = ''
    }

    if (blacklistCommitAuthors) {
        blacklistCommitAuthors = blacklistCommitAuthors.join(', ')
    } else {
        blacklistCommitAuthors = ''
    }

    def jsonBuilder = new groovy.json.JsonBuilder()
    def jsonRoot = jsonBuilder {
        requestForTestingPhrase ghprbConfig.requestForTestingPhrase
        whitelistPhrase ghprbConfig.whitelistPhrase
        okToTestPhrase ghprbConfig.okToTestPhrase
        retestPhrase ghprbConfig.retestPhrase
        skipBuildPhrase ghprbConfig.skipBuildPhrase
        cron ghprbConfig.cronSchedule
        useComments ghprbConfig.useComments
        useDetailedComments ghprbConfig.useDetailedComments
        manageWebhooks ghprbConfig.manageWebhooks
        unstableAs ghprbConfig.unstableAs.toUpperCase()
        adminlist ghprbConfig.adminList.join(', ')
        autoCloseFailedPullRequests ghprbConfig.autoCloseFailedPrs
        displayBuildErrorsOnDownstreamBuilds ghprbConfig.displayErrorsDownstream
        githubAuth ghprbConfig.githubAuth
        blackListCommitAuthor blacklistCommitAuthors
        whiteListLabels whiteList
        blackListLabels blackList
    }

    JSONObject jsonObj = new JSONObject(jsonRoot)

    StaplerRequest stapler =  new RequestImpl(
        new Stapler(),
        Mockito.mock(HttpServletRequestWrapper.class),
        new ArrayList<AncestorImpl>(),
        new TokenList("")
    )
    descriptor.configure(stapler, jsonObj)

    Field githubAuth = descriptor.class.getDeclaredField("githubAuth")
    githubAuth.setAccessible(true)
    githubAuthArray = new ArrayList<GhprbGitHubAuth>()
    Secret sharedSecret = new Secret(ghprbConfig.sharedSecret)
    githubAuthArray.add(new GhprbGitHubAuth(
        ghprbConfig.serverApiUrl,
        null,
        ghprbConfig.credentialsId,
        null,
        null,
        sharedSecret)
    )
    githubAuth.set(descriptor, githubAuthArray)
    descriptor.save()

    // Configure plugin extensions after the main configuration has been set up
    List<GhprbExtension, GhprbExtensionDescriptor> extensions = descriptor.getExtensions()
    // Remove any previously configured extensions, as they will create duplicates
    extensions.remove(GhprbSimpleStatus.class)
    extensions.remove(GhprbPublishJenkinsUrl.class)
    extensions.remove(GhprbBuildLog.class)
    extensions.remove(GhprbBuildResultMessage.class)

    // Only add GHPRB extensions if they have non empty/zero values in
    // github_config.yml.
    if (!ghprbConfig.simpleStatus.isEmpty()) {
        extensions.push(new GhprbSimpleStatus(ghprbConfig.simpleStatus))
    }
    if (!ghprbConfig.publishJenkinsUrl.isEmpty()) {
        extensions.push(new GhprbPublishJenkinsUrl(ghprbConfig.publishJenkinsUrl))
    }
    if (ghprbConfig.buildLogLinesToDisplay > 0) {
        extensions.push(new GhprbBuildLog(ghprbConfig.buildLogLinesToDisplay))
    }
    if (ghprbConfig.resultMessages.isEmpty()) {
        ArrayList<GhprbBuildResultMessage> buildResultMessages = new ArrayList<GhprbBuildResultMessage>()
        ghprbConfig.resultMessages.each { resultMessage ->
            try {
                resultCommitState = resultMessage.status.toUpperCase() as GHCommitState
                buildResultMessages << new GhprbBuildResultMessage(resultCommitState, resultMessage.message)
            } catch (IllegalArgumentException e) {
                logger.severe('Unable to cast resultMessage.status into GHCommitState')
                logger.severe('Make sure it is one the following values: PENDING, FAILURE, ERROR')
                jenkins.doSafeExit(null)
                System.exit(1)
            }
        }
        extensions.push(new GhprbBuildStatus(buildResultMessages))
    }

    logger.info('Successfully configured the GHPRB plugin')
}
