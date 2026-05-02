FROM alpine/java:21-jre

COPY target/ai-document-1.0.0.jar /app.jar
EXPOSE 8099
ENTRYPOINT ["java","-jar","/app.jar"]
