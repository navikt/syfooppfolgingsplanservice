package no.nav.syfo.repository.dao;

import no.nav.syfo.domain.Oppfolgingsplan;
import no.nav.syfo.repository.domain.POppfoelgingsdialog;
import org.slf4j.Logger;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.inject.Inject;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

import static java.time.LocalDateTime.now;
import static java.util.Collections.emptyList;
import static no.nav.syfo.mappers.persistency.POppfoelgingsdialogMapper.p2oppfoelgingsdialog;
import static no.nav.syfo.repository.DbUtil.convert;
import static no.nav.syfo.repository.DbUtil.nesteSekvensverdi;
import static no.nav.syfo.util.MapUtil.map;
import static no.nav.syfo.util.MapUtil.mapListe;
import static no.nav.syfo.util.OppfoelgingsdialogUtil.erArbeidstakeren;
import static org.slf4j.LoggerFactory.getLogger;

@Repository
public class OppfolgingsplanDAO {

    private static final Logger log = getLogger(OppfolgingsplanDAO.class);

    private JdbcTemplate jdbcTemplate;
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private ArbeidsoppgaveDAO arbeidsoppgaveDAO;
    private GodkjenningerDAO godkjenningerDAO;
    private GodkjentplanDAO godkjentplanDAO;
    private TiltakDAO tiltakDAO;

    @Inject
    public OppfolgingsplanDAO(
            JdbcTemplate jdbcTemplate,
            NamedParameterJdbcTemplate namedParameterJdbcTemplate,
            ArbeidsoppgaveDAO arbeidsoppgaveDAO,
            GodkjenningerDAO godkjenningerDAO,
            GodkjentplanDAO godkjentplanDAO,
            TiltakDAO tiltakDAO
    ) {
        this.jdbcTemplate = jdbcTemplate;
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
        this.arbeidsoppgaveDAO = arbeidsoppgaveDAO;
        this.godkjenningerDAO = godkjenningerDAO;
        this.godkjentplanDAO = godkjentplanDAO;
        this.tiltakDAO = tiltakDAO;
    }

    public Oppfolgingsplan create(Oppfolgingsplan oppfolgingsplan) {
        long id = nesteSekvensverdi("OPPFOELGINGSDIALOG_ID_SEQ", jdbcTemplate);
        MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                .addValue("oppfoelgingsdialog_id", id)
                .addValue("uuid", UUID.randomUUID().toString())
                .addValue("aktoer_id", oppfolgingsplan.arbeidstaker.aktoerId)
                .addValue("virksomhetsnummer", oppfolgingsplan.virksomhet.virksomhetsnummer)
                .addValue("opprettet_av", oppfolgingsplan.opprettetAvAktoerId)
                .addValue("created", convert(now()))
                .addValue("arbeidsgiver_sist_innlogget", convert(oppfolgingsplan.arbeidsgiver.sistInnlogget))
                .addValue("sykmeldt_sist_innlogget", convert(oppfolgingsplan.arbeidstaker.sistInnlogget))
                .addValue("sist_endret_av", oppfolgingsplan.sistEndretAvAktoerId)
                .addValue("sist_endret", convert(now()))
                .addValue("arbeidsgiver_sist_aksessert", convert(oppfolgingsplan.arbeidsgiver.sistAksessert))
                .addValue("sykmeldt_sist_aksessert", convert(oppfolgingsplan.arbeidstaker.sistInnlogget))
                .addValue("arbeidsgiver_sist_endret", convert(oppfolgingsplan.arbeidsgiver.sisteEndring))
                .addValue("sykmeldt_sist_endret", convert(oppfolgingsplan.arbeidstaker.sisteEndring))
                .addValue("samtykke_sykmeldt", null)
                .addValue("samtykke_arbeidsgiver", null)
                .addValue("sm_fnr", oppfolgingsplan.arbeidstaker.fnr)
                .addValue("opprettet_av_fnr", oppfolgingsplan.opprettetAvFnr);

        namedParameterJdbcTemplate.update("insert into oppfoelgingsdialog " +
                "(oppfoelgingsdialog_id, uuid, aktoer_id, virksomhetsnummer, opprettet_av, created, arbeidsgiver_sist_innlogget, " +
                "sykmeldt_sist_innlogget, sist_endret_av, sist_endret, arbeidsgiver_sist_aksessert, sykmeldt_sist_aksessert, " +
                "arbeidsgiver_sist_endret, sykmeldt_sist_endret, samtykke_sykmeldt, samtykke_arbeidsgiver, sm_fnr, opprettet_av_fnr) " +
                "values(:oppfoelgingsdialog_id, :uuid, :aktoer_id, :virksomhetsnummer, :opprettet_av, :created, :arbeidsgiver_sist_innlogget, " +
                ":sykmeldt_sist_innlogget, :sist_endret_av, :sist_endret, :arbeidsgiver_sist_aksessert, :sykmeldt_sist_aksessert, " +
                ":arbeidsgiver_sist_endret, :sykmeldt_sist_endret, :samtykke_sykmeldt, :samtykke_arbeidsgiver, :sm_fnr, :opprettet_av_fnr)", namedParameters);

        return oppfolgingsplan.id(id);
    }

