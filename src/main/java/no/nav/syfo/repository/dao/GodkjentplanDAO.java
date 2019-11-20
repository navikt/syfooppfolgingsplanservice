package no.nav.syfo.repository.dao;

import no.nav.syfo.domain.GodkjentPlan;
import no.nav.syfo.metric.Metrikk;
import no.nav.syfo.repository.domain.PGodkjentPlan;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.inject.Inject;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static java.time.LocalDateTime.now;
import static no.nav.syfo.mappers.persistency.POppfoelgingsdialogMapper.p2godkjentplan;
import static no.nav.syfo.repository.DbUtil.*;
import static no.nav.syfo.util.MapUtil.mapListe;

@Repository
public class GodkjentplanDAO {

    private JdbcTemplate jdbcTemplate;
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private Metrikk metrikk;

    @Inject
    public GodkjentplanDAO(
            JdbcTemplate jdbcTemplate,
            NamedParameterJdbcTemplate namedParameterJdbcTemplate,
            Metrikk metrikk
    ) {
        this.jdbcTemplate = jdbcTemplate;
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
        this.metrikk = metrikk;
    }

    public Optional<GodkjentPlan> godkjentPlanByOppfoelgingsdialogId(long oppfoelgingsdialogId) {
        return queryOptional(jdbcTemplate, "SELECT * FROM godkjentplan WHERE oppfoelgingsdialog_id = ?", new GodkjentPlanRowMapper(), oppfoelgingsdialogId)
                .map(p2godkjentplan);
    }

    public Long godkjentPlanIdByOppfoelgingsdialogId(long oppfoelgingsdialogId) {
        return jdbcTemplate.queryForObject("SELECT godkjentplan_id FROM godkjentplan WHERE oppfoelgingsdialog_id = ?", (rs, rowNum) -> rs.getLong("godkjentplan_id"), oppfoelgingsdialogId);
    }

    public List<GodkjentPlan> hentIkkeSaksfoertePlaner() {
        return mapListe(jdbcTemplate.query("SELECT * FROM godkjentplan WHERE sak_id IS NULL AND delt_med_nav = 1", new GodkjentPlanRowMapper()), p2godkjentplan);
    }

    public List<GodkjentPlan> hentIkkeJournalfoertePlaner() {
        return mapListe(jdbcTemplate.query("SELECT * FROM godkjentplan WHERE sak_id IS NOT NULL AND journalpost_id IS NULL AND delt_med_nav = 1", new GodkjentPlanRowMapper()), p2godkjentplan);
    }

    public GodkjentPlan create(GodkjentPlan godkjentPlan) {
        metrikk.tellAntallDagerMellom("godkjentplanlengde", godkjentPlan.gyldighetstidspunkt.fom, godkjentPlan.gyldighetstidspunkt.tom);

        long id = nesteSekvensverdi("GODKJENTPLAN_ID_SEQ", jdbcTemplate);
        MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                .addValue("godkjentplan_id", id)
                .addValue("oppfoelgingsdialog_id", godkjentPlan.oppfoelgingsdialogId)
                .addValue("dokument_uuid", godkjentPlan.dokumentUuid)
                .addValue("created", convert(now()))
                .addValue("fom", convert(godkjentPlan.gyldighetstidspunkt.fom.atStartOfDay()))
                .addValue("tom", convert(godkjentPlan.gyldighetstidspunkt.tom.atStartOfDay()))
                .addValue("evalueres", convert(godkjentPlan.gyldighetstidspunkt.evalueres.atStartOfDay()))
                .addValue("tvungen_godkjenning", godkjentPlan.tvungenGodkjenning)
                .addValue("delt_med_nav", godkjentPlan.deltMedNAV)
                .addValue("delt_med_nav_tidspunkt", convert(godkjentPlan.deltMedNAVTidspunkt))
                .addValue("delt_med_fastlege", false);
        namedParameterJdbcTemplate.update("INSERT INTO godkjentplan " +
                "(godkjentplan_id, oppfoelgingsdialog_id, dokument_uuid, created, fom, tom, evalueres, tvungen_godkjenning, " +
                "delt_med_nav, delt_med_nav_tidspunkt, delt_med_fastlege) " +
                "VALUES(:godkjentplan_id, :oppfoelgingsdialog_id, :dokument_uuid, :created, :fom, :tom, :evalueres, :tvungen_godkjenning, " +
                ":delt_med_nav, :delt_med_nav_tidspunkt, :delt_med_fastlege)", namedParameters);
        return godkjentPlan.id(id);
    }

