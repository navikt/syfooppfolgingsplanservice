package no.nav.syfo.api.v2.domain.sykmelding

import no.nav.syfo.model.Organisasjonsinformasjon

data class OrganisasjonsinformasjonV2(
    val orgnummer: String,
    val orgNavn: String
)

fun Organisasjonsinformasjon.toOrganisasjonsinformasjonV2(): OrganisasjonsinformasjonV2 =
    OrganisasjonsinformasjonV2(
        orgnummer = this.orgnummer,
        orgNavn = this.orgNavn
    )