    public Oppfolgingsplan oppfolgingsplanByTiltakId(Long tiltakId) {
        return map(jdbcTemplate.queryForObject("select * from oppfoelgingsdialog join tiltak " +
                "on tiltak.oppfoelgingsdialog_id = oppfoelgingsdialog.oppfoelgingsdialog_id " +
                "where tiltak_id = ?", new OppfoelgingsdialogRowMapper(), tiltakId), p2oppfoelgingsdialog);
    }

    public Oppfolgingsplan finnOppfolgingsplanMedId(Long oppfoelgingsdialogId) {
        return map(jdbcTemplate.queryForObject("select * from oppfoelgingsdialog where oppfoelgingsdialog_id = ?", new OppfoelgingsdialogRowMapper(), oppfoelgingsdialogId), p2oppfoelgingsdialog);
    }

    public List<Oppfolgingsplan> oppfolgingsplanerKnyttetTilSykmeldt(String aktoerId) {
        return mapListe(jdbcTemplate.query("select * from oppfoelgingsdialog where aktoer_id = ?", new OppfoelgingsdialogRowMapper(), aktoerId), p2oppfoelgingsdialog);
    }

    public List<Oppfolgingsplan> oppfolgingsplanerKnyttetTilSykmeldtogVirksomhet(String sykmeldtAktoerId, String virksomhetsnummer) {
        return mapListe(jdbcTemplate.query("select * from oppfoelgingsdialog where aktoer_id = ? and virksomhetsnummer = ?", new OppfoelgingsdialogRowMapper(), sykmeldtAktoerId, virksomhetsnummer), p2oppfoelgingsdialog);
    }

    public Oppfolgingsplan populate(Oppfolgingsplan oppfolgingplan) {
        oppfolgingplan.arbeidsoppgaveListe = arbeidsoppgaveDAO.arbeidsoppgaverByOppfoelgingsdialogId(oppfolgingplan.id);
        oppfolgingplan.tiltakListe = tiltakDAO.finnTiltakByOppfoelgingsdialogId(oppfolgingplan.id);
        oppfolgingplan.godkjenninger = godkjenningerDAO.godkjenningerByOppfoelgingsdialogId(oppfolgingplan.id);
        oppfolgingplan.godkjentPlan = godkjentplanDAO.godkjentPlanByOppfolgingsplanId(oppfolgingplan.id);

        oppfolgingplan.godkjentPlan
                .filter(godkjentPlan -> !oppfolgingplan.godkjenninger.isEmpty())
                .ifPresent(godkjentPlan -> {
                    log.warn("Sletter godkjenning som finnes selv om oppfølgingsplanen allerede er godkjent");
                    godkjenningerDAO.deleteAllByOppfoelgingsdialogId(godkjentPlan.oppfoelgingsdialogId);
                    oppfolgingplan.godkjenninger = emptyList();
                });

        return oppfolgingplan;

    }

    public void sistEndretAv(Long oppfoelgingsplanId, String innloggetAktoerId) {
        Oppfolgingsplan oppfolgingsplan = finnOppfolgingsplanMedId(oppfoelgingsplanId);
        if (erArbeidstakeren(oppfolgingsplan, innloggetAktoerId)) {
            jdbcTemplate.update("update oppfoelgingsdialog set sykmeldt_sist_endret = ?, sist_endret = ?, sist_endret_av = ? where oppfoelgingsdialog_id = ?",
                    convert(now()), convert(now()), innloggetAktoerId, oppfoelgingsplanId);
        } else {
            jdbcTemplate.update("update oppfoelgingsdialog set arbeidsgiver_sist_endret = ?, sist_endret = ?, sist_endret_av = ? where oppfoelgingsdialog_id = ?",
                    convert(now()), convert(now()), innloggetAktoerId, oppfoelgingsplanId);
        }
    }

