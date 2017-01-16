FROM huygensing/timbuctoo:build-base

ENV BASE_URI=http://localhost:8080
ENV TIMBUCTOO_SEARCH_URL=http://localhost:8082
EXPOSE 80 81

WORKDIR /app
RUN mkdir -p /data/auth/authorizations
RUN mkdir -p /data/database
RUN echo "[]" > /data/auth/logins.json
RUN echo "[]" > /data/auth/users.json

RUN apt-get update && apt-get install -y --no-install-recommends jq && rm -rf /var/lib/apt/lists/*

COPY ./ContractDiff ./ContractDiff
COPY ./HttpCommand ./HttpCommand
COPY ./security-client-agnostic ./security-client-agnostic
COPY ./timbuctoo-test-services ./timbuctoo-test-services
COPY ./timbuctoo-instancev4/src ./timbuctoo-instancev4/src
COPY ./timbuctoo-instancev4/pom.xml ./timbuctoo-instancev4/pom.xml
COPY ./pom.xml ./pom.xml

# FIXME: do a maven install and then run appassembler with generateRepository=false and specify /root/.m2 as the REPO
# variable
# This will save unnecessary package copying from the local repository to the target folder. Making the image smaller
RUN mvn clean package

COPY ./timbuctoo-instancev4/docker_config.yaml ./timbuctoo-instancev4/docker_config.yaml
COPY ./devtools/debugrun/debugrun.sh ./timbuctoo-instancev4/debugrun.sh

CMD ["./timbuctoo-instancev4/target/appassembler/bin/timbuctoo", "server", "./timbuctoo-instancev4/docker_config.yaml"]
