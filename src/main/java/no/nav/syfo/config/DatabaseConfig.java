package no.nav.syfo.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import no.nav.syfo.repository.dao.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;

import static java.lang.System.getProperty;

@Configuration
@EnableTransactionManagement
public class DatabaseConfig {

    public static final String SERVICEOPPFOELGINGSDIALOGDB_URL = "SERVICEOPPFOELGINGSDIALOGDB_URL";
    public static final String SERVICEOPPFOELGINGSDIALOGDB_USERNAME = "SERVICEOPPFOELGINGSDIALOGDB_USERNAME";
    public static final String SERVICEOPPFOELGINGSDIALOGDB_PASSWORD = "SERVICEOPPFOELGINGSDIALOGDB_PASSWORD";

    @Bean
    public static DataSource dataSource() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(getProperty(SERVICEOPPFOELGINGSDIALOGDB_URL));
        config.setUsername(getProperty(SERVICEOPPFOELGINGSDIALOGDB_USERNAME));
        config.setPassword(getProperty(SERVICEOPPFOELGINGSDIALOGDB_PASSWORD));
        config.setMaximumPoolSize(10);
        config.setMinimumIdle(2);
        return new HikariDataSource(config);
    }

    @Bean(name = "transactionManager")
    public PlatformTransactionManager transactionManager(DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

    @Bean
    public JdbcTemplate jdbcTemplate(DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    @Bean
    public NamedParameterJdbcTemplate namedParameterJdbcTemplate() {
        return new NamedParameterJdbcTemplate(dataSource());
    }

    @Bean
    public GodkjentplanDAO godkjentplanDAO() {
        return new GodkjentplanDAO();
    }

    @Bean
    public ArbeidsoppgaveDAO arbeidsoppgaveDAO() {
        return new ArbeidsoppgaveDAO();
    }

    @Bean
    public TiltakDAO tiltakDAO() {
        return new TiltakDAO();
    }

    @Bean
    public KommentarDAO kommentarDAO() {
        return new KommentarDAO();
    }

    @Bean
    public OppfoelingsdialogDAO oppfoelingsdialogDAO() {
        return new OppfoelingsdialogDAO();
    }

    @Bean
    public DokumentDAO dokumentDAO() {
        return new DokumentDAO();
    }

    @Bean
    public GodkjenningerDAO godkjenningerDAO() {
        return new GodkjenningerDAO();
    }

    @Bean
    public AsynkOppgaveDAO asynkOppgaveDAO() {
        return new AsynkOppgaveDAO();
    }

    @Bean
    public VeilederBehandlingDAO veilederBehandlingDAO() {
        return new VeilederBehandlingDAO();
    }
}
