FROM jenkins/jenkins:lts

ENV JAVA_OPTS="-Djenkins.install.runSetupWizard=false"
ENV JENKINS_CONFIG_PATH $JENKINS_HOME/init-configs
ENV JENKINS_INIT_PATH /usr/share/jenkins/ref/init.groovy.d

# Plugin installation
COPY plugins.txt /usr/share/jenkins/ref/plugins.txt
RUN /usr/local/bin/install-plugins.sh < /usr/share/jenkins/ref/plugins.txt

# Jenkins Global Configuration
COPY modules/jenkins.yml $JENKINS_CONFIG_PATH/main_config.yml
COPY /src/main/groovy/SetJenkinsMainConfig.groovy $JENKINS_INIT_PATH/00MainConfig.groovy

# CSRF Configuration
COPY /src/main/groovy/ConfigureCSRF.groovy $JENKINS_INIT_PATH/01Csrf.groovy

# Disable CLI
COPY /src/main/groovy/DisableJenkinsCli.groovy $JENKINS_INIT_PATH/02Cli.groovy

# Secure JNLP
COPY /src/main/groovy/SecureJenkinsJnlp.groovy $JENKINS_INIT_PATH/03Jnlp.groovy

# Credential Import
COPY modules/credentials/* $JENKINS_CONFIG_PATH/credentials/
COPY modules/credentials.yml $JENKINS_CONFIG_PATH/credentials.yml
COPY /src/main/groovy/SetCredentials.groovy $JENKINS_INIT_PATH/10Credentials.groovy

# Git Global Configuration
COPY modules/git_config.yml $JENKINS_CONFIG_PATH/git_config.yml
COPY /src/main/groovy/SetGitConfig.groovy $JENKINS_INIT_PATH/20ScmGit.groovy

EXPOSE 8080
EXPOSE 50000
