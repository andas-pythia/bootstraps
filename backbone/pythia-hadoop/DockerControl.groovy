import groovy.util.CliBuilder


public class DockerControl {

  static def appName = "pythia-hadoop"
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
    String shellCommand = "docker run -d -p 50010:50010 -p 50020:50020 -p 50070:50070 -p 50075:50075-p 50090:50090-p 8020:8020-p 9000:9000-p 10020:10020-p 19888:19888-p 8030:8030-p 8031:8031-p 8032:8032-p 8033:8033-p 8040:8040-p 8042:8042-p 8088:8088-p 49707:49707-p 2122:2122 --name ${dockerContainerName} --restart unless-stopped ${dockerContainerName}:${dockerImageVersion}"
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
    process.waitForOrKill(1000)
    return process.text
  }

  private static String build(String dockerContainerName) {
    String shellCommand = "docker build --rm -t ${dockerContainerName} ."
    println shellCommand
    def process = shellCommand.execute()
    process.waitForOrKill(120000)
    return process.text
  }

  private static String clean(String dockerContainerName) {
    String shellCommand = "docker rm ${dockerContainerName}"
    println shellCommand
    def process = shellCommand.execute()
    return process.text
  }

  private static void buildAndRun(String dockerContainerName, String dockerImageVersion) {
    build(dockerContainerName)
    start(dockerContainerName, dockerImageVersion)
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
      return // bail in a cross-platform way
    }

    String dockerContainerName = getName()
    String dockerImageVersion = getVersion()
    String verb = options.arguments()[0]

    switch(verb) {
      case 'build':
        println build(dockerContainerName)
        break
      case 'start':
        println start(dockerContainerName, dockerImageVersion)
        break
      case 'buildrun':
        println buildAndRun(dockerContainerName, dockerImageVersion)
        break
      case 'stop':
        String dockerPid = getPid(dockerContainerName)
        println stop(dockerPid)
        break
      case 'restart':
        String dockerPid = getPid(dockerContainerName)
        println restart(dockerPid, dockerContainerName)
        break
      case 'status':
        println status(dockerContainerName)
        break
      case 'clean':
        println clean(dockerContainerName)
        break
      default:
        cli.usage()
        break
    }
  }
}
