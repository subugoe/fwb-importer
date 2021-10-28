FROM gradle:6.8 as build

WORKDIR /project

COPY . /project

CMD gradle --gradle-user-home /project/.gradle-user-home

FROM java:8

RUN apt-get install -y git

COPY --from=build /project/solr-importer/web/build/libs/web-0.0.1-SNAPSHOT.jar /tmp/web-importer.jar

CMD java -jar /tmp/web-importer.jar
