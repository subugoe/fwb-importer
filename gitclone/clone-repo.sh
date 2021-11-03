#!/bin/bash
GIT_URL=https://github.com/subugoe/fwb-daten
gitRepo="/git/fwb-daten"

if [[ $GIT_URL == https://* ]]; then
  PROTOCOL="https://"
  GIT_URL_PART=${GIT_URL:8}
fi

if [ ! -d "${gitRepo}" ]; then
	cd /git || exit
  echo "git clone $PROTOCOL${GIT_USER}:${GIT_PASSWORD}@$GIT_URL_PART"
	git clone $PROTOCOL"${GIT_USER}":"${GIT_PASSWORD}"@"$GIT_URL_PART"
fi
