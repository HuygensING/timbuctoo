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

CMD ["./timbuctoo-instancev4/target/appassembler/bin/timbuctoo", "server", "./timbuctoo-instancev4/example_config.yaml"]

RUN mkdir -p /root/data/dataSets

ENV timbuctoo_elasticsearch_host=http://example.com/elasticsearchhost
ENV timbuctoo_elasticsearch_port=80
ENV timbuctoo_elasticsearch_user=user
ENV timbuctoo_elasticsearch_password=password
ENV timbuctoo_indexer_url=http://indexer
ENV timbuctoo_dataPath="/root/data"
ENV timbuctoo_authPath="/root/data/auth"
ENV timbuctoo_port="80"
ENV timbuctoo_adminPort="81"
ENV base_uri=http://localhost:8080
ENV timbuctoo_search_url=http://localhost:8082
ENV timbuctoo_gui_public_url=http://localhost:8082/overview/
