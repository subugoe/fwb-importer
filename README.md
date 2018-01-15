# Data importer for FWB-online (Frühneuhochdeutsches Wörterbuch online)

## Description

This project extends the solr-importer (https://github.com/subugoe/solr-importer) by implementing some plugin classes. The project's main purpose is to convert specifically formatted TEI files to Solr XML files. It also contains plugin classes which assert that the results are correct. Furthermore, it contains the complete configuration which is needed for a Solr server (schema, solrconfig.xml, etc.). In combination with the solr-importer, it becomes a web user interface (UI) tool to convert and import files delivered by the FWB-online project into the Solr server of its web site (https://fwb-online.de).

## System requirements

Docker, docker-compose, git.

## Installation of the importer

For convenience purposes, all installation commands are encapsulated inside shell scripts. If you need to execute finer-grained commands or if you are on Windows, you should look into the shell script files. In general, they contain simple docker-compose or git commands.

Note: all the following commands must be executed inside this project's main directory.

The first thing you have to do is add the solr-importer to the project:

```./1-clone-solr-importer.sh```

Next, you must configure the importer by creating and editing a text file:

```cp docker.env.dist docker.env```

```vi docker.env```

(Retrieve the necessary data from a different source or create new repositories etc.)

Now you can compile the combination of the two projects by executing:

```./2-compile.sh```

In the background, Docker images will be downloaded and built, so on the first time it might take a while. Also, the projects are compiled with Gradle, but it is all contained inside of Docker images, so you should not worry about it.

By default, the importer is configured to start on port 9090. If you want to change this (e. g to port 9091), you have to edit the file docker-compose.yml:

```  importer:

    ...
    
    ports:
    
      - 9091:8080```

## Starting the importer



## Installing and starting a Solr server