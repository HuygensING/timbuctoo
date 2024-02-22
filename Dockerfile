FROM maven:3.9-eclipse-temurin-21 AS builder

WORKDIR /app

COPY ./src ./src
COPY ./pom.xml ./pom.xml
COPY ./example_config.yaml ./example_config.yaml

RUN mvn clean package

FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=builder /app/target/appassembler .
COPY --from=builder /app/example_config.yaml .

CMD ["./bin/timbuctoo", "server", "./example_config.yaml"]

EXPOSE 80 81

ENV timbuctoo_port="80"
ENV timbuctoo_adminPort="81"
ENV base_uri=http://localhost:8080

ENV timbuctoo_elasticsearch_host=http://example.com/elasticsearchhost
ENV timbuctoo_elasticsearch_port=80
ENV timbuctoo_elasticsearch_user=user
ENV timbuctoo_elasticsearch_password=password

ENV timbuctoo_dataPath="/root/data"
ENV timbuctoo_authPath="/root/data/auth"

ENV timbuctoo_search_url=http://localhost:8082
ENV timbuctoo_indexer_url=http://indexer
