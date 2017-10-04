FROM huygensing/timbuctoo:buildbase

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
COPY ./timbuctoo-instancev4/example_config.yaml ./timbuctoo-instancev4/example_config.yaml
RUN mvn clean package

COPY ./timbuctoo-instancev4/docker_config.yaml ./timbuctoo-instancev4/docker_config.yaml

CMD ["./timbuctoo-instancev4/target/appassembler/bin/timbuctoo", "server", "./timbuctoo-instancev4/docker_config.yaml"]

RUN mkdir -p /root/data/dataSets

ENV TIMBUCTOO_ELASTICSEARCH_HOST=http://example.com/elasticsearchhost
ENV TIMBUCTOO_ELASTICSEARCH_PORT=80
ENV TIMBUCTOO_ELASTICSEARCH_USER=user
ENV TIMBUCTOO_ELASTICSEARCH_PASSWORD=password
