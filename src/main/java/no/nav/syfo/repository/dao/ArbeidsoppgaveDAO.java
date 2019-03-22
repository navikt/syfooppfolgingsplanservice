package no.nav.syfo.repository.dao;

import no.nav.syfo.domain.Arbeidsoppgave;
import no.nav.syfo.repository.domain.PArbeidsoppgave;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.support.SqlLobValue;
import org.springframework.stereotype.Repository;

import javax.inject.Inject;
import java.sql.*;
import java.util.List;

import static java.time.LocalDateTime.now;
import static no.nav.syfo.mappers.persistency.POppfoelgingsdialogMapper.p2arbeidsoppgave;
import static no.nav.syfo.repository.DbUtil.*;
import static no.nav.syfo.util.MapUtil.map;
import static no.nav.syfo.util.MapUtil.mapListe;

@Repository
public class ArbeidsoppgaveDAO {

    private JdbcTemplate jdbcTemplate;
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Inject
    public ArbeidsoppgaveDAO(
            JdbcTemplate jdbcTemplate,
            NamedParameterJdbcTemplate namedParameterJdbcTemplate
    ) {
        this.jdbcTemplate = jdbcTemplate;
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
    }

    public List<Arbeidsoppgave> arbeidsoppgaverByOppfoelgingsdialogId(long oppfoelgingsdialogId) {
        return mapListe(jdbcTemplate.query("select * from arbeidsoppgave where oppfoelgingsdialog_id = ?", new ArbeidsoppgaveRowMapper(), oppfoelgingsdialogId), p2arbeidsoppgave);
    }

    public Arbeidsoppgave finnArbeidsoppgave(long id) {
        return map(jdbcTemplate.queryForObject("select * from arbeidsoppgave where arbeidsoppgave_id = ?", new ArbeidsoppgaveRowMapper(), id), p2arbeidsoppgave);
    }

    public Arbeidsoppgave create(Arbeidsoppgave arbeidsoppgave) {
        long arbeidsoppgaveId = nesteSekvensverdi("ARBEIDSOPPGAVE_ID_SEQ", jdbcTemplate);
        MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                .addValue("arbeidsoppgave_id", arbeidsoppgaveId)
                .addValue("oppfoelgingsdialog_id", arbeidsoppgave.oppfoelgingsdialogId)
                .addValue("navn", sanitizeUserInput(arbeidsoppgave.navn))
                .addValue("er_vurdert_av_sykmeldt", arbeidsoppgave.erVurdertAvSykmeldt)
                .addValue("opprettet_av", arbeidsoppgave.opprettetAvAktoerId)
                .addValue("sist_endret_av", arbeidsoppgave.opprettetAvAktoerId)
                .addValue("sist_endret_dato", convert(now()))
                .addValue("opprettet_dato", convert(now()))
                .addValue("gjennomfoering_status", arbeidsoppgave.gjennomfoering.gjennomfoeringStatus)
                .addValue("paa_annet_sted", arbeidsoppgave.gjennomfoering.paaAnnetSted)
                .addValue("med_mer_tid", arbeidsoppgave.gjennomfoering.medMerTid)
                .addValue("med_hjelp", arbeidsoppgave.gjennomfoering.medHjelp)
                .addValue("beskrivelse", new SqlLobValue(sanitizeUserInput(arbeidsoppgave.gjennomfoering.kanBeskrivelse)), Types.CLOB)
                .addValue("kan_ikke_beskrivelse", new SqlLobValue(sanitizeUserInput(arbeidsoppgave.gjennomfoering.kanIkkeBeskrivelse)), Types.CLOB);
        namedParameterJdbcTemplate.update("insert into arbeidsoppgave (arbeidsoppgave_id, oppfoelgingsdialog_id, navn," +
                "er_vurdert_av_sykmeldt, opprettet_av, sist_endret_av, sist_endret_dato, opprettet_dato, " +
                "paa_annet_sted, med_mer_tid, med_hjelp, beskrivelse, kan_ikke_beskrivelse, gjennomfoering_status) values " +
                "(:arbeidsoppgave_id, :oppfoelgingsdialog_id, :navn, :er_vurdert_av_sykmeldt, :opprettet_av, :sist_endret_av, " +
                ":sist_endret_dato, :opprettet_dato, :paa_annet_sted, " +
                ":med_mer_tid, :med_hjelp, :beskrivelse, :kan_ikke_beskrivelse, :gjennomfoering_status)", namedParameters);
        return arbeidsoppgave.id(arbeidsoppgaveId);
    }

