#!/usr/bin/env groovy

@Grab('org.yaml:snakeyaml:1.17')

import java.util.logging.Logger
import javax.servlet.http.HttpServletRequestWrapper
import java.lang.reflect.Field
import jenkins.*
import jenkins.model.*
import hudson.model.*
import hudson.util.Secret
import org.yaml.snakeyaml.Yaml

import com.cloudbees.plugins.credentials.Credentials
import com.cloudbees.plugins.credentials.CredentialsScope
import com.cloudbees.plugins.credentials.domains.Domain
import com.cloudbees.plugins.credentials.impl.UsernamePasswordCredentialsImpl
import com.cloudbees.plugins.credentials.SystemCredentialsProvider

import com.nirima.jenkins.plugins.docker.*
import com.nirima.jenkins.plugins.docker.launcher.*
import com.nirima.jenkins.plugins.docker.strategy.*

class DockerCloudTemplate {
    String  image
    String  labelString
    String  remoteFs
    String  credentialsId
    Integer idleTerminationMinutes
    Integer sshLaunchTimeoutMinutes
    String  jvmOptions
    String  javaPath
    Integer memoryLimit
    Integer memorySwap
    Integer cpuShares
    String  prefixStartSlaveCommand
    String  suffixStartSlaveCommand
    Integer instanceCapStr
    String  dnsString
    String  dockerCommand
    String  volumesString
    String  volumesFromString
    String  hostname
    Array   bindPorts
    Boolean bindAllPorts
    Boolean privileged
    Boolean tty
    String  macAddress
}

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
        configText = new File("${configPath}/docker.yml").text
    } catch (FileNotFoundException e) {
        logger.severe("Cannot find config file path @ ${configPath}/docker.yml")
        jenkins.doSafeExit(null)
        System.exit(1)
    }

    // confval
    def dockerCertPath = System.getenv("DOCKER_CERT_PATH")

    def dockerCertPathCredentialId = yaml.load(configText).dockerCertPathCredentialId
    def jenkinsSlaveCredentialId = yaml.load(configText).jenkinsSlaveCredentialId

    def systemCredential = SystemCredentialsProvider.getInstance()

    Map<Domain, List<Credentials>> domainCredentialsMap = systemCredential.getDomainCredentialsMap()

    domainCredentialsMap[Domain.global()].add(
            new UsernamePasswordCredentialsImpl(
                    CredentialsScope.SYSTEM,
                    jenkinsSlaveCredentialsId,
                    'Jenkins Build Slave Docker Container Credential',
                    'jenkins',
                    'jenkins'
            )
    )

    domainCredentialsMap[Domain.global()].add(
            new com.nirima.jenkins.plugins.docker.utils.DockerDirectoryCredentials(
                    CredentialsScope.SYSTEM,
                    dockerCertPathCredentialsId,
                    'Contains certificates necessary to establish a connection to Docker',
                    dockerCertPath
            )
    )
    systemCredential.save()

    /**
     TODO:
     * https://github.com/samrocketman/jenkins-bootstrap-jervis/blob/dee6430a075d8862d6f0a854769c9a42477fbe9e/scripts/configure-docker-cloud.groovy
     * https://github.com/batmat/jez/blob/master/jenkins-master/init_scripts/configure_docker_swarm_cloud.groovy
     **/

    def dockerSettings = [:]
    dockerSettings =
            [
                    [
                            name             : yaml.load(configText).swarmMasterName,
                            serverUrl        : yaml.load(configText).swarmMasterUrl,
                            containerCapStr  : yaml.load(configText).containerCap,
                            connectionTimeout: yaml.load(configText).connectionTimeout,
                            readTimeout      : yaml.load(configText).readTimeout,
                            credentialsId    : dockerCertPathCredentialsId,
                            version          : yaml.load(configText).version,
                            templates        : [
                                    [
                                            image                  : 'batmat/jenkins-ssh-slave',
                                            labelString            : 'some label for demo',
                                            remoteFs               : '',
                                            credentialsId          : jenkinsSlaveCredentialsId,
                                            idleTerminationMinutes : '5',
                                            sshLaunchTimeoutMinutes: '1',
                                            jvmOptions             : '',
                                            javaPath               : '',
                                            memoryLimit            : 2500,
                                            memorySwap             : 0,
                                            cpuShares              : 0,
                                            prefixStartSlaveCmd    : '',
                                            suffixStartSlaveCmd    : '',
                                            instanceCapStr         : '100',
                                            dnsString              : '',
                                            dockerCommand          : '',
                                            volumesString          : '',
                                            volumesFromString      : '',
                                            hostname               : '',
                                            bindPorts              : '',
                                            bindAllPorts           : false,
                                            privileged             : false,
                                            tty                    : false,
                                            macAddress             : ''
                                    ]
                            ]
                    ]
            ]

    def dockerClouds = []
    dockerSettings.each { cloud ->
        def templates = []
        cloud.templates.each { template ->
            def dockerTemplateBase =
                    new DockerTemplateBase(
                            template.image,
                            template.dnsString,
                            template.dockerCommand,
                            template.volumesString,
                            template.volumesFromString,
                            template.environmentsString,
                            template.lxcConfString,
                            template.hostname,
                            template.memoryLimit,
                            template.memorySwap,
                            template.cpuShares,
                            template.bindPorts,
                            template.bindAllPorts,
                            template.privileged,
                            template.tty,
                            template.macAddress
                    )

            def dockerTemplate =
                    new DockerTemplate(
                            dockerTemplateBase,
                            template.labelString,
                            template.remoteFs,
                            template.remoteFsMapping,
                            template.instanceCapStr
                    )

            def dockerComputerSSHLauncher = new DockerComputerSSHLauncher(
                    new hudson.plugins.sshslaves.SSHConnector(22, template.credentialsId, null, null, null, null, null)
            )

            dockerTemplate.setLauncher(dockerComputerSSHLauncher)

            dockerTemplate.setMode(Node.Mode.NORMAL)
            dockerTemplate.setNumExecutors(1)
            dockerTemplate.setRemoveVolumes(true)
            dockerTemplate.setRetentionStrategy(new DockerOnceRetentionStrategy(10))
            dockerTemplate.setPullStrategy(DockerImagePullStrategy.PULL_LATEST)

            templates.add(dockerTemplate)
        }

        dockerClouds.add(
                new DockerCloud(cloud.name,
                        templates,
                        cloud.serverUrl,
                        cloud.containerCapStr,
                        cloud.connectTimeout ?: 15,
                        cloud.readTimeout ?: 15,
                        cloud.credentialsId,
                        cloud.version
                )
        )
    }

    Jenkins.instance.clouds.addAll(dockerClouds)
    println 'Configured docker cloud.'
}