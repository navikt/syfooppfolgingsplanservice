group = "no.nav.syfo"
version = "1.0.0"

object Versions {
    const val avroVersion = "1.11.0"
    const val confluentVersion = "7.1.0"
    const val cxfVersion = "3.6.2"
    const val flywayVersion = "9.10.0"
    const val tokenSupportVersion = "3.2.0"
    const val tokenTestSupportVersion = "2.0.5"
    const val ojdbc8Version = "19.3.0.0"
    const val helseXmlVersion = "1.0.4"
    const val syfotjenesterVersion = "1.2020.07.02-07.44-62078cd74f7e"
    const val tjenesteSpesifikasjonerVersion = "1.2020.06.23-15.31-57b909d0a05c"
    const val altinnKanalSchemasVersion = "2.0.0"
    const val jaxwsVersion = "2.3.2"
    const val h2Version = "2.1.210"
    const val mockkVersion = "1.13.4"
    const val atomikosVersion = "6.0.0"
    const val jakartaRsApiVersion = "3.1.0"
    const val jacksonVersion = "2.15.3"
    const val apacheHttpClientVersion = "5.2.1"
    const val javaxWsVersion = "2.3.1"
    const val javaxSoapVersion = "1.3.5"
    const val jaxbVersion = "2.3.1"
    const val javaxActivationVersion = "1.2.0"
    const val jakartaSoapVersion = "1.5.1"
}

val githubUser: String by project
val githubPassword: String by project

plugins {
    kotlin("jvm") version "1.9.23"
    id("java")
    id("org.jetbrains.kotlin.plugin.allopen") version "1.9.23"
    id("org.springframework.boot") version "3.2.4"
    id("io.spring.dependency-management") version "1.1.4"
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(19))
    }
}

allOpen {
    annotation("org.springframework.context.annotation.Configuration")
    annotation("org.springframework.stereotype.Service")
    annotation("org.springframework.stereotype.Component")
}

repositories {
    mavenCentral()
    maven(url = "https://packages.confluent.io/maven/")
    maven {
        url = uri("https://maven.pkg.github.com/navikt/syfo-xml-codegen")
        credentials {
            username = githubUser
            password = githubPassword
        }
    }
    maven {
        url = uri("https://maven.pkg.github.com/navikt/syfotjenester")
        credentials {
            username = githubUser
            password = githubPassword
        }
    }
    maven {
        url = uri("https://maven.pkg.github.com/navikt/tjenestespesifikasjoner")
        credentials {
            username = githubUser
            password = githubPassword
        }
    }
}

