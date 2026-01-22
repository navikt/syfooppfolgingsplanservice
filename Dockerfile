FROM europe-north1-docker.pkg.dev/cgr-nav/pull-through/nav.no/jre:openjdk-21
LABEL maintainer="Team-esyfo"

ENV LANG='nb_NO.UTF-8' LANGUAGE='nb_NO:nb' LC_ALL='nb:NO.UTF-8' TZ="Europe/Oslo"
ENV JAVA_TOOL_OPTIONS="-Xmx1024M \
        -Xms512M\
        -Djava.security.egd=file:/dev/./urandom \
        -Djavax.net.debug=ssl,handshake"

COPY build/libs/app.jar /app/app.jar
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
