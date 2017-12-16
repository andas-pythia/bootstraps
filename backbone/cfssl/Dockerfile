FROM cfssl/cfssl:latest

MAINTAINER nic seltzer <nic@nicseltzer.com>

EXPOSE 8888

VOLUME ["/ca"]

RUN mkdir /ca/
WORKDIR /ca/
COPY ./cfssl_ctl.sh /ca/
RUN ./cfssl_ctl.sh

ENTRYPOINT ["cfssl"]

CMD ["serve","-ca=ca.pem","-ca-key=ca-key.pem","-address=0.0.0.0"]
