FROM maven:3.3-jdk-8

ENV TIMBUCTOO_URL=http://localhost:8080
EXPOSE 8080 8081

WORKDIR /app
RUN mkdir -p /data/auth/authorizations
RUN mkdir -p /data/database
RUN echo "[]" > /data/auth/logins.json
RUN echo "[]" > /data/auth/users.json

COPY . ./
RUN mvn clean package

CMD ["./timbuctoo-instancev4/target/appassembler/bin/timbuctoo", "server", "./timbuctoo-instancev4/docker_config.yaml"]
