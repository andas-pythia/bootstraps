#!/usr/bin/env groovy

/**
* Security - User Plugin / GitHub OAUTH
*
* In order to configure Github OAuth, you must first create a id-secret
* keypair. Follow the steps described here:
* https://wiki.jenkins.io/display/JENKINS/GitHub+OAuth+Plugin#GitHubOAuthPlugin-Setup
* to create the keypair. Input these values into test_data/security.yml
**/

@Grab('org.yaml:snakeyaml:1.17')

import java.util.logging.Logger
import jenkins.model.Jenkins
import hudson.security.SecurityRealm
import hudson.security.ProjectMatrixAuthorizationStrategy
import hudson.security.Permission
import org.jenkinsci.plugins.GithubSecurityRealm
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
        configText = new File("${configPath}/security.yml").text
    } catch (FileNotFoundException e) {
        logger.severe("Cannot find config file path @ ${configPath}/security.yml")
        jenkins.doSafeExit(null)
        System.exit(1)
    }

    securityGroups = yaml.load(configText).securityGroups
    oauthSettings = yaml.load(configText).oauthSettings

    SecurityRealm github_realm = new GithubSecurityRealm(oauthSettings.githubWebUrl,
                                                        oauthSettings.githubApiUrl,
                                                        oauthSettings.clientId,
                                                        oauthSettings.clientSecret,
                                                        oauthSettings.scopes
                                                        )

    if (!github_realm.equals(jenkins.getSecurityRealm())) {
        jenkins.setSecurityRealm(github_realm)
        jenkins.save()
    }

    List validPermissions = [   
        'hudson.model.Computer.Build',
        'hudson.model.Computer.Configure',
        'hudson.model.Computer.Connect',
        'hudson.model.Computer.Create',
        'hudson.model.Computer.Delete',
        'hudson.model.Computer.Disconnect',
        'hudson.model.Hudson.Administer',
        'hudson.model.Hudson.ConfigureUpdateCenter',
        'hudson.model.Hudson.Read',
        'hudson.model.Hudson.RunScripts',
        'hudson.model.Hudson.UploadPlugins',
        'hudson.model.Item.Build',
        'hudson.model.Item.Cancel',
        'hudson.model.Item.Configure',
        'hudson.model.Item.Create',
        'hudson.model.Item.Delete',
        'hudson.model.Item.Discover',
        'hudson.model.Item.Read',
        'hudson.model.Item.Workspace',
        'hudson.model.Run.Delete',
        'hudson.model.Run.Update',
        'hudson.model.View.Configure',
        'hudson.model.View.Create',
        'hudson.model.View.Delete',
        'hudson.model.View.Read'
        ]

    def strategy = new ProjectMatrixAuthorizationStrategy()

    securityGroups.each { group ->
        logger.info("Adding security group: ${group.name}")
        group.users.each { user ->
            group.permissions.each { permissionString ->
                if (!validPermissions.any { it == permissionString }) {
                    logger.severe("Permission (${permissaionString}) is not supported in Jenkins")
                    jenkins.doSafeExit(null)
                    System.exit(1)
                } else {
                    Permission permission = Permission.fromId(permissionString)
                    strategy.add(permission, user)
                }
            }
        }
    }

    jenkins.setAuthorizationStrategy(strategy)
    jenkins.save()
}
