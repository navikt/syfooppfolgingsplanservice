package no.nav.syfo.dkif

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

const val dkifErrorMessage = "Request to get Kontakinformasjon from DKIF Failed"

@ResponseStatus(code = HttpStatus.INTERNAL_SERVER_ERROR)
class DKIFRequestFailedException(
        message: String = ""
) : RuntimeException("$dkifErrorMessage $message")
