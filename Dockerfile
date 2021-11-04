FROM gradle:6.8 as build
ARG GIT_URL

WORKDIR /project

COPY . /project
RUN git clone https://github.com/subugoe/solr-importer && \
    gradle --gradle-user-home /project/.gradle-user-home

FROM java:8
ARG GIT_URL

RUN mkdir /git && \
    cd /git && \
    git clone $GIT_URL

COPY --from=build /project/solr-importer/web/build/libs/web-0.0.1-SNAPSHOT.jar /tmp/web-importer.jar

CMD java -jar /tmp/web-importer.jar
