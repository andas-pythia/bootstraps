import jenkins.model.*

def instance = Jenkins.getInstance()

def descriptor = inst.getDescriptor("hudson.plugins.git.GitSCM")

descriptor.setGlobalConfigName("Pythia Build System")
descriptor.setGlobalConfigEmail("noreply@nicseltzer.com")

descriptor.save()