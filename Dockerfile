FROM gcr.io/distroless/java21
ENV TZ="Europe/Oslo"
# ENV JDK_JAVA_OPTIONS="-Djava.security.egd=file:/dev/./urandom -Dspring.profiles.active=remote -Xmx1024M -Xms512M"
# ENV JDK_JAVA_OPTIONS="-XX:MaxRAMPercentage=75 -Dlogback.configurationFile=logback.xml"
WORKDIR /app
COPY build/libs/*.jar app.jar
COPY init.sh /init-scripts/init.sh
ENTRYPOINT ["java", "-jar", "app.jar"]
LABEL org.opencontainers.image.source=https://github.com/navikt/syfooppfolgingsplanservice
