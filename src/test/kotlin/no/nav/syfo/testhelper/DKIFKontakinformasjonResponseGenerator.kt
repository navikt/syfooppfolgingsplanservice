package no.nav.syfo.testhelper

import no.nav.syfo.dkif.DigitalKontaktinfo
import no.nav.syfo.testhelper.UserConstants.*

fun generateDigitalKontaktinfo(): DigitalKontaktinfo {
    return DigitalKontaktinfo(
            epostadresse = PERSON_EMAIL,
            kanVarsles = false,
            reservert = false,
            mobiltelefonnummer = PERSON_TLF,
            personident = ARBEIDSTAKER_FNR
    )
}
