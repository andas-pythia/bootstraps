FROM consul:latest

LABEL author='nic seltzer <nic@nicseltzer.com>'

# install golang and golang dependencies
RUN apk update && \
    apk add curl git mercurial bzr 'go=1.8.4-r0' musl-dev && \
    rm -rf /var/cache/apk/*

ENV GOROOT /usr/lib/go
ENV GOPATH /gopath
ENV GOBIN /gopath/bin
ENV PATH $PATH:$GOROOT/bin:$GOPATH/bin

RUN go get -u github.com/cloudflare/cfssl/cmd/cfssl

COPY config.json /consul/config/base-config.json
VOLUME ["/consul/data"]

EXPOSE 8300
EXPOSE 8301 8301/udp 8302 8302/udp
EXPOSE 8500 8600 8600/udp

CMD ["agent", "-server"]
