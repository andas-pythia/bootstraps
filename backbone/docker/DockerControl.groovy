import groovy.util.CliBuilder


public class DockerControl {

  static def appName = "pythia-placeholder"
  static def appVersion = "latest"

  private static String getName() {
    return this.appName
  }

  private static String getVersion() {
    return this.appVersion
  }

  private static String getPid(String dockerContainerName) {
    String dockerPid
    String shellCommand = "docker ps --filter name=${dockerContainerName} -q"
    dockerPid = shellCommand.execute().text
    assert dockerPid : "The PID for ${dockerContainerName} could not be found"
    return dockerPid
  }

  private static String start(String dockerContainerName, String dockerImageVersion) {
    // this method requires that ports be defined or removed; will not work
    clean(dockerContainerName)
    String shellCommand = "docker run -d -p port:port -p port:port --name ${dockerContainerName} --restart unless-stopped ${dockerContainerName}:${dockerImageVersion}"
    println shellCommand
    def process = shellCommand.execute()
    process.waitForProcessOutput()
    return process.text
  }

  private static String stop(String dockerPid) {
    // https://stackoverflow.com/questions/159148/groovy-executing-shell-commands
    String shellCommand = "docker stop ${dockerPid}"
    println shellCommand
    def process = shellCommand.execute()
    process.waitForProcessOutput()
    return process.text
  }

  private static String build(String dockerContainerName) {
    String shellCommand = "docker build --rm -t ${dockerContainerName} ."
    println shellCommand
    def process = shellCommand.execute()
    process.waitForProcessOutput()
    return process.text
  }

  private static String clean(String dockerContainerName) {
    String shellCommand = "docker rm ${dockerContainerName}"
    println shellCommand
    def process = shellCommand.execute()
    process.waitForProcessOutput()
    return process.text
  }

  private static void buildAndRun(String dockerContainerName, String dockerImageVersion) {
    build(dockerContainerName)
    start(dockerContainerName)
  }

  private static void restart(String dockerPid, String dockerContainerName) {
    stop(dockerPid)
    start(dockerContainerName)
  }

  private static String status(String dockerContainerName) {
    String shellCommand = "docker inspect ${dockerContainerName}"
    println shellCommand
    def process = shellCommand.execute()
    return process.text
  }

  public static void main(String[] args) {
    def cli = new CliBuilder(usage: "groovy ./DockerControl.groovy [build, start, buildrun, stop, restart, status, clean]", posix: false)
    def options = cli.parse(args)
    try {
      assert options.arguments()
    } catch (AssertionError ex) {
      println "Error: A verb is required."
      cli.usage()
      return
    }

    String dockerContainerName = getName()
    String dockerImageVersion = getVersion()
    String verb = options.arguments()[0]

    switch(verb) {
      case 'build':
        build(dockerContainerName)
        break
      case 'start':
        start(dockerContainerName, dockerImageVersion)
        break
      case 'buildrun':
        buildAndRun(dockerContainerName, dockerImageVersion)
        break
      case 'stop':
        String dockerPid = getPid(dockerContainerName)
        stop(dockerPid)
        break
      case 'restart':
        String dockerPid = getPid(dockerContainerName)
        restart(dockerPid, dockerContainerName)
        break
      case 'status':
        status(dockerContainerName)
        break
      case 'clean':
        clean(dockerContainerName)
        break
      default:
        cli.usage()
        break
    }
  }
}
