#!/bin/bash

if [ ! -e docker.env ]; then
	touch docker.env
fi
chmod a+w solr/fwb solr/fwb/core.properties solr/fwboffline solr/fwboffline/core.properties
docker-compose up -d solr
