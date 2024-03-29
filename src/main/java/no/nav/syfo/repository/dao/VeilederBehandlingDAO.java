package no.nav.syfo.repository.dao;

import no.nav.syfo.domain.VeilederBehandling;
import no.nav.syfo.repository.domain.PVeilederBehandling;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.inject.Inject;
import java.sql.ResultSet;
import java.sql.SQLException;

import static java.util.UUID.randomUUID;
import static no.nav.syfo.repository.DbUtil.convert;
import static no.nav.syfo.repository.DbUtil.nesteSekvensverdi;
import static no.nav.syfo.repository.domain.VeilederBehandlingStatus.IKKE_LEST;

@Repository
public class VeilederBehandlingDAO {

    private JdbcTemplate jdbcTemplate;
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Inject
    public VeilederBehandlingDAO(
            JdbcTemplate jdbcTemplate,
            NamedParameterJdbcTemplate namedParameterJdbcTemplate
    ) {
        this.jdbcTemplate = jdbcTemplate;
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
    }

    public VeilederBehandling opprett(VeilederBehandling veilederBehandling) {
        long id = nesteSekvensverdi("VEILEDER_BEHANDLING_ID_SEQ", jdbcTemplate);
        MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                .addValue("oppgave_id", id)
                .addValue("oppgave_uuid", randomUUID().toString())
                .addValue("godkjentplan_id", veilederBehandling.godkjentplanId())
                .addValue("tildelt_enhet", veilederBehandling.tildeltEnhet())
                .addValue("tildelt_ident", null)
                .addValue("opprettet_dato", convert(veilederBehandling.opprettetDato()))
                .addValue("sist_endret", convert(veilederBehandling.sistEndret()))
                .addValue("sist_endret_av", null)
                .addValue("status", IKKE_LEST.name());
        namedParameterJdbcTemplate.update("INSERT INTO veileder_behandling " +
                "(oppgave_id, oppgave_uuid, godkjentplan_id, tildelt_enhet, tildelt_ident, " +
                "opprettet_dato, sist_endret, sist_endret_av, status) " +
                "VALUES(:oppgave_id, :oppgave_uuid, :godkjentplan_id, :tildelt_enhet, :tildelt_ident, " +
                ":opprettet_dato, :sist_endret, :sist_endret_av, :status)", namedParameters);

        return veilederBehandling.oppgaveId(id);
    }

    private class VeilederBehandlingRowMapper implements RowMapper<PVeilederBehandling> {
        @Override
        public PVeilederBehandling mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new PVeilederBehandling()
                    .oppgaveId(rs.getLong("oppgave_id"))
                    .oppgaveUUID(rs.getString("oppgave_uuid"))
                    .godkjentplanId(rs.getLong("godkjentplan_id"))
                    .tildeltEnhet(rs.getString("tildelt_enhet"))
                    .tildeltIdent(rs.getString("tildelt_ident"))
                    .opprettetDato(convert(rs.getTimestamp("opprettet_dato")))
                    .sistEndret(convert(rs.getTimestamp("sist_endret")))
                    .sistEndretAv(rs.getString("sist_endret_av"))
                    .status(rs.getString("status"));
        }
    }

}
