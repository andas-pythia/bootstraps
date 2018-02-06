FROM localhost:5000/pythia-hadoop

LABEL author='nic seltzer <nic@nicseltzer.com>'

# install golang and golang dependencies
# RUN yum upgrade -y

# RUN touch /var/lib/rpm/*

# CentOS workaround to bring the contents of /var/lib/rpm/* to the overlay FS
# This may not be necessary in POSIX fses, but it's needed by Windows at the time
# of this writing: 12/10/2017
RUN touch /var/lib/rpm/* &&\
    yum install yum-plugin-ovl -y

RUN yum install wget curl git mercurial bzr -y

RUN wget https://storage.googleapis.com/golang/go1.9.2.linux-amd64.tar.gz && \
    tar -xzf go1.9.2.linux-amd64.tar.gz && \
    mv go /usr/local && \
    rm -rf go1.9.2.linux-amd64.tar.gz

ENV GOROOT=/usr/local/go
ENV GOPATH /gopath
ENV GOBIN /gopath/bin
ENV PATH $PATH:$GOROOT/bin:$GOPATH/bin

RUN go get -u github.com/cloudflare/cfssl/cmd/cfssl



# Application logic goes here.
