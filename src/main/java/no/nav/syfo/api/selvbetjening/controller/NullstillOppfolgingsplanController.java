package no.nav.syfo.api.selvbetjening.controller;

import no.nav.security.token.support.core.api.Unprotected;

import no.nav.syfo.pdl.PdlConsumer;
import no.nav.syfo.repository.dao.OppfolgingsplanDAO;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;
import java.util.List;

import static org.slf4j.LoggerFactory.getLogger;

@RestController
@Unprotected
@RequestMapping(value = "/internal/oppfolgingsplan")
public class NullstillOppfolgingsplanController {

    private static final Logger logger = getLogger(NullstillOppfolgingsplanController.class);
    private final OppfolgingsplanDAO oppfolgingsplanDAO;
    private final PdlConsumer pdlConsumer;

    @Inject
    public NullstillOppfolgingsplanController(
            OppfolgingsplanDAO oppfolgingsplanDAO,
            PdlConsumer pdlConsumer
    ) {
        this.oppfolgingsplanDAO = oppfolgingsplanDAO;
        this.pdlConsumer = pdlConsumer;
    }

    @DeleteMapping(path = "/slett/{id}")
    public ResponseEntity<?> deleteOppfolgingsplanById(
            @PathVariable("id") Long id,
            @Value("${nais.cluster.name}") String env
    ) {
        if (isDev(env)) {
            logger.info("Sletter oppfolgingsplan for id");
            oppfolgingsplanDAO.deleteOppfolgingsplan(id);

            return ResponseEntity.ok().build();
        } else {
            return handleEndpointNotAvailableForProdError();
        }
    }

    @DeleteMapping(path = "/slett/person/{fnr}")
    public ResponseEntity<?> deleteOppfolgingsplanByFnr(
            @PathVariable("fnr") String fnr,
            @Value("${nais.cluster.name}") String env
    ) {
        if (isDev(env)) {
            String aktorId = pdlConsumer.aktorid(fnr);
            List<Long> dialogIder = oppfolgingsplanDAO.hentDialogIDerByAktoerId(aktorId);

            logger.info("Sletter oppfolgingsplaner for aktorId");

            dialogIder.forEach(oppfolgingsplanDAO::deleteOppfolgingsplan);

            return ResponseEntity.ok().build();
        } else {
            return handleEndpointNotAvailableForProdError();
        }
    }

    private ResponseEntity<?> handleEndpointNotAvailableForProdError() {
        logger.error("Det ble gjort kall mot 'slett oppfolgingsplan', men dette endepunktet er togglet av og skal aldri brukes i prod.");
        return ResponseEntity.notFound().build();
    }

    private boolean isDev(String env) {
        return env.equals("dev-fss") || env.equals("local");
    }
}
