import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import com.github.jengelman.gradle.plugins.shadow.transformers.PropertiesFileTransformer
import com.github.jengelman.gradle.plugins.shadow.transformers.ServiceFileTransformer
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

group = "no.nav.syfo"
version = "1.0.0"

val cxfVersion = "3.2.7"
val oidcSpringSupportVersion = "0.2.4"
val springBootVersion = "2.0.4.RELEASE"
val kotlinLibVersion = "1.3.50"
val kotlinJacksonVersion = "2.9.8"

plugins {
    kotlin("jvm") version "1.3.50"
    id("org.jetbrains.kotlin.plugin.allopen") version "1.3.50"
    id("com.github.johnrengelman.shadow") version "4.0.3"
    id("java")
}

buildscript {
    dependencies {
        classpath("org.springframework.boot:spring-boot-gradle-plugin:2.0.4.RELEASE")
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
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:$kotlinLibVersion")
    implementation("org.jetbrains.kotlin:kotlin-reflect:$kotlinLibVersion")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:$kotlinJacksonVersion")

    implementation("org.projectlombok:lombok:1.16.20")
    annotationProcessor("org.projectlombok:lombok:1.16.20")
    implementation("no.nav.syfo.tjeneste:altinn-correspondence-agency-external-sf-tjenestespesifikasjon:1.1")
    implementation("no.nav.tjenestespesifikasjoner:nav-arbeidsforhold-v3-tjenestespesifikasjon:1.2019.03.05-14.13-d95264192bc7")
    implementation("no.nav.syfo.tjenester:behandleJournal-v2:1.0.2")
    implementation("no.nav.sbl.dialogarena:arbeidsfordeling-v1-tjenestespesifikasjon:1.1.0")
    implementation("no.nav.sbl:brukerprofil-v3-tjenestespesifikasjon:3.0.3")
    implementation("no.nav.syfo.tjenester:dkif-tjenestespesifikasjon:1.2")
    implementation("no.nav.syfo.tjenester:egenAnsatt-v1-tjenestespesifikasjon:1.0.1")
    implementation("no.nav.sbl.dialogarena:organisasjonv4-tjenestespesifikasjon:1.0.1")
    implementation("no.nav.sbl.dialogarena:organisasjonenhet-v2-tjenestespesifikasjon:1.0.6")
    implementation("no.nav.sbl.dialogarena:person-v3-tjenestespesifikasjon:3.0.2")
    implementation("no.nav.syfo.tjenester:sykefravaersoppfoelgingv1-tjenestespesifikasjon:1.0.20")
    implementation("no.nav.sbl.dialogarena:dial-nav-tjeneste-aktoer_v2:1.2")
    implementation("no.nav.syfo.tjenester:servicemeldingMedKontaktinformasjon-v1:1.0.0")
    implementation("no.nav.syfo.tjenester:behandleSak-v1:1.0.2")
    implementation("no.nav.syfo.tjenester:sak-v1:1.0.0")
    implementation("no.nav.sbl.dialogarena:varsel-inn:1.0.5")

    implementation("org.apache.cxf:cxf-rt-features-logging:$cxfVersion")
    implementation("org.apache.cxf:cxf-rt-ws-security:$cxfVersion")
    implementation("org.apache.cxf:cxf-rt-ws-policy:$cxfVersion")
    implementation("org.apache.cxf:cxf-rt-transports-http:$cxfVersion")
    implementation("org.apache.cxf:cxf-rt-frontend-jaxws:$cxfVersion")

    implementation("org.springframework.boot:spring-boot-starter-web:$springBootVersion")
    implementation("org.springframework.boot:spring-boot-starter-actuator:$springBootVersion")
    implementation("org.springframework.boot:spring-boot-starter-jersey:$springBootVersion")
    implementation("org.springframework.boot:spring-boot-starter-logging:$springBootVersion")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa:$springBootVersion")
    implementation("org.springframework.boot:spring-boot-starter-jta-atomikos:$springBootVersion")
    implementation("org.springframework.boot:spring-boot-starter-data-redis:$springBootVersion")
    implementation("io.micrometer:micrometer-registry-prometheus:1.0.6")
    implementation("org.springframework:spring-jms:5.0.7.RELEASE")
    implementation("org.springframework:spring-context:5.0.7.RELEASE")
    implementation("no.nav.security:oidc-support:0.2.4")
    implementation("no.nav.security:oidc-spring-support:0.2.4")
    implementation("com.ibm.mq:com.ibm.mq.allclient:9.0.4.0")
    implementation("org.flywaydb:flyway-core:4.0.3")
    implementation("net.logstash.logback:logstash-logback-encoder:4.10")
    implementation("commons-io:commons-io:2.5")
    implementation("com.lowagie:itext:2.1.7")
    implementation("com.googlecode.owasp-java-html-sanitizer:owasp-java-html-sanitizer:20171016.1")
    implementation("javax.ws.rs:javax.ws.rs-api:2.0.1")
    implementation("org.apache.pdfbox:pdfbox:2.0.18")
    implementation("org.apache.pdfbox:pdfbox-tools:2.0.18")
    implementation("org.xhtmlrenderer:flying-saucer-pdf:9.0.4")
    implementation("javax.inject:javax.inject:1")
    implementation("org.apache.commons:commons-lang3:3.5")
    implementation("org.slf4j:slf4j-api:1.7.25")
    implementation("net.sf.saxon:Saxon-HE:9.7.0-8")
    runtime("com.oracle:ojdbc6:11.2.0.3")
    testImplementation("org.springframework.boot:spring-boot-starter-test:$springBootVersion")
    testImplementation("no.nav.security:oidc-spring-test:0.2.4")
    testImplementation("com.h2database:h2:1.4.197")
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
