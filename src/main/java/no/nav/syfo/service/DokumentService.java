package no.nav.syfo.service;

import no.nav.syfo.repository.dao.DokumentDAO;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

@Service
public class DokumentService {

    private DokumentDAO dokumentDAO;

    @Inject
    public DokumentService(DokumentDAO dokumentDAO) {
        this.dokumentDAO = dokumentDAO;
    }

    public byte[] hentDokument(String dokumentUuid) {
        return dokumentDAO.hent(dokumentUuid);
    }
}
