#!/usr/bin/env groovy

/**
* main configuration
*
* This script will automate the configuration of the main jenkins options,
* found at <jenkinsURL>/configure. It does not configure any of the options
* offered via plugins.
*
* Values for this configuration should NEVER be placed in this script, but
* available to Jenkins as an environment variable pointing to YAML data
*
**/

@Grab('org.yaml:snakeyaml:1.17')

import java.util.logging.Logger
import jenkins.*
import jenkins.model.*
import hudson.model.*
import hudson.tasks.Shell
import hudson.slaves.EnvironmentVariablesNodeProperty
import hudson.slaves.EnvironmentVariablesNodeProperty.Entry
import hudson.model.Node.Mode
import hudson.markup.RawHtmlMarkupFormatter
import hudson.markup.EscapedMarkupFormatter
import org.yaml.snakeyaml.Yaml

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
        configText = new File("${configPath}/jenkins.yml").text
    } catch (FileNotFoundException e) {
        logger.severe("Cannot find config file path @ ${configPath}/jenkins.yml")
        jenkins.doSafeExit(null)
        System.exit(1)
    }

    Map mainConfig = yaml.load(configText).main
    Map propertiesConfig = yaml.load(configText).globalProperties
    Map locationConfig = yaml.load(configText).location
    Map shellConfig = yaml.load(configText).shell
    Map formatterConfig = yaml.load(configText).formatter

    logger.info("Configuring basic Jenkins options")
    try {
        jenkins.setSystemMessage(mainConfig.systemMessage)
        jenkins.setNumExecutors(mainConfig.numberOfExecutors)
        jenkins.setLabelString(mainConfig.labels.join(' '))
        jenkins.setQuietPeriod(mainConfig.quietPeriod)
        jenkins.setScmCheckoutRetryCount(mainConfig.scmCheckoutRetryCount)
        jenkins.setDisableRememberMe(mainConfig.disableRememberMe)
        if (mainConfig.usage.toUpperCase() == 'NORMAL') {
            jenkins.setMode(Mode.NORMAL)
        } else if (mainConfig.usage.toUpperCase() == 'EXCLUSIVE') {
            jenkins.setMode(Mode.EXCLUSIVE)
        }
        else {
            logger.severe('Invalid value specified for usage. Exiting.')
            jenkins.doSafeExit(null)
            System.exit(1)
        }
    } catch (MissingMethodException e) {
        logger.severe("Invalid value specified for Main configuration in ${configPath}/jenkins.yml")
        jenkins.doSafeExit(null)
        System.exit(1)
    }

    logger.info("Adding global environment variables to Jenkins")
    List<Entry> envVarList = new ArrayList<Entry>()
    propertiesConfig.environmentVariables.each { envVar ->
                    try {
                        envVarList.add(new Entry(envVar.NAME, envVar.VALUE))
                    } catch (MissingMethodException e) {
                        logger.severe("Invalid value specified for environment variables in ${configPath}/jenkins.yml")
                        jenkins.doSafeExit(null)
                        System.exit(1)
                    }
    }

    jenkins.getGlobalNodeProperties().replaceBy(
        Collections.singleton(
            new EnvironmentVariablesNodeProperty(envVarList)
        )
    )

    logger.info("Setting up the Jenkins URL")
    JenkinsLocationConfiguration location = jenkins.getExtensionList(jenkins.model.JenkinsLocationConfiguration).get(0)
    try {
        location.setUrl(locationConfig.url)
        location.setAdminAddress(locationConfig.adminEmail)
    } catch (MissingMethodException e) {
        logger.severe("Invalid value in the LOCATION configuration of ${configPath}/jenkins.yml")
        jenkins.doSafeExit(null)
        System.exit(1)
    }

    logger.info("Setting the default shell used by Jenkins")
    Process p = "stat ${shellConfig.executable}".execute()
    p.waitForOrKill(1000)
    if (p.exitcode != 0) {
        logger.severe("Executable ${shellConfig.executable} not present on system")
        jenkins.doSafeExit(null)
        System.exit(1)
    }
    Shell.DescriptorImpl shell = jenkins.getExtensionList(Shell.DescriptorImpl.class).get(0)
    shell.setShell(shellConfig.executable)
    shell.save()

    // Configure the markup formatter for build and job descriptions
    if (formatterConfig.formatterType.toLowerCase() == 'rawhtml') {
        RawHtmlMarkupFormatter markupFormatter = new RawHtmlMarkupFormatter(
            formatterConfig.disableSyntaxHighlighting
        )
        jenkins.setMarkupFormatter(markupFormatter)
    } else if (formatterConfig.formatterType.toLowerCase() == 'plain') {
        EscapedMarkupFormatter markupFormatter = new EscapedMarkupFormatter()
        jenkins.setMarkupFormatter(markupFormatter)
    } else {
        logger.severe("Invalid value in the Formatter configuration of ${configPath}/jenkins.yml")
        jenkins.doSafeExit(null)
        System.exit(1)
    }

    jenkins.save()
    logger.info("Finished configuring the main Jenkins options.")
}
