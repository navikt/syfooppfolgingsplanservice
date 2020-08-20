package no.nav.syfo.lps

import no.nav.helse.op2016.Skjemainnhold

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
            forsteFravearsdag = skjemainnhold.sykefravaerForSykmeldtArbeidstaker?.foersteFravaersdag,
            sykmeldingsDato = skjemainnhold.sykefravaerForSykmeldtArbeidstaker?.sykmeldingsdato,
            sykmeldingsProsentVedSykmeldDato = skjemainnhold.sykefravaerForSykmeldtArbeidstaker?.sykmeldingsprosentVedSykmeldingsDato
        ),
        tiltak = skjemainnhold.tiltak.tiltaksinformasjon.map {
            Tiltak(
                ordineareArbeidsoppgaverSomKanIkkeKanUtfores = it.ordinaereArbeidsoppgaverSomIkkeKanUtfoeres,
                beskrivelseAvTiltak = it.beskrivelseAvTiltaket,
                maalMedTiltaket = it.maalMedTiltaket,
                tiltaketGjennonforesIPerioden = TiltaketGjennonforesIPerioden(
                    fraDato = it?.tidsrom?.periodeFra,
                    tilDato = it?.tidsrom?.periodeTil
                ),
                tilrettelagtArbeidIkkeMulig = it.tilrettelagtArbeidIkkeMulig,
                sykmeldingsprosendIPerioden = it.sykmeldingsprosentIPerioden,
                behovForBistandFraNav = if (
                    it.isBistandRaadOgVeiledning == null
                    && it.bistandRaadOgVeiledningBeskrivelse == null
                    && it.isBistandDialogMoeteMedNav == null
                    && it.bistandDialogMoeteMedNavBeskrivelse == null
                    && it.isBistandArbeidsrettedeTiltakOgVirkemidler == null
                    && it.bistandArbeidsrettedeTiltakOgVirkemidlerBeskrivelse == null
                    && it.isBistandHjelpemidler == null
                    && it.bistandHjelpemidlerBeskrivelse == null
                ) null else BehovForBistandFraNav(
                    raadOgVeiledning = it.isBistandRaadOgVeiledning,
                    raadOgVeiledningBeskrivelse = it.bistandRaadOgVeiledningBeskrivelse,
                    dialogmoteMed = it.isBistandDialogMoeteMedNav,
                    dialogmoteMedBeskrivelse = it.bistandDialogMoeteMedNavBeskrivelse,
                    arbeidsrettedeTiltak = it.isBistandArbeidsrettedeTiltakOgVirkemidler,
                    arbeidsrettedeTiltakBeskrivelse = it.bistandArbeidsrettedeTiltakOgVirkemidlerBeskrivelse,
                    hjelpemidler = it.isBistandHjelpemidler,
                    hjelpemidlerBeskrivelse = it.bistandHjelpemidlerBeskrivelse
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
                    datoforUnderskift = it.underskriftsdato,
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
