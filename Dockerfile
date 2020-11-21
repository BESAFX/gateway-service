FROM openjdk:8
COPY target/gateway-server.jar  /usr/share/food-cloud/gateway-server.jar
ENV DOMAIN=127.0.0.1
ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-Dspring.profiles.active=prod", "-jar", "/usr/share/food-cloud/gateway-server.jar"]