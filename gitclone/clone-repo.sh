#!/bin/bash

gitRepo="/git/fwb-daten"

if [[ $GIT_URL == https://* ]]; then
  PROTOCOL="https://"
  GIT_URL_PART=${GIT_URL:8}
elif [[ $GIT_URL == http://* ]]; then
  PROTOCOL="http://"
  GIT_URL_PART=${GIT_URL:7}
fi

if [ ! -d "$gitRepo" ]; then
	cd /git
	git clone $PROTOCOL${GIT_USER}:${GIT_PASSWORD}@$GIT_URL_PART
fi