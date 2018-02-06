#!/usr/bin/env groovy

@Grab('org.yaml:snakeyaml:1.17')

import java.util.logging.Logger
import hudson.security.csrf.DefaultCrumbIssuer
import jenkins.model.Jenkins
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

    Boolean csrfEnabled = (Boolean) yaml.load(configText).csrf.csrfEnabled

    if (!csrfEnabled) {
        logger.info("Jenkins CSRF is disabled in ${configPath}/jenkins.yml")
        System.exit(0)
    } else {
        jenkins.setCrumbIssuer(new DefaultCrumbIssuer(true))
        jenkins.save()
        logger.info("CSRF has been configured for Jenkins.")
    }
}
