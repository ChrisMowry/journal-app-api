###################################################################
## This is a simple multi stage docker file
## The first bit builds the application jar and the second bit
## creates the runtime container.
####################################################################

### Runtime container
FROM docker.nexus.heb.tools/eclipse-temurin:21
WORKDIR /usr/service
EXPOSE 8080 8081
COPY target/service-all.jar app.jar

ENTRYPOINT ["java","-XX:MaxRAMPercentage=75.0","-XX:+UseContainerSupport","-jar","/app/app.jar"]

