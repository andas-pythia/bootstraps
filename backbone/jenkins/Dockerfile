FROM jenkins/jenkins:lts

ENV JAVA_OPTS="-Djenkins.install.runSetupWizard=false"

# User Configuration
COPY admin_password.txt /run/secrets/jenkins-pass
COPY SetAdminUserDetail.groovy /usr/share/jenkins/ref/init.groovy.d/security.groovy

# Plugin installation
COPY plugins.txt /usr/share/jenkins/ref/plugins.txt
RUN /usr/local/bin/install-plugins.sh < /usr/share/jenkins/ref/plugins.txt
