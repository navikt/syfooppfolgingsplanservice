package no.nav.syfo.sykmeldinger.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.experimental.Accessors;
import org.joda.time.LocalDate;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

@Data
@Accessors(fluent = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class SykmeldingDTO {
    public String id;
    public OffsetDateTime mottattTidspunkt;
    public BehandlingsutfallDTO behandlingsutfall;
    public String legekontorOrgnummer;
    public ArbeidsgiverDTO arbeidsgiver;
    public List<SykmeldingsperiodeDTO> sykmeldingsperioder;
    public SykmeldingStatusDTO sykmeldingStatus;
    public MedisinskVurderingDTO medisinskVurdering;
    public boolean skjermesForPasient;
    public PrognoseDTO prognose;
    public Map<String, Map<String, SporsmalSvarDTO>> utdypendeOpplysninger;
    public String tiltakArbeidsplassen;
    public String tiltakNAV;
    public String andreTiltak;
    public MeldingTilNavDTO meldingTilNAV;
    public String meldingTilArbeidsgiver;
    public KontaktMedPasientDTO kontaktMedPasient;
    public OffsetDateTime behandletTidspunkt;
    public BehandlerDTO behandler;
    public LocalDate syketilfelleStartDato;
    public String navnFastlege;
    public boolean egenmeldt;
    public boolean papirsykmelding;
    public boolean harRedusertArbeidsgiverperiode;
    public List<MerknadDTO> merknader;
}


