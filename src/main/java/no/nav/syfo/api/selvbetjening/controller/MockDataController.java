package no.nav.syfo.api.selvbetjening.controller;

import no.nav.security.token.support.core.api.Unprotected;
import no.nav.syfo.aktorregister.AktorregisterConsumer;
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
@RequestMapping(value = "/internal")
public class MockDataController {

    private static final Logger logger = getLogger(MockDataController.class);
    private final OppfolgingsplanDAO oppfolgingsplanDAO;
    private final AktorregisterConsumer aktorregisterConsumer;

    @Inject
    public MockDataController(
            OppfolgingsplanDAO oppfolgingsplanDAO,
            AktorregisterConsumer aktorregisterConsumer
    ) {
        this.oppfolgingsplanDAO = oppfolgingsplanDAO;
        this.aktorregisterConsumer = aktorregisterConsumer;
    }

    @DeleteMapping(path = "/oppfolgingsplan/slett/{id}")
    public ResponseEntity<?> slettOppfolgingsplanById(
            @PathVariable("id") Long id,
            @Value("${nais.cluster.name}") String env
    ) {
        if (isDev(env)) {
            logger.info("Sletter oppfolgingsplan for id {}", id);
            oppfolgingsplanDAO.deleteOppfolgingsplan(id);

            return ResponseEntity.ok().build();
        } else {
            return handleEndpointNotAvailableForProdError();
        }
    }

    @DeleteMapping(path = "/oppfolgingsplan/slett/{fnr}")
    public ResponseEntity<?> slettOppfolgingsplanByFnr(
            @PathVariable("fnr") String fnr,
            @Value("${nais.cluster.name}") String env
    ) {
        if (isDev(env)) {
            String aktorId = aktorregisterConsumer.hentAktorIdForFnr(fnr);
            List<Long> dialogIder = oppfolgingsplanDAO.hentDialogIDerByAktoerId(aktorId);

            logger.info("Sletter oppfolgingsplaner for aktorId {}", aktorId);

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
        return env.equals("q1") || env.equals("local");
    }
}
