//   echo "err: invalid argument. accepts:
// stop, restart, build, build-run, rebuild, start, clean, status"

import groovy.util.CliBuilder


public class DockerControl {

  private static def getName() {
    String name = "PlaceHolder"
    return name
  }

  private static String getPid(String dockerContainerName) {
    String dockerPid
    String shellCommand = "docker ps --filter name=${dockerContainerName} -q"
    dockerPid = shellCommand.execute().text
    assert dockerPid : "The PID for ${dockerContainerName} could not be found"
    return dockerPid
  }

  private static String run(String dockerContainerName) {
    clean(dockerContainerName)
    String shellCommand = "docker run -d -p 8888:8888 --name ${dockerContainerName} --restart unless-stopped ${dockerContainerName}"
    println shellCommand
    def process = shellCommand.execute()
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
    process.waitForOrKill(1000)
    return process.text
  }

  private static String clean(String dockerContainerName) {
    String shellCommand = "docker rm ${dockerContainerName}"
    println shellCommand
    def process = shellCommand.execute()
    return process.text
  }

  private static void buildAndRun(String dockerContainerName) {
    build(dockerContainerName)
    run(dockerContainerName)
  }

  private static void restart(String dockerPid, String dockerContainerName) {
    stop(dockerPid)
    run(dockerContainerName)
  }

  private static String status(String dockerContainerName) {
    String shellCommand = "docker inspect ${dockerContainerName}"
    println shellCommand
    def process = shellCommand.execute()
    return process.text
  }

  public static void main(String[] args) {
    def cli = new CliBuilder(usage: "groovy ./DockerControl.groovy [build, run, buildrun, stop, restart, status, clean]", posix: false)
    def options = cli.parse(args)
    try {
      assert options.arguments()
    } catch (AssertionError ex) {
      cli.usage()
      return // bail
    }

    String dockerContainerName = getName()
    String verb = options.arguments()[0]

    switch(verb) {
      case 'build':
        println build(dockerContainerName)
        break
      case 'run':
        println run(dockerContainerName)
        break
      case 'buildrun':
        println buildAndRun(dockerContainerName)
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
