export SPRING_DATASOURCE_URL=$(cat /secrets/oppfolgingsplandb/config/jdbc_url)
export SPRING_DATASOURCE_USERNAME=$(cat /secrets/oppfolgingsplandb/credentials/username)
export SPRING_DATASOURCE_PASSWORD=$(cat /secrets/oppfolgingsplandb/credentials/password)
