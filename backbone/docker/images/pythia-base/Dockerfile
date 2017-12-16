FROM alpine:latest

MAINTAINER nic selzer <nic@nicseltzer.com>

# install golang and golang dependencies
RUN apk update && \
    apk add curl git mercurial bzr 'go=1.9.2-r1' musl-dev && \
    rm -rf /var/cache/apk/*

ENV GOROOT /usr/lib/go
ENV GOPATH /gopath
ENV GOBIN /gopath/bin
ENV PATH $PATH:$GOROOT/bin:$GOPATH/bin

RUN go get -u github.com/cloudflare/cfssl/cmd/cfssl

# Exposes nothing; simply a base upon which other images can and should be built