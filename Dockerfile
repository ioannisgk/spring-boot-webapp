FROM tomcat:9.0.82-jdk17-temurin
COPY ./target/spring-webapp-0.0.1-SNAPSHOT.war /usr/local/tomcat/webapps/ROOT.war
EXPOSE 8080
CMD ["catalina.sh", "run"]
