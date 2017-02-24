FROM maven:3.3-jdk-8-alpine

RUN apk add --no-cache jq

ENV BASE_URI=http://localhost:8080
ENV TIMBUCTOO_SEARCH_URL=http://localhost:8082
EXPOSE 80 81

WORKDIR /app
RUN mkdir -p /data/auth/authorizations
RUN mkdir -p /data/database
RUN echo "[]" > /data/auth/logins.json
RUN echo "[]" > /data/auth/users.json

# First copy the POMs and fetch the dependencies.
# As long as the POMs don't change, the dependencies will be cached.
COPY ./pom.xml ./pom.xml
COPY ./ContractDiff/pom.xml ./ContractDiff/pom.xml
COPY ./HttpCommand/pom.xml ./HttpCommand/pom.xml
COPY ./security-client-agnostic/pom.xml ./security-client-agnostic/pom.xml
COPY ./timbuctoo-test-services/pom.xml ./timbuctoo-test-services/pom.xml
COPY ./timbuctoo-instancev4/pom.xml ./timbuctoo-instancev4/pom.xml
RUN mvn dependency:resolve-plugins

COPY ./ContractDiff/src ./ContractDiff/src
COPY ./HttpCommand/src ./HttpCommand/src
COPY ./security-client-agnostic/src ./security-client-agnostic/src
COPY ./timbuctoo-test-services/src ./timbuctoo-test-services/src
COPY ./timbuctoo-instancev4/src ./timbuctoo-instancev4/src

# FIXME: do a maven install and then run appassembler with generateRepository=false and specify /root/.m2 as the REPO
# variable
# This will save unnecessary package copying from the local repository to the target folder. Making the image smaller
RUN mvn clean package

COPY ./timbuctoo-instancev4/docker_config.yaml ./timbuctoo-instancev4/docker_config.yaml
COPY ./devtools/debugrun/debugrun.sh ./timbuctoo-instancev4/debugrun.sh

CMD ["./timbuctoo-instancev4/target/appassembler/bin/timbuctoo", "server", "./timbuctoo-instancev4/docker_config.yaml"]
