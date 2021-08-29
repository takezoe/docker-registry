FROM openjdk:8-jre

LABEL maintainer="Naoki Takezoe <takezoe [at] gmail.com>"

COPY target/executable/docker-registry.war /opt/docker-registry.war

EXPOSE 8080

CMD ["sh", "-c", "java -jar /opt/docker-registry.war"]
