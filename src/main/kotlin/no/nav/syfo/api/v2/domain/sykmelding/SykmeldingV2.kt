package no.nav.syfo.api.v2.domain.sykmelding

import no.nav.syfo.model.Sykmelding

data class SykmeldingV2(
    val id: String,
    val fnr: String,
    val sykmeldingsperioder: List<SykmeldingsperiodeV2>,
    val organisasjonsinformasjon: OrganisasjonsinformasjonV2?
)

fun Sykmelding.toSykmeldingV2(): SykmeldingV2 =
    SykmeldingV2(
        id = this.id,
        fnr = this.fnr,
        sykmeldingsperioder = this.sykmeldingsperioder.map { it.toSykmeldingsperiodeV2() },
        organisasjonsinformasjon = this.organisasjonsinformasjon?.toOrganisasjonsinformasjonV2()
    )