FROM tomcat:9.0.52-jdk11-openjdk-slim
COPY target/scala-2.13/docker-registry.war webapps/ROOT.war