    public void lagreSamtykkeArbeidsgiver(long oppfolgingplanId, boolean samtykke) {
        jdbcTemplate.update("update oppfoelgingsdialog set samtykke_arbeidsgiver = ? where oppfoelgingsdialog_id = ?", samtykke, oppfolgingplanId);
    }

    public void lagreSamtykkeSykmeldt(long oppfolgingsplanId, boolean samtykke) {
        jdbcTemplate.update("update oppfoelgingsdialog set samtykke_sykmeldt = ? where oppfoelgingsdialog_id = ?", samtykke, oppfolgingsplanId);
    }

    public void oppdaterSistInnlogget(Oppfolgingsplan oppfolgingsplan, String innloggetAktoerId) {
        if (innloggetAktoerId.equals(oppfolgingsplan.arbeidstaker.aktoerId)) {
            jdbcTemplate.update("update oppfoelgingsdialog set sykmeldt_sist_innlogget = ? where oppfoelgingsdialog_id = ?", convert(now()), oppfolgingsplan.id);
        } else {
            jdbcTemplate.update("update oppfoelgingsdialog set arbeidsgiver_sist_innlogget = ? where oppfoelgingsdialog_id = ?", convert(now()), oppfolgingsplan.id);
        }
    }

    public void oppdaterSistAksessert(Oppfolgingsplan oppfolgingsplan, String innloggetAktoerId) {
        if (innloggetAktoerId.equals(oppfolgingsplan.arbeidstaker.aktoerId)) {
            jdbcTemplate.update("update oppfoelgingsdialog set sykmeldt_sist_aksessert = ? where oppfoelgingsdialog_id = ?", convert(now()), oppfolgingsplan.id);
        } else {
            jdbcTemplate.update("update oppfoelgingsdialog set arbeidsgiver_sist_aksessert = ? where oppfoelgingsdialog_id = ?", convert(now()), oppfolgingsplan.id);
        }
    }

    public void avbryt(long oppfolgingsplanId, String innloggetAktoerId) {
        jdbcTemplate.update("update godkjentplan set avbrutt_av = ?, avbrutt_tidspunkt = ? where oppfoelgingsdialog_id = ?",
                innloggetAktoerId, convert(now()), oppfolgingsplanId);
    }

    public void nullstillSamtykke(long oppfolgingsplanId) {
        jdbcTemplate.update("update oppfoelgingsdialog set samtykke_sykmeldt = ? where oppfoelgingsdialog_id = ?", null, oppfolgingsplanId);
        jdbcTemplate.update("update oppfoelgingsdialog set samtykke_arbeidsgiver = ? where oppfoelgingsdialog_id = ?", null, oppfolgingsplanId);
    }

    public void deleteOppfolgingsplan(long oppfolgingsdialogId) {
        jdbcTemplate.update("DELETE ARBEIDSOPPGAVE WHERE OPPFOELGINGSDIALOG_ID = ?", oppfolgingsdialogId);
        jdbcTemplate.query("SELECT * FROM TILTAK WHERE OPPFOELGINGSDIALOG_ID = ?", (rs, rowNum) -> rs.getString("TILTAK_ID"), oppfolgingsdialogId)
                .forEach(tiltakId -> jdbcTemplate.update("DELETE KOMMENTAR WHERE TILTAK_ID = ?", tiltakId));
        jdbcTemplate.update("DELETE TILTAK WHERE OPPFOELGINGSDIALOG_ID = ?", oppfolgingsdialogId);
        jdbcTemplate.update("DELETE GODKJENNING WHERE OPPFOELGINGSDIALOG_ID = ?", oppfolgingsdialogId);
        jdbcTemplate.query("SELECT * from GODKJENTPLAN WHERE OPPFOELGINGSDIALOG_ID = ?", (rs, rowNum) -> rs.getString("DOKUMENT_UUID"), oppfolgingsdialogId)
                .forEach(uuid -> jdbcTemplate.update("DELETE DOKUMENT WHERE DOKUMENT_UUID = ?", uuid));
        jdbcTemplate.query("SELECT * from GODKJENTPLAN WHERE OPPFOELGINGSDIALOG_ID = ?", (rs, rowNum) -> rs.getLong("GODKJENTPLAN_ID"), oppfolgingsdialogId)
                .forEach(id -> jdbcTemplate.update("DELETE VEILEDER_BEHANDLING WHERE GODKJENTPLAN_ID = ?", id));
        jdbcTemplate.update("DELETE GODKJENTPLAN WHERE OPPFOELGINGSDIALOG_ID = ?", oppfolgingsdialogId);
        jdbcTemplate.update("DELETE OPPFOELGINGSDIALOG WHERE OPPFOELGINGSDIALOG_ID = ?", oppfolgingsdialogId);
    }

