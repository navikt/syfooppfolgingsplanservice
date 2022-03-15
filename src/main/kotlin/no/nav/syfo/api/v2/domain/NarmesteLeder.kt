package no.nav.syfo.api.v2.domain

import no.nav.syfo.model.Naermesteleder
import java.time.LocalDate
import java.time.LocalDateTime

data class NarmesteLeder (
    val virksomhetsnummer: String,
    val erAktiv: Boolean,
    val aktivFom: LocalDate,
    val aktivTom: LocalDate?,
    val navn: String = " ",
    val fnr: String,
    val epost: String?,
    val tlf: String?,
    val sistInnlogget: LocalDateTime?,
    val samtykke: Boolean?
)

fun Naermesteleder.mapToNarmesteLeder(): NarmesteLeder {
    return NarmesteLeder(
        virksomhetsnummer = this.orgnummer,
        navn = this.navn,
        epost = this.epost,
        tlf = this.mobil,
        erAktiv = this.naermesteLederStatus.erAktiv,
        aktivFom = this.naermesteLederStatus.aktivFom,
        aktivTom = this.naermesteLederStatus.aktivTom,
        fnr = this.naermesteLederFnr,
        sistInnlogget = null,
        samtykke = null
    )
}
