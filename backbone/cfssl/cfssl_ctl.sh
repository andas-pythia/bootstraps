#!/bin/bash

set -x

CA_ROOT_DIR=/ca
CA_CONFIG_JSON=${CA_ROOT_DIR}/ca-config.json
CA_CSR_JSON=${CA_ROOT_DIR}/ca-csr.json

if [[ ! -e $CA_CONFIG_JSON ]]; then
  cfssl print-defaults config > $CA_CONFIG_JSON
  cfssl print-defaults csr > $CA_CSR_JSON
fi

if [[ ! -e $CA_CSR_JSON ]]; then
  cfssl genkey -initca $CA_CSR_JSON | cfssljson -bare ca
fi

echo "done" && exit 0
