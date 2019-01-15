package no.nav.syfo.service;

import no.nav.syfo.repository.dao.DokumentDAO;

import javax.inject.Inject;

public class DokumentService {

    @Inject
    private DokumentDAO dokumentDAO;

    public byte[] hentDokument(String dokumentUuid) {
        return dokumentDAO.hent(dokumentUuid);
    }
}
