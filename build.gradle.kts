import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import com.github.jengelman.gradle.plugins.shadow.transformers.PropertiesFileTransformer
import com.github.jengelman.gradle.plugins.shadow.transformers.ServiceFileTransformer
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

group = "no.nav.syfo"
version = "1.0.0"

object Versions {
    const val avroVersion = "1.8.2"
    const val confluentVersion = "4.0.0"
    const val cxfVersion = "3.3.7"
    const val flywayVersion = "5.1.4"
    const val kotlinJacksonVersion = "2.9.8"
    const val tokenSupportVersion = "1.3.5"
    const val ojdbc8Version = "19.3.0.0"
    const val helseXmlVersion = "1.5d21db9"
    const val syfoOppfolgingsplanSchemaVersion = "1.0.2"
    const val syfotjenesterVersion = "1.2020.07.02-07.44-62078cd74f7e"
    const val tjenesteSpesifikasjonerVersion = "1.2020.06.23-15.31-57b909d0a05c"
    const val kafkaVersion = "2.0.0"
    const val altinnKanalSchemasVersion = "1.0.1"
    const val jaxwsVersion = "2.3.2"
    const val h2Version ="2.1.210"
}

plugins {
    kotlin("jvm") version "1.3.72"
    id("org.jetbrains.kotlin.plugin.allopen") version "1.3.50"
    id("com.github.johnrengelman.shadow") version "5.2.0"
    id("java")
    id("org.springframework.boot") version "2.2.8.RELEASE"
    id("io.spring.dependency-management") version "1.0.9.RELEASE"
    id("com.github.ManifestClasspath") version "0.1.0-RELEASE"
}

buildscript {
    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.3.0")
        classpath("javax.xml.bind:jaxb-api:2.4.0-b180830.0359")
        classpath("org.glassfish.jaxb:jaxb-runtime:2.4.0-b180830.0438")
        classpath("com.sun.activation:javax.activation:1.2.0")
        classpath("com.sun.xml.ws:jaxws-tools:2.3.1") {
            exclude(group = "com.sun.xml.ws", module = "policy")
        }
    }
}

allOpen {
    annotation("org.springframework.context.annotation.Configuration")
    annotation("org.springframework.stereotype.Service")
    annotation("org.springframework.stereotype.Component")
}

