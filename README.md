# Syfooppfolgingsplanservice
Backend applikasjon som lagrer, tilbyr og videresender NAV's versjon
av digitale oppfølgingsplaner.


## Lokal utvikling
Start opp via `LocalApplication.main`. Kjører på port 8583.


### Pipeline

Pipeline er på Github Action.
Commits til main-branch deployes automatisk til dev-fss og prod-fss.
Commits til en ikke-main-branch deployes dev-fss.


## Database
Appen kjører med en lokal H2 in-memory database. Den spinnes opp som en del av applikasjonen og er 
også tilgjengelig i tester. Du kan logge inn og kjøre spørringer på:
`localhost/h2` med jdbc_url: `jdbc:h2:mem:testdb`

## Redis Cache
syfooppfolgingsplanservice bruker redis for cache.
Redis pod må startes manuelt ved å kjøre følgdende kommando: `kubectl apply -f redis-config.yaml`.

## Alerterator
Syfooppfolgingsplanservice er satt opp med alerterator, slik når appen er nede vil det sendes en varsling til Slack kanalene #veden-alerts.
Spec'en for alerts ligger i filen alerts.yaml. Hvis man ønsker å forandre på hvilke varsler som skal sendes må man forandre
på alerts.yaml og deretter kjøre:
`kubectl apply -f alerts.yaml`.
For å se status på syfooppfolgingsplanservice alerts kan man kjøre:
`kubectl describe alert syfooppfolgingsplanservice-alerts`.
Dokumentasjon for Alerterator ligger her: https://doc.nais.io/observability/alerts

## Hente pakker fra Github Package Registry
Noen pakker hentes fra Github Package Registry som krever autentisering.
Pakkene kan lastes ned via build.gradle slik:
```
val githubUser: String by project
val githubPassword: String by project
repositories {
    maven {
        url = uri("https://maven.pkg.github.com/navikt/tjenestespesifikasjoner")
        credentials {
            username = githubUser
            password = githubPassword
        }
    }
}
```

`githubUser` og `githubPassword` settes i `~/.gradle/gradle.properties`:

```
githubUser=x-access-token
githubPassword=<token>
```

Hvor `<token>` er et personal access token med scope `read:packages`(og SSO enabled).

Evt. kan variablene kan også konfigureres som miljøvariabler eller brukes i kommandolinjen:

* `ORG_GRADLE_PROJECT_githubUser`
* `ORG_GRADLE_PROJECT_githubPassword`

```
./gradlew -PgithubUser=x-access-token -PgithubPassword=[token]
```
