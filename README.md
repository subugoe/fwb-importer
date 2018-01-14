# Data importer for FWB (Frühneuhochdeutsches Wörterbuch)

## Description

This project extends the solr-importer (https://github.com/subugoe/solr-importer) by implementing some plugin classes. The project's main purpose is to convert specifically formatted TEI files to Solr XML files. It also contains plugin classes which assert that the results are correct. Furthermore, it contains the complete configuration which is needed for a Solr server (schema, solrconfig.xml, etc.). In combination with the solr-converter, it becomes a web user interface (UI) tool to convert and import files delivered by the FWB project.

## System requirements

Docker, docker-compose, git.

## Installation

For convenience purposes, all installation commands are encapsulated inside shell scripts. If you need to execute more fine-grained commands or if you are on Windows, you should look into the shell script files.

1-update-sources.sh

