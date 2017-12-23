#!groovy

import jenkins.model.*
import hudson.security.*
import java.util.logging.Logger
import jenkins.security.s2m.AdminWhitelistRule

def logger = Logger.getLogger("")
def instance = Jenkins.getInstance()
def adminUser = "admin"
def adminPass = new File("/run/secrets/jenkins-pass").text.trim()

def hudsonRealm = new HudsonPrivateSecurityRealm(false)

hudsonRealm.createAccount(adminUser, adminPass)

instance.setSecurityRealm(hudsonRealm)

def strategy = new FullControlOnceLoggedInAuthorizationStrategy()
strategy.setAllowAnonymousRead(false)
instance.setAuthorizationStrategy(strategy)
instance.save()

Jenkins.instance.getInjector()
       .getInstance(AdminWhitelistRule.class)
       .setMasterKillSwitch(false)

static main(args) {
    new SetAdminUserDetail()
}
