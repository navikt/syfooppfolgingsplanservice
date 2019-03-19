package no.nav.syfo.service;

import no.nav.syfo.repository.dao.*;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.List;

import static java.util.stream.Collectors.toList;

@Service
public class VeilederBehandlingService {

    private GodkjentplanDAO godkjentplanDAO;
    private OppfoelingsdialogDAO oppfoelingsdialogDAO;
    private VeilederBehandlingDAO veilederBehandlingDAO;

    @Inject
    public VeilederBehandlingService(
            GodkjentplanDAO godkjentplanDAO,
            OppfoelingsdialogDAO oppfoelingsdialogDAO,
            VeilederBehandlingDAO veilederBehandlingDAO
    ) {
        this.godkjentplanDAO = godkjentplanDAO;
        this.oppfoelingsdialogDAO = oppfoelingsdialogDAO;
        this.veilederBehandlingDAO = veilederBehandlingDAO;
    }


    public List<String> hentSykmeldteMedUlesteOppfolgingsplaner(String enhetId) {
        return veilederBehandlingDAO.hentVeilederBehandlingByEnhetId(enhetId)
                .stream()
                .map(veilederBehandling -> godkjentplanDAO.oppfolgingsplanIdByGodkjentPlanId(veilederBehandling.godkjentplanId))
                .map(oppfolgingsplanId -> oppfoelingsdialogDAO.aktorIdByOppfolgingsplanId(oppfolgingsplanId))
                .distinct()
                .collect(toList());
    }

}