configurations.all {
    resolutionStrategy.eachDependency {
        if (requested.group == "org.scala-lang" && requested.name == "scala-library" && (requested.version == "2.13.6")) {
            useVersion("2.13.9")
            because("fixes critical bug CVE-2022-36944 in 2.13.6")
        }
    }
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation(kotlin("reflect"))
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:${Versions.jacksonVersion}")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-xml:${Versions.jacksonVersion}")
    implementation("com.fasterxml.jackson.module:jackson-module-jaxb-annotations:${Versions.jacksonVersion}")

    implementation("com.sun.xml.ws:jaxws-ri:${Versions.jaxwsVersion}")

    implementation("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")

    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-jersey")
    implementation("org.springframework.boot:spring-boot-starter-logging")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    implementation("org.springframework.kafka:spring-kafka")

    implementation("org.springframework:spring-jms")

    implementation("no.nav.security:token-validation-spring:${Versions.tokenSupportVersion}")

    implementation("org.apache.cxf:cxf-rt-features-logging:${Versions.cxfVersion}")
    implementation("org.apache.cxf:cxf-rt-ws-security:${Versions.cxfVersion}")
    implementation("org.apache.cxf:cxf-rt-ws-policy:${Versions.cxfVersion}")
    implementation("org.apache.cxf:cxf-rt-transports-http:${Versions.cxfVersion}")
    implementation("org.apache.cxf:cxf-rt-frontend-jaxws:${Versions.cxfVersion}")

    implementation("javax.xml.bind:jaxb-api:${Versions.jaxbVersion}")
    implementation("com.sun.xml.bind:jaxb-impl:${Versions.jaxbVersion}")


    implementation("no.nav.helse.xml:oppfolgingsplan:${Versions.helseXmlVersion}")

    implementation("no.nav.syfotjenester:oppfolgingsplanlps:${Versions.syfotjenesterVersion}")
    implementation("no.nav.altinnkanal.avro:altinnkanal-schemas:${Versions.altinnKanalSchemasVersion}")

    implementation("no.nav.tjenestespesifikasjoner:altinn-correspondence-agency-external:${Versions.tjenesteSpesifikasjonerVersion}")
    implementation("no.nav.tjenestespesifikasjoner:altinn-correspondence-agency-external-basic:${Versions.tjenesteSpesifikasjonerVersion}")
    implementation("no.nav.tjenestespesifikasjoner:servicemeldingMedKontaktinformasjon-v1-tjenestespesifikasjon:${Versions.tjenesteSpesifikasjonerVersion}")
    implementation("no.nav.tjenestespesifikasjoner:varsel-inn:${Versions.tjenesteSpesifikasjonerVersion}")

    implementation("org.apache.avro:avro:${Versions.avroVersion}")
    implementation("io.confluent:kafka-avro-serializer:${Versions.confluentVersion}") {
        exclude(group = "log4j", module = "log4j")
    }

    implementation("org.flywaydb:flyway-core:${Versions.flywayVersion}")
    implementation("com.oracle.ojdbc:ojdbc8:${Versions.ojdbc8Version}")

    implementation("io.micrometer:micrometer-registry-prometheus:1.12.4")
    implementation("net.logstash.logback:logstash-logback-encoder:4.10")
    implementation("commons-io:commons-io:2.15.1")
    implementation("com.lowagie:itext:2.1.7")
    implementation("com.googlecode.owasp-java-html-sanitizer:owasp-java-html-sanitizer:20220608.1")
    implementation("javax.inject:javax.inject:1")
    implementation("org.apache.pdfbox:pdfbox:2.0.25")
    implementation("org.apache.pdfbox:pdfbox-tools:2.0.25")
    implementation("org.xhtmlrenderer:flying-saucer-pdf:9.7.1")
    implementation("org.apache.commons:commons-lang3")
    implementation("org.slf4j:slf4j-api")
    implementation("net.sf.saxon:Saxon-HE:9.7.0-8")
    implementation("org.apache.kafka:kafka_2.13") {
        exclude(group = "log4j", module = "log4j")
    }
    implementation("com.atomikos:transactions-spring-boot3-starter:${Versions.atomikosVersion}")
    implementation("jakarta.ws.rs:jakarta.ws.rs-api:${Versions.jakartaRsApiVersion}")
    implementation("com.sun.activation:javax.activation:${Versions.javaxActivationVersion}")
    implementation("org.apache.httpcomponents.client5:httpclient5:${Versions.apacheHttpClientVersion}")
    implementation("javax.xml.ws:jaxws-api:${Versions.javaxWsVersion}")
    implementation("com.sun.xml.messaging.saaj:saaj-impl:${Versions.jakartaSoapVersion}")
    implementation("javax.xml.soap:saaj-api:${Versions.javaxSoapVersion}")

    testImplementation("junit:junit")
    testImplementation("io.mockk:mockk:${Versions.mockkVersion}")
    testImplementation("com.h2database:h2:${Versions.h2Version}")
    testImplementation("no.nav.security:token-validation-spring-test:${Versions.tokenSupportVersion}")
    testImplementation("org.springframework.boot:spring-boot-starter-test")

    constraints {
        implementation("org.apache.zookeeper:zookeeper") {
            because("CVE-2023-44981")
            version {
                require("3.8.3")
            }
        }
        implementation("commons-collections:commons-collections") {
            because("CVE-2015-7501")
            version {
                require("3.2.2")
            }
        }
    }
}

tasks {
    extra["log4j2.version"] = "2.16.0"

    named<org.springframework.boot.gradle.tasks.bundling.BootJar>("bootJar") {
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
        archiveFileName.set("app.jar")
    }
    named<Jar>("jar") {
        enabled = false
    }
}
