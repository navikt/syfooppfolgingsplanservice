
# Syfooppfolgingsplanservice
Tidligere serviceoppfoelgingsdialog, håndterer digitale oppfølgigngsplaner

## Lokal utvikling
Start opp via `LocalApplication.main`. Kjører på port 8583.


## Veien til prod
Bygg og Pipeline jobber ligger i jenkins: https://jenkins-digisyfo.adeo.no/job/digisyfo/job/syfooppfolgingsplanservice/


## Database
Appen kjører med en lokal H2 in-memory database. Den spinnes opp som en del av applikasjonen og er 
også tilgjengelig i tester. Du kan logge inn og kjøre spørringer på:
`localhost/h2` med jdbc_url: `jdbc:h2:mem:testdb`

##Redis Cache
syfooppfolgingsplanservice bruker redis for cache.
Redis pod må startes manuelt ved å kjøre følgdende kommando: `kubectl apply -f redis-config.yaml`.