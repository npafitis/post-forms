FROM openjdk:8-alpine

COPY target/uberjar/post-forms.jar /post-forms/app.jar

EXPOSE 3000

CMD ["java", "-jar", "/post-forms/app.jar"]
