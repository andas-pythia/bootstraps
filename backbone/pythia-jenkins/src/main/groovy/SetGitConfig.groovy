@Grab('org.yaml:snakeyaml:1.17')

import java.util.logging.Logger
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
        configText = new File("${configPath}/git_config.yml").text
    } catch (FileNotFoundException e) {
        logger.severe("Cannot find config file path @ ${configPath}/git_config.yml")
        jenkins.doSafeExit(null)
        System.exit(1)
    }

    Map gitConfig = yaml.load(configText)

    def gitScm = jenkins.getDescriptorByType(hudson.plugins.git.GitSCM.DescriptorImpl.class)

    gitScm.setGlobalConfigName(gitConfig.NAME)
    gitScm.setGlobalConfigEmail(gitConfig.EMAIL)

    jenkins.save()
}
