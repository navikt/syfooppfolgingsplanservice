package no.nav.syfo.api.exception;

import no.nav.security.token.support.spring.validation.interceptor.JwtTokenUnauthorizedException;
import no.nav.syfo.brukertilgang.RequestUnauthorizedException;
import no.nav.syfo.metric.Metrikk;
import no.nav.syfo.util.ConflictException;
import org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR
import org.springframework.http.*;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.util.WebUtils;

import javax.inject.Inject;
import javax.validation.ConstraintViolationException;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.NotFoundException;

import org.slf4j.LoggerFactory.getLogger;

@ControllerAdvice
class ControllerExceptionHandler @Inject constructor(private val metrikk: Metrikk) {

    companion object {
        private val log = getLogger(ControllerExceptionHandler::class.java);
    }

    private val BAD_REQUEST_MSG = "Kunne ikke tolke inndataene"
    private val CONFLICT_MSG = "Det oppstod en konflikt i tilstand"
    private val FORBIDDEN_MSG = "Handling er forbudt"
    private val INTERNAL_MSG = "Det skjedde en uventet feil"
    private val UNAUTHORIZED_MSG = "Autorisasjonsfeil"
    private val NOT_FOUND_MSG = "Fant ikke ressurs"

    @ExceptionHandler(
            Exception::class,
            ConflictException::class,
            ConstraintViolationException::class,
            ForbiddenException::class,
            IllegalArgumentException::class,
            JwtTokenUnauthorizedException::class,
            NotFoundException::class
    )
    fun handleException(ex: Exception, request: WebRequest) : ResponseEntity<ApiError> {
        return when (ex) {
            is RequestUnauthorizedException ->  { handleRequestUnauthorizedException(ex, request) }
            is ConstraintViolationException ->  { handleConstraintViolationException(ex, request) }
            is ForbiddenException ->            { handleForbiddenException(ex, request) }
            is IllegalArgumentException ->      { handleIllegalArgumentException(ex, request) }
            is JwtTokenUnauthorizedException -> { handleJwtTokenUnauthorizedException(ex, request) }
            is NotFoundException ->             { handleNotFoundException(ex, request) }
            is ConflictException ->             { handleConflictException(ex, request) }
            else ->                             { handleExceptionInternal(ex, ApiError(INTERNAL_SERVER_ERROR.value(), INTERNAL_MSG), INTERNAL_SERVER_ERROR, request) }
        }
    }

    private fun handleRequestUnauthorizedException(ex: RequestUnauthorizedException, request: WebRequest) : ResponseEntity<ApiError> {
        return handleExceptionInternal(ex, ApiError(HttpStatus.UNAUTHORIZED.value(), UNAUTHORIZED_MSG), HttpStatus.UNAUTHORIZED, request)
    }

    private fun handleConstraintViolationException(ex: ConstraintViolationException, request: WebRequest) : ResponseEntity<ApiError> {
        return handleExceptionInternal(ex, ApiError(HttpStatus.BAD_REQUEST.value(), BAD_REQUEST_MSG), HttpStatus.BAD_REQUEST, request)
    }

    private fun handleForbiddenException(ex: ForbiddenException, request: WebRequest) : ResponseEntity<ApiError> {
        return handleExceptionInternal(ex, ApiError(HttpStatus.FORBIDDEN.value(), FORBIDDEN_MSG), HttpStatus.FORBIDDEN, request)
    }

    private fun handleIllegalArgumentException(ex: IllegalArgumentException, request: WebRequest) : ResponseEntity<ApiError> {
        return handleExceptionInternal(ex, ApiError(HttpStatus.BAD_REQUEST.value(), BAD_REQUEST_MSG), HttpStatus.BAD_REQUEST, request)
    }

    private fun handleJwtTokenUnauthorizedException(ex: JwtTokenUnauthorizedException, request: WebRequest) : ResponseEntity<ApiError> {
        return handleExceptionInternal(ex, ApiError(HttpStatus.UNAUTHORIZED.value(), UNAUTHORIZED_MSG), HttpStatus.UNAUTHORIZED, request)
    }

    private fun handleNotFoundException(ex: NotFoundException, request: WebRequest) : ResponseEntity<ApiError> {
        return handleExceptionInternal(ex, ApiError(HttpStatus.NOT_FOUND.value(), NOT_FOUND_MSG), HttpStatus.NOT_FOUND, request)
    }

    private fun handleConflictException(ex: ConflictException, request: WebRequest) : ResponseEntity<ApiError> {
        return handleExceptionInternal(ex, ApiError(HttpStatus.CONFLICT.value(), CONFLICT_MSG), HttpStatus.CONFLICT, request)
    }

    private fun handleExceptionInternal(ex: Exception, body: ApiError, status: HttpStatus, request: WebRequest) : ResponseEntity<ApiError> {
        metrikk.tellHttpKall(status.value())
        if (!status.is2xxSuccessful) {
            if (status == INTERNAL_SERVER_ERROR) {
                log.error("Uventet feil: {} : {}", ex.javaClass.toString(), ex.message, ex)
                request.setAttribute(WebUtils.ERROR_EXCEPTION_ATTRIBUTE, ex, WebRequest.SCOPE_REQUEST)
            } else {
                log.warn("Fikk response med kode : {} : {} : {}", status.value(), ex.javaClass.toString(), ex.message, ex)
            }
        }
        return ResponseEntity(body, status)
    }
}
