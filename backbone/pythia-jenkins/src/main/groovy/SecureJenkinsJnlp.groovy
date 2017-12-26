/*
   Copyright (c) 2015-2017 Sam Gleske - https://github.com/samrocketman/jenkins-bootstrap-shared
   Copyright (c) 2017 Nic Seltzer

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
   */
/*
   Disable all JNLP protocols except for JNLP4.  JNLP4 is the most secure agent
   protocol because it is using standard TLS.
 */

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
        configText = new File("${configPath}/main_config.yml").text
    } catch (FileNotFoundException e) {
        logger.severe("Cannot find config file path @ ${configPath}/main_config.yml")
        jenkins.doSafeExit(null)
        System.exit(1)
    }

    Boolean jnlpSecured = (Boolean) yaml.load(configText).JNLP.JNLP_SECURED

    if (!jnlpSecured) {
        logger.info("Additional JNLP security hasn't been enacted. All protocols still enabled.")
        System.exit(0)
    } else {
        Set<String> agentProtocolsList = ['JNLP4-connect', 'Ping']
        if(!jenkins.getAgentProtocols().equals(agentProtocolsList)) {
            jenkins.setAgentProtocols(agentProtocolsList)
            jenkins.save()
            logger.info("JNLP has been secured. All insecure agents stopped.")
        }
    }
}