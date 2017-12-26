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
        configText = new File("${configPath}/main_config.yml").text
    } catch (FileNotFoundException e) {
        logger.severe("Cannot find config file path @ ${configPath}/main_config.yml")
        jenkins.doSafeExit(null)
        System.exit(1)
    }

    Boolean csrfEnabled = (Boolean) yaml.load(configText).CSRF.CSRF_ENABLED

    if (!csrfEnabled) {
        logger.info("Jenkins CSRF is disabled in ${configPath}/main_config.yml")
        System.exit(0)
    }

    logger.info("CSRF has been configured for Jenkins.")
    jenkins.setCrumbIssuer(new DefaultCrumbIssuer(true))
    jenkins.save()
}
