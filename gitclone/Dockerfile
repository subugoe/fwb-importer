FROM debian:8

RUN apt-get update && apt-get install -y git

COPY clone-repo.sh /tmp/clone-repo.sh

CMD /tmp/clone-repo.sh
