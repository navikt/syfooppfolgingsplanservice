package no.nav.syfo.repository.dao;

import no.nav.syfo.domain.FeiletSending;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import no.nav.syfo.repository.domain.PFeiletSending;

import javax.inject.Inject;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import static no.nav.syfo.mappers.persistency.POppfoelgingsdialogMapper.p2feiletSending;
import static java.time.LocalDateTime.now;
import static no.nav.syfo.repository.DbUtil.*;
import static no.nav.syfo.repository.DbUtil.convert;
import static no.nav.syfo.util.MapUtil.mapListe;

@Transactional
@Repository
public class FeiletSendingDAO {

    private JdbcTemplate jdbcTemplate;
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Inject
    public FeiletSendingDAO(
            JdbcTemplate jdbcTemplate,
            NamedParameterJdbcTemplate namedParameterJdbcTemplate
    ) {
        this.jdbcTemplate = jdbcTemplate;
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
    }

    public List<FeiletSending> hentFeiledeSendinger() {
        return mapListe(jdbcTemplate.query("select * from feilet_sending where max_retries > number_of_tries", new FeiletSendingRowMapper()), p2feiletSending);
    }

    public void create(FeiletSending feiletSending) {
        long feiletSendingId = nesteSekvensverdi("FEILET_SENDING_ID_SEQ", jdbcTemplate);
        MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                .addValue("id", feiletSendingId)
                .addValue("oppfolgingsplanlps_id", feiletSending.oppfolgingsplanId)
                .addValue("number_of_tries", feiletSending.number_of_tries)
                .addValue("max_retries", feiletSending.max_retries)
                .addValue("opprettetDato", convert(now()))
                .addValue("sistEndretDato", convert(now()));

        namedParameterJdbcTemplate.update("insert into feilet_sending " +
                "(id, oppfolgingsplanlps_id, aktoer_id, number_of_tries, max_retries, opprettetDato, sistEndretDato) values" +
                "(:id, :oppfolgingsplanlps_id, :number_of_tries, :max_retries, :opprettetDato, :sistEndretDato)", namedParameters);
    }

    public void update(FeiletSending feiletSending) {
        MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                .addValue("oppfolgingsplanlps_id", feiletSending.oppfolgingsplanId)
                .addValue("number_of_tries", feiletSending.number_of_tries)
                .addValue("sistEndretDato", convert(now()));

        namedParameterJdbcTemplate.update("update feilet_sending " +
                "set number_of_tries=:number_of_tries, sistEndretDato=:sistEndretDato" +
                "where oppfolgingsplanlps_id=:oppfolgingsplanlps_id", namedParameters);
    }

    public void remove(Long oppfolgingsplanId) {
        MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                .addValue("oppfolgingsplanlps_id", oppfolgingsplanId);

        namedParameterJdbcTemplate.update("delete from feilet_sending " +
                "where oppfolgingsplanlps_id=:oppfolgingsplanlps_id", namedParameters);
    }

    private class FeiletSendingRowMapper implements RowMapper<PFeiletSending> {
        public PFeiletSending mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new PFeiletSending()
                    .id(rs.getLong("id"))
                    .oppfolgingsplanId(rs.getLong("oppfoelgingsplanlps_id"))
                    .number_of_tries(rs.getInt("number_of_tries"))
                    .max_retries(rs.getInt("max_retries"))
                    .opprettetDato(convert(rs.getTimestamp("opprettetDato")))
                    .sistEndretDato(convert(rs.getTimestamp("sistEndretDato")));
        }
    }
}
