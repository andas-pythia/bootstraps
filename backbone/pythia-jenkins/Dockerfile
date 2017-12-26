FROM jenkins/jenkins:lts

ENV JAVA_OPTS="-Djenkins.install.runSetupWizard=false"
ENV JENKINS_CONFIG_PATH $JENKINS_HOME/init-configs

# Plugin installation
COPY plugins.txt /usr/share/jenkins/ref/plugins.txt
RUN /usr/local/bin/install-plugins.sh < /usr/share/jenkins/ref/plugins.txt

# Jenkins Global Configuration
COPY jenkins_config.yml $JENKINS_CONFIG_PATH/main_config.yml
COPY /src/main/groovy/SetJenkinsMainConfig.groovy /usr/share/jenkins/ref/init.groovy.d/00MainConfig.groovy

# User Configuration
COPY security.yml $JENKINS_CONFIG_PATH/security.yml
COPY /src/main/groovy/ConfigureSecurityPlugin.groovy /usr/share/jenkins/ref/init.groovy.d/01Security.groovy

# Credential Import
COPY credentials/* $JENKINS_CONFIG_PATH/credentials/
COPY credentials.yml $JENKINS_CONFIG_PATH/credentials.yml
COPY /src/main/groovy/SetCredentials.groovy /usr/share/jenkins/ref/init.groovy.d/10Credentials.groovy

# Git Global Configuration
COPY /src/main/groovy/SetGitGlobalConfig.groovy /usr/share/jenkins/ref/init.groovy.d/20ScmGit.groovy

EXPOSE 8080
EXPOSE 50000