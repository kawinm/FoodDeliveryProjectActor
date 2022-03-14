FROM adoptopenjdk/openjdk11:alpine-jre
ARG JAR_FILE=target/*.jar
ARG INITIAL_FILE=initialData.txt
COPY ${JAR_FILE} app.jar
COPY ${INITIAL_FILE} initialData.txt
ENTRYPOINT ["java","-jar","/app.jar"]
