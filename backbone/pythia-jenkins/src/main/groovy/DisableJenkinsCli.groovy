#!/usr/bin/env groovy

/*
The MIT License

Copyright (c) 2015, Kohsuke Kawaguchi
Copyright (c) 2017, Nicholas Seltzer

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
*/

/**
* shutdown jenkins CLI
*
* as advised in https://github.com/jenkinsci-cert/SECURITY-218, this script
* will shutdown the Jenkins CLI components, which are a security concern for
* the version of Jenkins we are currently running.
*
**/

@Grab('org.yaml:snakeyaml:1.17')

import java.util.logging.Logger
import jenkins.*;
import jenkins.model.*;
import hudson.model.*;
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

    Boolean cliEnabled = (Boolean) yaml.load(configText).Cli.cliEnabled

    if (cliEnabled) {
        logger.info("Jenkins CLI is enabled in ${configPath}/jenkins.yml")
        System.exit(0)
    } else {

        jenkins.getDescriptor("jenkins.CLI").get().setEnabled(false)

        def agentProtocol = AgentProtocol.all()
        agentProtocol.each { agent ->
            if (agent.name && agent.name.contains("CLI")) {
                logger.info("Disabling ${agent.name}...")
                agentProtocol.remove(agent)
            }
        }

        // 'lst' is exposed by Jenkins (Hudson)
        def removal = { lst ->
            lst.each { plugin ->
                if (plugin.getClass().name.contains("CLIAction")) {
                    logger.info("Disabling ${plugin.getClass().name}...")
                    lst.remove(plugin)
                }
            }
        }

        logger.info("Jenkins CLI subsystems, modules, and agents removed.")
        removal(jenkins.getExtensionList(RootAction.class))
        removal(jenkins.actions)
    }
}
