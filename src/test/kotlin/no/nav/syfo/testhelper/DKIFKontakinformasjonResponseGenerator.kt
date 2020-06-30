package no.nav.syfo.testhelper

import no.nav.syfo.dkif.DigitalKontaktinfo
import no.nav.syfo.testhelper.UserConstants.ARBEIDSTAKER_FNR
import no.nav.syfo.testhelper.UserConstants.PERSON_EMAIL
import no.nav.syfo.testhelper.UserConstants.PERSON_TLF

fun generateDigitalKontaktinfo(): DigitalKontaktinfo {
    return DigitalKontaktinfo(
            epostadresse = PERSON_EMAIL,
            kanVarsles = false,
            reservert = false,
            mobiltelefonnummer = PERSON_TLF,
            personident = ARBEIDSTAKER_FNR
    )
}
