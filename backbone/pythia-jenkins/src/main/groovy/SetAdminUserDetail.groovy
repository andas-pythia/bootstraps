#!groovy

import jenkins.model.*
import hudson.security.*
import jenkins.security.s2m.AdminWhitelistRule

def instance = Jenkins.getInstance()

def adminUser = "admin"
def adminPass = new File("/run/secrets/jenkins-pass").text.trim()

def hudsonRealm = new HudsonPrivateSecurityRealm(false)
hudsonRealm.createAccount(adminUser, adminPass)
hudsonRealm.createAccount("build", "build")
instance.setSecurityRealm(hudsonRealm)

def strategy = new FullControlOnceLoggedInAuthorizationStrategy()
strategy.setAllowAnonymousRead(false)
instance.setAuthorizationStrategy(strategy)
instance.save()

Jenkins.instance.getInjector().getInstance(AdminWhitelistRule.class).setMasterKillSwitch(false)