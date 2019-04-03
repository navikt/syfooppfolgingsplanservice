package no.nav.syfo.api.selvbetjening.domain;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Accessors(fluent = true)
public class RSBrukerOppfolgingsplan {
    public Long id;

    public LocalDateTime sistEndretDato;
    public LocalDate opprettetDato;

    public String status;
    public RSVirksomhet virksomhet;

    public RSGodkjentPlan godkjentPlan;
    public List<RSGodkjenning> godkjenninger = new ArrayList<>();
    public List<RSArbeidsoppgave> arbeidsoppgaveListe = new ArrayList<>();
    public List<RSTiltak> tiltakListe = new ArrayList<>();
    public List<RSAvbruttplan> avbruttPlanListe = new ArrayList<>();

    public RSArbeidsgiver arbeidsgiver;
    public RSPerson arbeidstaker;
    public RSPerson sistEndretAv;
}