    public List<Long> hentDialogIDerByAktoerId(String aktoerId) {
        return jdbcTemplate.query("SELECT * FROM OPPFOELGINGSDIALOG WHERE AKTOER_ID = ?", (rs, rowNum) -> rs.getLong("OPPFOELGINGSDIALOG_ID"), aktoerId);
    }

    public List<POppfoelgingsdialog> plansWithoutFnr() {
        return jdbcTemplate.query("SELECT * FROM oppfoelgingsdialog WHERE sm_fnr IS NULL OR opprettet_av_fnr OFFSET 0 ROWS FETCH NEXT 50 ROWS ONLY", new AktorIdMigrationRowMapper());
    }

    public boolean updateSmFnr(Long id, String fnr) {
        String updateSql = "UPDATE oppfoelgingsdialog SET sm_fnr = ? WHERE id = ?";
        return jdbcTemplate.update(updateSql, fnr, id) == 0;
    }

    public boolean updateOpprettetAvFnr(Long id, String fnr) {
        String updateSql = "UPDATE oppfoelgingsdialog SET opprettet_av_fnr = ? WHERE id = ?";
        return jdbcTemplate.update(updateSql, fnr, id) == 0;
    }

    private class AktorIdMigrationRowMapper implements RowMapper<POppfoelgingsdialog> {
        public POppfoelgingsdialog mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new POppfoelgingsdialog()
                    .id(rs.getLong("oppfoelgingsdialog_id"))
                    .aktoerId(rs.getString("aktoer_id"))
                    .opprettetAv(rs.getString("opprettet_av"));
        }
    }

    private class OppfoelgingsdialogRowMapper implements RowMapper<POppfoelgingsdialog> {
        public POppfoelgingsdialog mapRow(ResultSet rs, int rowNum) throws SQLException {
            Boolean samtykke_sykmeldt = rs.getBoolean("samtykke_sykmeldt");
            samtykke_sykmeldt = rs.wasNull() ? null : samtykke_sykmeldt;
            Boolean samtykke_arbeidsgiver = rs.getBoolean("samtykke_arbeidsgiver");
            samtykke_arbeidsgiver = rs.wasNull() ? null : samtykke_arbeidsgiver;
            return new POppfoelgingsdialog()
                    .id(rs.getLong("oppfoelgingsdialog_id"))
                    .uuid(rs.getString("uuid"))
                    .aktoerId(rs.getString("aktoer_id"))
                    .virksomhetsnummer(rs.getString("virksomhetsnummer"))
                    .opprettetAv(rs.getString("opprettet_av"))
                    .created(convert(rs.getTimestamp("created")))
                    .sisteInnloggingArbeidsgiver(convert(rs.getTimestamp("arbeidsgiver_sist_innlogget")))
                    .sisteInnloggingSykmeldt(convert(rs.getTimestamp("sykmeldt_sist_innlogget")))
                    .sistEndretAv(rs.getString("sist_endret_av"))
                    .sistEndret(convert(rs.getTimestamp("sist_endret")))
                    .sistAksessertArbeidsgiver(convert(rs.getTimestamp("arbeidsgiver_sist_aksessert")))
                    .sistAksessertSykmeldt(convert(rs.getTimestamp("sykmeldt_sist_aksessert")))
                    .sistEndretArbeidsgiver(convert(rs.getTimestamp("arbeidsgiver_sist_endret")))
                    .sistEndretSykmeldt(convert(rs.getTimestamp("sykmeldt_sist_endret")))
                    .samtykkeSykmeldt(samtykke_sykmeldt)
                    .samtykkeArbeidsgiver(samtykke_arbeidsgiver)
                    .smFnr(rs.getString("sm_fnr"))
                    .opprettetAvFnr(rs.getString("opprettet_av_fnr"));
        }
    }
}
