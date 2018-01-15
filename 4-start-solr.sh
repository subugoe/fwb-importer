#!/bin/bash

if [ ! -e docker.env ]; then
	touch docker.env
fi
chmod a+w solr/fwb solr/fwboffline
docker-compose up -d solr