    public void sakId(long oppfoelgingsdialogId, String sakId) {
        jdbcTemplate.update("UPDATE godkjentplan SET sak_id = ? WHERE oppfoelgingsdialog_id = ?", sakId, oppfoelgingsdialogId);
    }

    public void journalpostId(long oppfoelgingsdialogId, String journalpostId) {
        jdbcTemplate.update("UPDATE godkjentplan SET journalpost_id = ? WHERE oppfoelgingsdialog_id = ?", journalpostId, oppfoelgingsdialogId);
    }

    public void delMedNav(long oppfoelgingsdialogId, LocalDateTime deltMedNavTidspunkt) {
        jdbcTemplate.update("UPDATE godkjentplan SET delt_med_nav = 1, delt_med_nav_tidspunkt = ? WHERE oppfoelgingsdialog_id = ?", convert(deltMedNavTidspunkt), oppfoelgingsdialogId);
    }

    public void delMedNavTildelEnhet(long oppfoelgingsdialogId, String tildeltEnhet) {
        jdbcTemplate.update("UPDATE godkjentplan SET tildelt_enhet = ? WHERE oppfoelgingsdialog_id = ?", tildeltEnhet, oppfoelgingsdialogId);
    }

    public void delMedFastlege(long oppfoelgingsdialogId) {
        jdbcTemplate.update("UPDATE godkjentplan SET delt_med_fastlege = 1, delt_med_fastlege_tidspunkt = ? WHERE oppfoelgingsdialog_id = ?", convert(now()), oppfoelgingsdialogId);
    }

    public List<GodkjentPlan> godkjentePlanerSiden(LocalDateTime timestamp) {
        return mapListe(jdbcTemplate.query("SELECT * FROM godkjentplan WHERE DELT_MED_NAV_TIDSPUNKT > ?", new GodkjentPlanRowMapper(), convert(timestamp)), p2godkjentplan);
    }

    private class GodkjentPlanRowMapper implements RowMapper<PGodkjentPlan> {
        public PGodkjentPlan mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new PGodkjentPlan()
                    .id(rs.getLong("godkjentplan_id"))
                    .oppfoelgingsdialogId(rs.getLong("oppfoelgingsdialog_id"))
                    .dokumentUuid(rs.getString("dokument_uuid"))
                    .created(convert(rs.getTimestamp("created")))
                    .fom(convert(rs.getTimestamp("fom")))
                    .tom(convert(rs.getTimestamp("tom")))
                    .evalueres(convert(rs.getTimestamp("evalueres")))
                    .deltMedNavTidspunkt(convert(rs.getTimestamp("delt_med_nav_tidspunkt")))
                    .deltMedFastlegeTidspunkt(convert(rs.getTimestamp("delt_med_fastlege_tidspunkt")))
                    .tvungenGodkjenning(rs.getBoolean("tvungen_godkjenning"))
                    .deltMedNav(rs.getBoolean("delt_med_nav"))
                    .deltMedFastlege(rs.getBoolean("delt_med_fastlege"))
                    .avbruttTidspunkt(convert(rs.getTimestamp("avbrutt_tidspunkt")))
                    .avbruttAv(rs.getString("avbrutt_av"))
                    .sakId(rs.getString("sak_id"))
                    .journalpostId(rs.getString("journalpost_id"))
                    .tildeltEnhet(rs.getString("tildelt_enhet"))
                    ;
        }
    }
}
