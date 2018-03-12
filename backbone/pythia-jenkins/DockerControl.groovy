#!/usr/bin/env groovy

public class DockerControl {

  static String appName = "pythia-jenkins"
  static String appVersion = "latest"

  private static String getName() {
    return appName
  }

  private static String getVersion() {
    return appVersion
  }

  private static String getPid(String dockerContainerName) {
    String dockerPid
    String shellCommand = "docker ps --filter name=${dockerContainerName} -q" as String
    dockerPid = shellCommand.execute().text
    assert dockerPid : "The PID for ${dockerContainerName} could not be found"
    return dockerPid
  }

  private static String start(String dockerContainerName, String dockerImageVersion) {
    // this method requires that ports be defined or removed; will not work
    clean(dockerContainerName)
    String shellCommand = "docker run -d " +
            "-p 8080:8080 " +
            "-p 50000:50000 " +
            "--name ${dockerContainerName} " +
            "--restart unless-stopped " +
            "${dockerContainerName}:${dockerImageVersion}" as String
    println shellCommand
    def process = shellCommand.execute()
    return process.text
  }

  private static String stop(String dockerPid) {
    // https://stackoverflow.com/questions/159148/groovy-executing-shell-commands
    String shellCommand = "docker stop ${dockerPid}" as String
    println shellCommand
    def process = shellCommand.execute()
    process.waitForOrKill(1000)
    return process.text
  }

  private static String build(String dockerContainerName) {
    String shellCommand = "docker build --rm -t ${dockerContainerName} ." as String
    println shellCommand
    def process = shellCommand.execute()
    process.waitForOrKill(120000)
    return process.text
  }

  private static String clean(String dockerContainerName) {
    String shellCommand = "docker rm ${dockerContainerName}" as String
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
    def cli = new CliBuilder(usage: "groovy ./DockerControl.groovy " +
            "[build, start, buildrun, stop, restart, status, clean]", posix: false)
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
