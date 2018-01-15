# Data importer for FWB-online (Frühneuhochdeutsches Wörterbuch online)

## Description

This project extends the solr-importer (https://github.com/subugoe/solr-importer) by implementing some plugin classes. The project's main purpose is to convert specifically formatted TEI files to Solr XML files. It also contains plugin classes which assert that the results are correct. Furthermore, it contains the complete configuration which is needed for a Solr server (schema, solrconfig.xml, etc.). In combination with the solr-importer, it becomes a web user interface (UI) tool to convert and import files delivered by the FWB-online project into the Solr server of its web site (https://fwb-online.de).

## System requirements

Docker, docker-compose, Git.

## Installation of the importer

For convenience purposes, all installation commands are encapsulated inside shell scripts. If you need to execute finer-grained commands or if you are on Windows, you should look into the shell script files. In general, they contain simple docker-compose or Git commands.

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

```
  importer:
    ...  
    ports:
      - 9091:8080      
```

## Starting the importer

After completing the above installation, the importer with its Web UI can be started by executing:

```./3-start-importer.sh```

On the first run, the Git repository that contains all FWB TEI files will be cloned (you configured the repository in the docker.env file). This happens in the background and can last several minutes. After that, the Web UI should be available on localhost on the (default) port 9090.

Note: As a developer, you can start the importer for quick tests inside an IDE. The procedure for Eclipse is described in the README of the 'solr-importer' project (https://github.com/subugoe/solr-importer#starting-in-eclipse).

## Updating the importer

When source code is changed, the importer needs to be updated. The easiest way is again to use the shell scripts:

```
./6-update-sources.sh
./2-compile.sh
./3-start-importer.sh
```

## Installing and starting a Solr server

The solr/ directory contains all the files that are necessary to start a Solr server (version 5.4). The server's configuration and schema will be compatible with the Solr XML files produced by the importer. You can use the solr/ directory to configure your own Solr server.

However, this project also offers an out-of-the-box solution based on Docker. In a development environment, you can use the same Git clone both for the importer and for a Solr server. In a production environment, you should just clone this whole project into its own directory and use it only for Solr.

First, you can configure the port on which Solr will start. The default port is 8983. To change it to e. g. 8984, edit the file docker-compose.yml:

```
  solr:
    ...  
    ports:
      - 8984:8983      
```

Now you can start Solr:

```./4-start-solr.sh```

Note: If you want to install several Solr servers on the same host, you need to watch out for a little thing. The tool 'docker-compose' uses the parent directory name to identify Docker images. You can easily clone this project one more time and start a second Solr server. However, the parent directory must be different. The easiest way to achieve this is to clone to a directory with a different name:

```git clone ... other-directory/```

Also, it is often required to maintain several Solr servers for different environments, like 'dev' or 'live'. In that case, you can work with different Git branches in different clones.

## Updating Solr server

You can update all Solr configuration which has been changed in the remote Git repository by executing:

```
./6-update-sources.sh
./5-restart-solr.sh
```


