package no.nav.syfo.mockdata;

import no.nav.syfo.api.intern.domain.*;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static java.util.Arrays.asList;
import static org.apache.commons.io.IOUtils.toByteArray;
import static org.springframework.http.MediaType.APPLICATION_PDF;

public class MockData {
    public static List<RSOppfoelgingsdialog> mockedOppfoelgingsdialoger() {
        return asList(
                new RSOppfoelgingsdialog()
                        .id(1L)
                        .virksomhet(new RSVirksomhet()
                                .virksomhetsnummer("981566378")
                        )
                        .godkjentPlan(new RSGodkjentPlan()
                                .deltMedNAV(true)
                                .deltMedNAVTidspunkt(LocalDateTime.now())
                                .deltMedFastlege(true)
                                .deltMedFastlegeTidspunkt(LocalDateTime.now())
                                .dokumentUuid("1")
                                .gyldighetstidspunkt(new RSGyldighetstidspunkt()
                                        .fom(LocalDate.now().plusDays(1))
                                        .tom(LocalDate.now().plusWeeks(5))))
        );
    }

    public static ResponseEntity mockPdf() {
        byte[] dokument;
        try {
            dokument = toByteArray(MockData.class.getResourceAsStream("/mock.pdf"));
        } catch (IOException e) {
            throw new RuntimeException();
        }
        return ResponseEntity.ok()
                .contentType(APPLICATION_PDF)
                .body(dokument);
    }

    public static byte[] mockPdfBytes() {
        try {
            return toByteArray(MockData.class.getResourceAsStream("/mock.pdf"));
        } catch (IOException e) {
            throw new RuntimeException();
        }
    }
}
