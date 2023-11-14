import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import com.github.jengelman.gradle.plugins.shadow.transformers.PropertiesFileTransformer
import com.github.jengelman.gradle.plugins.shadow.transformers.ServiceFileTransformer
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

group = "no.nav.syfo"
version = "1.0.0"

object Versions {
    const val avroVersion = "1.11.0"
    const val confluentVersion = "7.1.0"
    const val cxfVersion = "3.5.0"
    const val flywayVersion = "9.10.0"
    const val tokenSupportVersion = "2.1.3"
    const val tokenTestSupportVersion = "2.0.5"
    const val ojdbc8Version = "19.3.0.0"
    const val helseXmlVersion = "1.0.4"
    const val syfotjenesterVersion = "1.2020.07.02-07.44-62078cd74f7e"
    const val tjenesteSpesifikasjonerVersion = "1.2020.06.23-15.31-57b909d0a05c"
    const val altinnKanalSchemasVersion = "2.0.0"
    const val jaxwsVersion = "2.3.2"
    const val h2Version = "2.1.210"
    const val mockkVersion = "1.13.4"
}

val githubUser: String by project
val githubPassword: String by project

plugins {
    kotlin("jvm") version "1.6.10"
    id("org.jetbrains.kotlin.plugin.allopen") version "1.6.10"
    id("com.github.johnrengelman.shadow") version "7.1.2"
    id("java")
    id("org.springframework.boot") version "2.7.11"
    id("io.spring.dependency-management") version "1.0.11.RELEASE"
    id("com.github.ManifestClasspath") version "0.1.0-RELEASE"
}

buildscript {
    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.6.10")
        classpath("javax.xml.bind:jaxb-api:2.4.0-b180830.0359")
        classpath("org.glassfish.jaxb:jaxb-runtime:2.4.0-b180830.0438")
        classpath("com.sun.activation:javax.activation:1.2.0")
        classpath("com.sun.xml.ws:jaxws-tools:2.3.1") {
            exclude(group = "com.sun.xml.ws", module = "policy")
        }
    }
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
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
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-xml")

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

    implementation("org.apache.httpcomponents:httpclient")

    implementation("no.nav.security:token-validation-spring:${Versions.tokenSupportVersion}")
    testImplementation("no.nav.security:token-validation-test-support:${Versions.tokenTestSupportVersion}")

    implementation("org.apache.cxf:cxf-rt-features-logging:${Versions.cxfVersion}")
    implementation("org.apache.cxf:cxf-rt-ws-security:${Versions.cxfVersion}")
    implementation("org.apache.cxf:cxf-rt-ws-policy:${Versions.cxfVersion}")
    implementation("org.apache.cxf:cxf-rt-transports-http:${Versions.cxfVersion}")
    implementation("org.apache.cxf:cxf-rt-frontend-jaxws:${Versions.cxfVersion}")

    implementation("com.ibm.mq:com.ibm.mq.allclient:9.2.5.0")

    implementation("no.nav.helse.xml:oppfolgingsplan:${Versions.helseXmlVersion}")

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

    implementation("io.micrometer:micrometer-registry-prometheus:1.10.5")
    implementation("net.logstash.logback:logstash-logback-encoder:4.10")
    implementation("commons-io:commons-io:2.5")
    implementation("com.lowagie:itext:2.1.7")
    implementation("com.googlecode.owasp-java-html-sanitizer:owasp-java-html-sanitizer:20220608.1")
    implementation("javax.ws.rs:javax.ws.rs-api:2.0.1")
    implementation("javax.inject:javax.inject:1")
    implementation("org.apache.pdfbox:pdfbox:2.0.25")
    implementation("org.apache.pdfbox:pdfbox-tools:2.0.25")
    implementation("org.xhtmlrenderer:flying-saucer-pdf:9.1.22")
    implementation("org.apache.commons:commons-lang3")
    implementation("org.slf4j:slf4j-api")
    implementation("net.sf.saxon:Saxon-HE:9.7.0-8")
    implementation("org.apache.kafka:kafka_2.13"){
        exclude(group = "log4j", module = "log4j")
    }

    testImplementation("junit:junit")
    testImplementation("io.mockk:mockk:${Versions.mockkVersion}")
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
        configureEach {
            append("META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports")
            append("META-INF/spring/org.springframework.boot.actuate.autoconfigure.web.ManagementContextConfiguration.imports")
        }
        mergeServiceFiles()
    }

    named<KotlinCompile>("compileKotlin") {
        kotlinOptions.jvmTarget = "17"
    }

    named<KotlinCompile>("compileTestKotlin") {
        kotlinOptions.jvmTarget = "17"
    }

}