    public Arbeidsoppgave update(Arbeidsoppgave arbeidsoppgave) {
        MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                .addValue("arbeidsoppgave_id", arbeidsoppgave.id)
                .addValue("navn", sanitizeUserInput(arbeidsoppgave.navn))
                .addValue("er_vurdert_av_sykmeldt", arbeidsoppgave.erVurdertAvSykmeldt)
                .addValue("sist_endret_av", arbeidsoppgave.sistEndretAvAktoerId)
                .addValue("sist_endret_dato", convert(now()))
                .addValue("gjennomfoering_status", arbeidsoppgave.gjennomfoering.gjennomfoeringStatus)
                .addValue("paa_annet_sted", arbeidsoppgave.gjennomfoering.paaAnnetSted)
                .addValue("med_mer_tid", arbeidsoppgave.gjennomfoering.medMerTid)
                .addValue("med_hjelp", arbeidsoppgave.gjennomfoering.medHjelp)
                .addValue("beskrivelse", new SqlLobValue(sanitizeUserInput(arbeidsoppgave.gjennomfoering.kanBeskrivelse)), Types.CLOB)
                .addValue("kan_ikke_beskrivelse", new SqlLobValue(sanitizeUserInput(arbeidsoppgave.gjennomfoering.kanIkkeBeskrivelse)), Types.CLOB);
        namedParameterJdbcTemplate.update("update arbeidsoppgave set navn = :navn, er_vurdert_av_sykmeldt = :er_vurdert_av_sykmeldt," +
                "sist_endret_av = :sist_endret_av, sist_endret_dato = :sist_endret_dato, gjennomfoering_status = :gjennomfoering_status, paa_annet_sted = :paa_annet_sted," +
                "med_mer_tid = :med_mer_tid, med_hjelp = :med_hjelp, beskrivelse = :beskrivelse, kan_ikke_beskrivelse = :kan_ikke_beskrivelse " +
                "where arbeidsoppgave_id = :arbeidsoppgave_id", namedParameters);
        return arbeidsoppgave;
    }

    public void delete(Long id) {
        jdbcTemplate.update("delete from arbeidsoppgave where arbeidsoppgave_id = ?", id);
    }

    private class ArbeidsoppgaveRowMapper implements RowMapper<PArbeidsoppgave> {
        public PArbeidsoppgave mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new PArbeidsoppgave()
                    .id(rs.getLong("arbeidsoppgave_id"))
                    .oppfoelgingsdialogId(rs.getLong("oppfoelgingsdialog_id"))
                    .navn(rs.getString("navn"))
                    .erVurdertAvSykmeldt(rs.getBoolean("er_vurdert_av_sykmeldt"))
                    .opprettetAvAktoerId(rs.getString("opprettet_av"))
                    .sistEndretAvAktoerId(rs.getString("sist_endret_av"))
                    .sistEndretDato(convert(rs.getTimestamp("sist_endret_dato")))
                    .opprettetDato(convert(rs.getTimestamp("opprettet_dato")))
                    .gjennomfoeringStatus(rs.getString("gjennomfoering_status"))
                    .paaAnnetSted(rs.getBoolean("paa_annet_sted"))
                    .medMerTid(rs.getBoolean("med_mer_tid"))
                    .medHjelp(rs.getBoolean("med_hjelp"))
                    .kanBeskrivelse(rs.getString("beskrivelse"))
                    .kanIkkeBeskrivelse(rs.getString("kan_ikke_beskrivelse"));
        }
    }
}
