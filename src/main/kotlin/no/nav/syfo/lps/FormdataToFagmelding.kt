package no.nav.syfo.lps

import no.nav.helse.op2016.Skjemainnhold
import java.time.*

fun mapFormdataToFagmelding(
    skjemainnhold: Skjemainnhold,
    incomingMetadata: IncomingMetadata
): Fagmelding = Fagmelding(
    oppfolgingsplan = Oppfolgingsplan(
        nokkelopplysninger = Nokkelopplysninger(
            virksomhetensnavn = incomingMetadata.senderOrgName,
            organiasjonsnr = incomingMetadata.senderOrgId,
            nearmestelederFornavnEtternavn = FornavnEtternavn(
                fornavn = skjemainnhold.arbeidsgiver.naermesteLederFornavn,
                etternavn = skjemainnhold.arbeidsgiver.naermesteLederEtternavn
            ),
            tlfnearmesteleder = skjemainnhold.arbeidsgiver.telefonNaermesteLeder,
            annenKontaktPersonFornavnEtternavn = FornavnEtternavn(
                fornavn = skjemainnhold.arbeidsgiver.annenKontaktpersonFornavn,
                etternavn = skjemainnhold.arbeidsgiver.annenKontaktpersonEtternavn
            ),
            tlfkontatkperson = skjemainnhold.arbeidsgiver.telefonKontaktperson,
            virksomhetenerIAVirksomhet = skjemainnhold.arbeidsgiver.isVirksomhetErIABedrift,
            virksomhetenHarBedrifsHelseTjeneste = skjemainnhold.arbeidsgiver.isVirksomhetHarBedriftshelsetjeneste
        ),
        opplysningerOmArbeidstakeren = OpplysningerOmArbeidstakeren(
            arbeidstakerenFornavnEtternavn = if (skjemainnhold.sykmeldtArbeidstaker.fornavn.isNullOrBlank() && skjemainnhold.sykmeldtArbeidstaker.etternavn.isNullOrBlank()) null
            else FornavnEtternavn(
                fornavn = skjemainnhold.sykmeldtArbeidstaker.fornavn,
                etternavn = skjemainnhold.sykmeldtArbeidstaker.etternavn
            ),
            fodselsnummer = skjemainnhold.sykmeldtArbeidstaker.fnr,
            tlf = skjemainnhold.sykmeldtArbeidstaker.tlf,
            stillingAvdeling = skjemainnhold.sykmeldtArbeidstaker.stillingAvdeling,
            ordineareArbeidsoppgaver = skjemainnhold.sykmeldtArbeidstaker.ordinaereArbeidsoppgaver
        ),
        opplysingerOmSykefravaeret = if (
            skjemainnhold.sykefravaerForSykmeldtArbeidstaker?.foersteFravaersdag == null
            && skjemainnhold.sykefravaerForSykmeldtArbeidstaker?.sykmeldingsdato == null
            && skjemainnhold.sykefravaerForSykmeldtArbeidstaker?.sykmeldingsprosentVedSykmeldingsDato == null
        ) null else OpplysingerOmSykefravaeret(
            forsteFravearsdag = skjemainnhold.sykefravaerForSykmeldtArbeidstaker?.foersteFravaersdag?.toZonedDateTime(),
            sykmeldingsDato = skjemainnhold.sykefravaerForSykmeldtArbeidstaker?.sykmeldingsdato?.toZonedDateTime(),
            sykmeldingsProsentVedSykmeldDato = skjemainnhold.sykefravaerForSykmeldtArbeidstaker?.sykmeldingsprosentVedSykmeldingsDato
        ),
        tiltak = skjemainnhold.tiltak.tiltaksinformasjon.map {
            Tiltak(
                ordineareArbeidsoppgaverSomKanIkkeKanUtfores = it.ordinaereArbeidsoppgaverSomIkkeKanUtfoeres,
                beskrivelseAvTiltak = it.beskrivelseAvTiltaket,
                maalMedTiltaket = it.maalMedTiltaket,
                tiltaketGjennonforesIPerioden = TiltaketGjennonforesIPerioden(
                    fraDato = it?.tidsrom?.periodeFra?.toZonedDateTime(),
                    tilDato = it?.tidsrom?.periodeTil?.toZonedDateTime()
                ),
                tilrettelagtArbeidIkkeMulig = it.tilrettelagtArbeidIkkeMulig,
                sykmeldingsprosendIPerioden = it.sykmeldingsprosentIPerioden,
                behovForBistandFraNav = if (
                    it.isBistandRaadOgVeiledning == null
                    && it.isBistandDialogMoeteMedNav == null
                    && it.isBistandArbeidsrettedeTiltakOgVirkemidler == null
                    && it.isBistandHjelpemidler == null
                ) null else BehovForBistandFraNav(
                    raadOgVeiledning = it.isBistandRaadOgVeiledning,
                    dialogmoteMed = it.isBistandDialogMoeteMedNav,
                    arbeidsrettedeTiltak = it.isBistandArbeidsrettedeTiltakOgVirkemidler,
                    hjelpemidler = it.isBistandHjelpemidler
                ),
                behovForBistandFraAndre = if (
                    it.isBistandBedriftshelsetjenesten == null
                    && it.isBistandAndre == null
                    && it.isBistandBedriftshelsetjenesten == null
                ) null else BehovForBistandFraAndre(
                    bedriftsHelsetjenesten = it.isBistandBedriftshelsetjenesten,
                    andre = it.isBistandAndre,
                    andreFritekts = it.bistandAndreBeskrivelse
                ),
                behovForAvklaringMedLegeSykmeleder = it.behovForAvklaringLegeSykmelder,
                vurderingEffektAvTiltak = VurderingEffektAvTiltak(
                    behovForNyeTiltak = it.isBehovForNyeTiltak,
                    vurderingEffektAvTiltakFritekst = it.vurderingAvTiltak
                ),
                fremdrift = it.oppfoelgingssamtaler,
                underskrift = Underskift(
                    datoforUnderskift = it.underskriftsdato?.toZonedDateTime(),
                    signertPapirkopiForeliggerPaaArbeidsplasssen = it.isSignertPapirkopiForeligger
                )
            )
        },
        arbeidstakersDeltakelse = skjemainnhold.arbeidstakersDeltakelse?.let {
            ArbeidstakersDeltakelse(
                arbeidstakerMedvirkGjeonnforingOppfolginsplan = it.isArbeidstakerMedvirketGjennomfoering,
                hvorforHarIkkeArbeidstakerenMedvirket = it.arbeidstakerIkkeMedvirketGjennomfoeringBegrunnelse
            )
        },
        utfyllendeInfo = skjemainnhold.utfyllendeOpplysninger
    )
)

fun LocalDate.toZonedDateTime(): ZonedDateTime = atStartOfDay(ZoneId.systemDefault())