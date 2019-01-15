package no.nav.syfo.repository.dao;

import no.nav.syfo.domain.VeilederBehandling;
import no.nav.syfo.repository.domain.PVeilederBehandling;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import javax.inject.Inject;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import static java.util.UUID.randomUUID;
import static no.nav.syfo.util.MapUtil.mapListe;
import static no.nav.syfo.mappers.persistency.POppfoelgingsdialogMapper.p2veilederbehandling;
import static no.nav.syfo.repository.DbUtil.convert;
import static no.nav.syfo.repository.DbUtil.nesteSekvensverdi;
import static no.nav.syfo.repository.domain.VeilederBehandlingStatus.IKKE_LEST;

public class VeilederBehandlingDAO {

    @Inject
    private JdbcTemplate jdbcTemplate;
    @Inject
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

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

    public List<VeilederBehandling> hentVeilederBehandlingByEnhetId(String enhetId) {
        return mapListe(jdbcTemplate.query("SELECT * FROM veileder_behandling WHERE tildelt_enhet = ?", new VeilederBehandlingRowMapper(), enhetId), p2veilederbehandling);
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
