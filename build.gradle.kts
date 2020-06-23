import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import com.github.jengelman.gradle.plugins.shadow.transformers.PropertiesFileTransformer
import com.github.jengelman.gradle.plugins.shadow.transformers.ServiceFileTransformer
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

group = "no.nav.syfo"
version = "1.0.0"

val cxfVersion = "3.3.3"
val oidcSupportVersion = "0.2.18"
val oidcSupportTestVersion = "0.2.4"
val kotlinLibVersion = "1.3.50"
val kotlinJacksonVersion = "2.9.8"
val lombokVersion = "1.16.20"
val tjenesteSpesifikasjonerVersion = "1.2019.09.25-00.21-49b69f0625e0"

val flywayVersion = "5.1.4"
val ojdbc8Version = "19.3.0.0"

plugins {
    kotlin("jvm") version "1.3.72"
    id("org.jetbrains.kotlin.plugin.allopen") version "1.3.50"
    id("com.github.johnrengelman.shadow") version "4.0.3"
    id("java")
    id("org.springframework.boot") version "2.1.8.RELEASE"
    id("io.spring.dependency-management") version "1.0.9.RELEASE"
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

repositories {
    mavenCentral()
    jcenter()
    maven(url = "https://repo.adeo.no/repository/maven-releases/")
    maven(url = "https://dl.bintray.com/kotlin/kotlinx/")
    maven(url = "http://packages.confluent.io/maven/")
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation(kotlin("reflect"))
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:$kotlinJacksonVersion")

    implementation("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")

    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-jersey")
    implementation("org.springframework.boot:spring-boot-starter-logging")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-jta-atomikos")
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    testImplementation("org.springframework.boot:spring-boot-starter-test")

    implementation("org.springframework:spring-jms")

    implementation("org.apache.httpcomponents:httpclient:4.5.6")

    implementation("no.nav.security:oidc-spring-support:$oidcSupportVersion")
    testImplementation("no.nav.security:oidc-test-support:$oidcSupportVersion")

    implementation("org.apache.cxf:cxf-rt-features-logging:$cxfVersion")
    implementation("org.apache.cxf:cxf-rt-ws-security:$cxfVersion")
    implementation("org.apache.cxf:cxf-rt-ws-policy:$cxfVersion")
    implementation("org.apache.cxf:cxf-rt-transports-http:$cxfVersion")
    implementation("org.apache.cxf:cxf-rt-frontend-jaxws:$cxfVersion")

    implementation("no.nav.syfo.tjeneste:altinn-correspondence-agency-external-sf-tjenestespesifikasjon:1.1")
    implementation("no.nav.syfo.tjenester:sykefravaersoppfoelgingv1-tjenestespesifikasjon:1.0.20")

    implementation("com.ibm.mq:com.ibm.mq.allclient:9.0.4.0")
    implementation("no.nav.syfo.tjenester:servicemeldingMedKontaktinformasjon-v1:1.0.0")
    implementation("no.nav.tjenestespesifikasjoner:varsel-inn:$tjenesteSpesifikasjonerVersion")

    implementation("org.flywaydb:flyway-core:$flywayVersion")
    implementation("com.oracle.ojdbc:ojdbc8:$ojdbc8Version")
    testImplementation("com.h2database:h2")

    implementation("io.micrometer:micrometer-registry-prometheus:1.0.6")
    implementation("net.logstash.logback:logstash-logback-encoder:4.10")
    implementation("commons-io:commons-io:2.5")
    implementation("com.lowagie:itext:2.1.7")
    implementation("com.googlecode.owasp-java-html-sanitizer:owasp-java-html-sanitizer:20171016.1")
    implementation("javax.ws.rs:javax.ws.rs-api:2.0.1")
    implementation("javax.inject:javax.inject:1")
    implementation("org.apache.pdfbox:pdfbox:2.0.18")
    implementation("org.apache.pdfbox:pdfbox-tools:2.0.18")
    implementation("org.xhtmlrenderer:flying-saucer-pdf:9.0.4")
    implementation("org.apache.commons:commons-lang3:3.5")
    implementation("org.slf4j:slf4j-api:1.7.25")
    implementation("net.sf.saxon:Saxon-HE:9.7.0-8")
}

tasks {
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
        kotlinOptions.jvmTarget = "1.8"
    }

    named<KotlinCompile>("compileTestKotlin") {
        kotlinOptions.jvmTarget = "1.8"
    }
}
