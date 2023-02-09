package no.nav.syfo.domain;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.util.Optional.empty;

@Data
@Accessors(fluent = true)
public class Oppfolgingsplan {
    public Long id;
    public String uuid;

    public LocalDateTime opprettet;

    public String sistEndretAvAktoerId;
    public String sistEndretAvFnr;
    public String opprettetAvAktoerId;
    public String opprettetAvFnr;
    public LocalDateTime sistEndretDato;
    public LocalDateTime sistEndretArbeidsgiver;
    public LocalDateTime sistEndretSykmeldt;
    public String status;
    public Virksomhet virksomhet = new Virksomhet();

    public Optional<GodkjentPlan> godkjentPlan = empty();
    public List<Godkjenning> godkjenninger = new ArrayList<>();
    public List<Arbeidsoppgave> arbeidsoppgaveListe = new ArrayList<>();
    public List<Tiltak> tiltakListe = new ArrayList<>();

    public Person arbeidsgiver = new Person();
    public Person arbeidstaker = new Person();
}
