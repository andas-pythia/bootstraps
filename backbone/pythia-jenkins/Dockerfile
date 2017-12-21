FROM jenkins/jenkins:lts

ENV JAVA_OPTS="-Djenkins.install.runSetupWizard=false"

# Plugin installation
COPY plugins.txt /usr/share/jenkins/ref/plugins.txt
RUN /usr/local/bin/install-plugins.sh < /usr/share/jenkins/ref/plugins.txt

# User Configuration
COPY adminPassword.txt /run/secrets/jenkins-pass
COPY /src/main/groovy/SetAdminUserDetail.groovy /usr/share/jenkins/ref/init.groovy.d/0_security.groovy

# Git Global Configuration
COPY /src/main/groovy/SetGitGlobalConfig.groovy /usr/share/jenkins/ref/init.groovy.d/10_scm_git.groovy

EXPOSE 8080
EXPOSE 50000