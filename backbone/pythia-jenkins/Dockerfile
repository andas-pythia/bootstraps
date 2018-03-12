FROM jenkins/jenkins

LABEL author='nic seltzer <nic@nicseltzer.com>'

ENV JAVA_OPTS="-Djenkins.install.runSetupWizard=false"
ENV JENKINS_CONFIG_PATH $JENKINS_HOME/init-configs
ENV JENKINS_INIT_PATH /usr/share/jenkins/ref/init.groovy.d

ENV DOCKER_CERT_PATH $JENKINS_HOME/docker-certificates

EXPOSE 8080
EXPOSE 50000

# Plugin installation
COPY plugins.txt /usr/share/jenkins/ref/plugins.txt
RUN /usr/local/bin/install-plugins.sh < /usr/share/jenkins/ref/plugins.txt

# Jenkins Global Configuration
COPY modules/jenkins.yml ${JENKINS_CONFIG_PATH}/jenkins.yml
COPY /src/main/groovy/SetJenkinsMainConfig.groovy ${JENKINS_INIT_PATH}/00MainConfig.groovy

# CSRF Configuration
COPY /src/main/groovy/ConfigureCSRF.groovy ${JENKINS_INIT_PATH}/01Csrf.groovy

# Disable CLI
COPY /src/main/groovy/DisableJenkinsCli.groovy ${JENKINS_INIT_PATH}/02Cli.groovy

# Secure JNLP
COPY /src/main/groovy/SecureJenkinsJnlp.groovy ${JENKINS_INIT_PATH}/03Jnlp.groovy

# Credential Import
COPY modules/credentials/* ${JENKINS_CONFIG_PATH}/credentials/
COPY modules/credentials.yml ${JENKINS_CONFIG_PATH}/credentials.yml
COPY /src/main/groovy/SetCredentials.groovy ${JENKINS_INIT_PATH}/10Credentials.groovy

# Git Global Configuration
COPY modules/git.yml ${JENKINS_CONFIG_PATH}/git.yml
COPY /src/main/groovy/SetGitConfig.groovy ${JENKINS_INIT_PATH}/20ScmGit.groovy

# GitHub Pull Request Builder Configuration
COPY modules/ghprb.yml ${JENKINS_CONFIG_PATH}/ghprb.yml
COPY /src/main/groovy/SetGhprbConfig.groovy ${JENKINS_INIT_PATH}/21GitHubPullRequestBuilder.groovy
