# Jenkins

## Notes

None at this time.

## Build jobs

### Goals

Jenkins should be able to:

1. bring up a build slave of a certain type
1. execute bulid steps / pipeline
1. register the distributable binary or docker container to a registry or centralized repository

Jenkins should then be able to:

1. bring up a build slave of a certain type
1. execute deploy steps
1. the deployment method should use terraform to define the state of the remote host

### Requirements

- thin build slaves with the minimum required footprint for a task
- terraform to be used a core tool for deployment
