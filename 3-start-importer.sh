#!/bin/bash

docker-compose up -d gitclone
docker-compose up -d --build importer
