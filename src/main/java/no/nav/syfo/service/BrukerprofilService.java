package no.nav.syfo.service;

import no.nav.syfo.domain.Person;
import no.nav.syfo.pdl.PdlConsumer;
import no.nav.syfo.pdl.exceptions.NameFromPDLIsNull;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.Optional;

@Service
public class BrukerprofilService {

    private final PdlConsumer pdlConsumer;

    @Inject
    public BrukerprofilService(
            PdlConsumer pdlConsumer
    ) {
        this.pdlConsumer = pdlConsumer;
    }

    public String hentNavnByAktoerId(String aktoerId) {
        if (!aktoerId.matches("\\d{13}$")) {
            throw new RuntimeException();
        }
        String fnr = pdlConsumer.fnr(aktoerId);
        return Optional.ofNullable(pdlConsumer.personName(fnr)).orElseThrow(() -> new NameFromPDLIsNull("Name of person was null"));
    }

    public Person hentNavnOgFnr(String aktorId) {
        if (!aktorId.matches("\\d{13}$")) {
            throw new RuntimeException();
        }
        String fnr = pdlConsumer.fnr(aktorId);
        String navn = Optional.ofNullable(pdlConsumer.personName(fnr)).orElseThrow(() -> new NameFromPDLIsNull("Name of person was null"));

        return new Person()
                .navn(navn)
                .fnr(fnr);
    }
}

