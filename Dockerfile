FROM maven:3.3-jdk-8

ENV BASE_URI=http://localhost:8080
ENV TIMBUCTOO_SEARCH_URL=http://localhost:8082
EXPOSE 80 81

WORKDIR /app
RUN mkdir -p /data/auth/authorizations
RUN mkdir -p /data/database
RUN echo "[]" > /data/auth/logins.json
RUN echo "[]" > /data/auth/users.json

COPY ./ContractDiff ./ContractDiff
COPY ./HttpCommand ./HttpCommand
COPY ./security-client-agnostic ./security-client-agnostic
COPY ./timbuctoo-test-services ./timbuctoo-test-services
COPY ./timbuctoo-instancev4/src ./timbuctoo-instancev4/src
COPY ./timbuctoo-instancev4/pom.xml ./timbuctoo-instancev4/pom.xml
COPY ./pom.xml ./pom.xml

RUN mvn clean package

COPY ./timbuctoo-instancev4/docker_config.yaml ./timbuctoo-instancev4/docker_config.yaml
COPY ./devtools/debugrun/debugrun.sh ./timbuctoo-instancev4/debugrun.sh

CMD ["./timbuctoo-instancev4/target/appassembler/bin/timbuctoo", "server", "./timbuctoo-instancev4/docker_config.yaml"]
