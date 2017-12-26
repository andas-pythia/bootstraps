FROM jenkins/jenkins:lts

ENV JAVA_OPTS="-Djenkins.install.runSetupWizard=false"
ENV JENKINS_CONFIG_PATH $JENKINS_HOME/init-configs

# Plugin installation
COPY plugins.txt /usr/share/jenkins/ref/plugins.txt
RUN /usr/local/bin/install-plugins.sh < /usr/share/jenkins/ref/plugins.txt

# Jenkins Global Configuration
COPY modules/jenkins_config.yml $JENKINS_CONFIG_PATH/main_config.yml
COPY /src/main/groovy/SetJenkinsMainConfig.groovy /usr/share/jenkins/ref/init.groovy.d/00MainConfig.groovy

# CSRF Configuration
COPY /src/main/groovy/ConfigureCSRF.groovy /usr/share/jenkins/ref/init.groovy.d/01Csrf.groovy

# Disable CLI
COPY /src/main/groovy/DisableJenkinsCli.groovy /usr/share/jenkins/ref/init.groovy.d/02Cli.groovy

# Credential Import
COPY modules/credentials/* $JENKINS_CONFIG_PATH/credentials/
COPY modules/credentials.yml $JENKINS_CONFIG_PATH/credentials.yml
COPY /src/main/groovy/SetCredentials.groovy /usr/share/jenkins/ref/init.groovy.d/10Credentials.groovy

# Git Global Configuration
COPY modules/git_config.yml $JENKINS_CONFIG_PATH/git_config.yml
COPY /src/main/groovy/SetGitConfig.groovy /usr/share/jenkins/ref/init.groovy.d/20ScmGit.groovy

EXPOSE 8080
EXPOSE 50000