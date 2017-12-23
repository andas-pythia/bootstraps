import java.util.logging.Logger
import jenkins.model.*

def logger = Logger.getLogger("")
def instance = Jenkins.getInstance()
def descriptor = instance.getDescriptor("hudson.plugins.git.GitSCM")

descriptor.setGlobalConfigName("Pythia Build System")
descriptor.setGlobalConfigEmail("noreply@nicseltzer.com")
descriptor.save()