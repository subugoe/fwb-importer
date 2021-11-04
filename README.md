# Data importer for FWB-online (Frühneuhochdeutsches Wörterbuch online)

## Description

This project extends the solr-importer (https://github.com/subugoe/solr-importer) by implementing some plugin classes.
The project's main purpose is to convert specifically formatted TEI files to Solr XML files. It also contains plugin
classes which assert that the results are correct. Furthermore, it contains the complete configuration which is needed
for a Solr server (schema, solrconfig.xml, etc.). In combination with the solr-importer, it becomes a web user
interface (UI) tool that can be used to convert and import files delivered by the FWB-online project into the Solr
server of its web site (https://fwb-online.de).

Here in this readme, you can read how to operate the project. In the wiki (https://github.com/subugoe/fwb-importer/wiki)
you can read about the structure of the application. For more information on the code, refer to
https://github.com/subugoe/fwb-importer/blob/master/fwb-plugin/src/main/java/docs/FwbPluginDocumentation.java.

## System requirements

Docker, docker-compose, Git.

## Installation of the importer

For convenience purposes, all installation commands are encapsulated inside shell scripts.
If you need to execute finer-grained commands or if you are on Windows, you should look into the shell script files.
In general, they contain simple docker-compose or Git commands.

Note: All the following commands must be executed inside this project's main directory.

The first thing you have to do is add the solr-importer to the project:

```git clone https://github.com/subugoe/solr-importer```

Next, you must configure the importer by creating and editing a text file:

```cp docker.env.dist docker.env```

```vi docker.env```

(You will have to retrieve the necessary data from a different source or create new repositories etc.)

Now you can compile the combination of the two projects by executing:

```./3-start-importer.sh```

In the background, Docker images will be downloaded and built, so on the first time it might take a while. Also, the
projects are compiled with Gradle, but it is all contained inside of Docker images, so you should not worry about it.

On some systems, the compilation might fail with a message that some external library could not be loaded. In such a
case, try to add the following line to the file Dockerfile-compile (after the FROM command):

```USER root```

By default, the importer is configured to start on port 9090. If you want to change this (e. g to port 9091), you have
to edit the file docker-compose.yml:

```
  importer:
    ...  
    ports:
      - 9091:8080      
```

## Starting the importer

After completing the above installation, the importer with its Web UI can be started by executing:

```./3-start-importer.sh```

On the first run, the Git repository that contains all FWB TEI files will be cloned (you configured the repository in
the docker.env file). This happens in the background and can last several minutes. After that, the Web UI should be
available on localhost on the (default) port 9090.

Note: As a developer, you can start the importer for quick tests inside an IDE. The procedure for Eclipse is described
in the README of the 'solr-importer' project (https://github.com/subugoe/solr-importer#starting-in-eclipse).

## Updating the importer

When source code is changed, the importer needs to be updated. The easiest way is again to use the shell scripts:

```
git -C solr-importer pull
git pull
./3-start-importer.sh
```
