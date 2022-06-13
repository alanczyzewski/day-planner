FROM openjdk:17-jdk-alpine
ADD /target/todo-0.0.1-SNAPSHOT.jar .
EXPOSE 8000
CMD java -jar todo-0.0.1-SNAPSHOT.jar
#docker build -f Dockerfile -t app1:v1 .
#docker run -p 8000:8080 <image_ID>