val githubUser: String by project
val githubPassword: String by project
repositories {
    mavenCentral()
    maven(url="http://packages.confluent.io/maven/")
    maven {
        url = uri("https://maven.pkg.github.com/navikt/syfo-xml-codegen")
        credentials {
            username = githubUser
            password = githubPassword
        }
    }
    maven {
        url = uri("https://maven.pkg.github.com/navikt/syfoopservice-schema")
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

dependencies {
    implementation(kotlin("stdlib"))
    implementation(kotlin("reflect"))
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:${Versions.kotlinJacksonVersion}")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-xml:${Versions.kotlinJacksonVersion}")

    implementation("com.sun.xml.ws:jaxws-ri:${Versions.jaxwsVersion}")

    implementation("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")

    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-jersey")
    implementation("org.springframework.boot:spring-boot-starter-logging")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-jta-atomikos")
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    implementation("org.springframework.kafka:spring-kafka")
    testImplementation("org.springframework.boot:spring-boot-starter-test")

    implementation("org.springframework:spring-jms")

    implementation("org.apache.httpcomponents:httpclient:4.5.6")

    implementation("no.nav.security:token-validation-spring:${Versions.tokenSupportVersion}")
    testImplementation("no.nav.security:token-validation-test-support:${Versions.tokenSupportVersion}")

    implementation("org.apache.cxf:cxf-rt-features-logging:${Versions.cxfVersion}")
    implementation("org.apache.cxf:cxf-rt-ws-security:${Versions.cxfVersion}")
    implementation("org.apache.cxf:cxf-rt-ws-policy:${Versions.cxfVersion}")
    implementation("org.apache.cxf:cxf-rt-transports-http:${Versions.cxfVersion}")
    implementation("org.apache.cxf:cxf-rt-frontend-jaxws:${Versions.cxfVersion}")

    implementation("com.ibm.mq:com.ibm.mq.allclient:9.0.5.0")

    implementation("no.nav.helse.xml:oppfolgingsplan:${Versions.helseXmlVersion}")

    implementation("no.nav.syfo.oppfolgingsplan.avro:syfoopservice-schema:${Versions.syfoOppfolgingsplanSchemaVersion}")
    implementation("no.nav.syfotjenester:oppfolgingsplanlps:${Versions.syfotjenesterVersion}")
    implementation("no.nav.altinnkanal.avro:altinnkanal-schemas:${Versions.altinnKanalSchemasVersion}")

    implementation("no.nav.tjenestespesifikasjoner:altinn-correspondence-agency-external:${Versions.tjenesteSpesifikasjonerVersion}")
    implementation("no.nav.tjenestespesifikasjoner:altinn-correspondence-agency-external-basic:${Versions.tjenesteSpesifikasjonerVersion}")
    implementation("no.nav.tjenestespesifikasjoner:servicemeldingMedKontaktinformasjon-v1-tjenestespesifikasjon:${Versions.tjenesteSpesifikasjonerVersion}")
    implementation("no.nav.tjenestespesifikasjoner:varsel-inn:${Versions.tjenesteSpesifikasjonerVersion}")

    implementation("org.apache.avro:avro:${Versions.avroVersion}")
    implementation("io.confluent:kafka-avro-serializer:${Versions.confluentVersion}"){
        exclude(group = "log4j", module = "log4j")
    }

    implementation("org.flywaydb:flyway-core:${Versions.flywayVersion}")
    implementation("com.oracle.ojdbc:ojdbc8:${Versions.ojdbc8Version}")
    testImplementation("com.h2database:h2:${Versions.h2Version}")

    implementation("io.micrometer:micrometer-registry-prometheus:1.0.6")
    implementation("net.logstash.logback:logstash-logback-encoder:4.10")
    implementation("commons-io:commons-io:2.5")
    implementation("com.lowagie:itext:2.1.7")
    implementation("com.googlecode.owasp-java-html-sanitizer:owasp-java-html-sanitizer:20171016.1")
    implementation("javax.ws.rs:javax.ws.rs-api:2.0.1")
    implementation("javax.inject:javax.inject:1")
    implementation("org.apache.pdfbox:pdfbox:2.0.18")
    implementation("org.apache.pdfbox:pdfbox-tools:2.0.18")
    implementation("org.xhtmlrenderer:flying-saucer-pdf:9.1.20")
    implementation("org.apache.commons:commons-lang3:3.5")
    implementation("org.slf4j:slf4j-api:1.7.25")
    implementation("net.sf.saxon:Saxon-HE:9.7.0-8")
    implementation("org.apache.kafka:kafka_2.12:${Versions.kafkaVersion}"){
        exclude(group = "log4j", module = "log4j")
    }
}

tasks {
    extra["log4j2.version"] = "2.16.0"

    shadowJar {
        isZip64 = true
    }

    withType<Jar> {
        manifest.attributes["Main-Class"] = "no.nav.syfo.Application"
    }

    create("printVersion") {
        doLast {
            println(project.version)
        }
    }

    withType<ShadowJar> {
        transform(ServiceFileTransformer::class.java) {
            setPath("META-INF/cxf")
            include("bus-extensions.txt")
        }
        transform(PropertiesFileTransformer::class.java) {
            paths = listOf("META-INF/spring.factories")
            mergeStrategy = "append"
        }
        mergeServiceFiles()
    }

    named<KotlinCompile>("compileKotlin") {
        kotlinOptions.jvmTarget = "11"
    }

    named<KotlinCompile>("compileTestKotlin") {
        kotlinOptions.jvmTarget = "11"
    }
}
