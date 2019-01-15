package no.nav.syfo.service;

import no.nav.syfo.repository.dao.GodkjentplanDAO;
import no.nav.syfo.repository.dao.OppfoelingsdialogDAO;
import no.nav.syfo.repository.dao.VeilederBehandlingDAO;

import javax.inject.Inject;
import java.util.List;

import static java.util.stream.Collectors.toList;

public class VeilederBehandlingService {

    @Inject
    private VeilederBehandlingDAO veilederBehandlingDAO;

    @Inject
    private GodkjentplanDAO godkjentplanDAO;

    @Inject
    private OppfoelingsdialogDAO oppfoelingsdialogDAO;


    public List<String> hentSykmeldteMedUlesteOppfolgingsplaner(String enhetId) {
        return veilederBehandlingDAO.hentVeilederBehandlingByEnhetId(enhetId)
                .stream()
                .map(veilederBehandling -> godkjentplanDAO.oppfolgingsplanIdByGodkjentPlanId(veilederBehandling.godkjentplanId))
                .map(oppfolgingsplanId -> oppfoelingsdialogDAO.aktorIdByOppfolgingsplanId(oppfolgingsplanId))
                .distinct()
                .collect(toList());
    }

}
