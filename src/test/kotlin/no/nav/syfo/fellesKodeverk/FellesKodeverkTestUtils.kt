package no.nav.syfo.fellesKodeverk

import java.util.*

fun fellesKodeverkResponseBody(stillingsnavn: String, stillingskode: String): KodeverkKoderBetydningerResponse {
    val beskrivelse = Beskrivelse()
        .tekst(stillingsnavn)
        .term(stillingsnavn)
    val beskrivelser: MutableMap<String, Beskrivelse> = HashMap()
    beskrivelser[SPRAK] = beskrivelse
    val betydning = Betydning()
        .beskrivelser(beskrivelser)
        .gyldigFra(Date().toString())
        .gyldigTil(Date().toString())
    val betydninger: MutableMap<String, List<Betydning>> = HashMap()
    betydninger[stillingskode] = listOf(betydning)
    return KodeverkKoderBetydningerResponse()
        .betydninger(betydninger)
}

fun fellesKodeverkResponseBodyWithWrongKode(): KodeverkKoderBetydningerResponse {
    val beskrivelse = Beskrivelse()
        .tekst(WRONG_STILLINGSNAVN)
        .term(WRONG_STILLINGSNAVN)
    val beskrivelser: MutableMap<String, Beskrivelse> = HashMap()
    beskrivelser[SPRAK] = beskrivelse
    val betydning = Betydning()
        .beskrivelser(beskrivelser)
        .gyldigFra(Date().toString())
        .gyldigTil(Date().toString())
    val betydninger: MutableMap<String, List<Betydning>> = HashMap()
    betydninger[WRONG_STILLINGSKODE] = listOf(betydning)
    return KodeverkKoderBetydningerResponse()
        .betydninger(betydninger)
}


const val STILLINGSNAVN = "Special Agent"
const val WRONG_STILLINGSNAVN = "Deputy Director"
const val STILLINGSKODE = "1234567"
const val WRONG_STILLINGSKODE = "9876543"
const val SPRAK = "nb"
