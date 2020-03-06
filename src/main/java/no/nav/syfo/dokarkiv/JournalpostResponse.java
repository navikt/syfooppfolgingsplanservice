package no.nav.syfo.dokarkiv;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(fluent = true)
@JsonSerialize
public class JournalpostResponse {
    public List<DokumentInfo> dokumenter;
    public Integer journalpostId;
    public Boolean journalpostferdigstilt;
    public String journalstatus;
    public String melding;
}

/*
{
  "dokumenter": [
    {
      "brevkode": "NAV 14-05.09",
      "dokumentInfoId": 123,
      "tittel": "Søknad om foreldrepenger ved fødsel"
    }
  ],
  "journalpostId": 12345678,
  "journalpostferdigstilt": true,
  "journalstatus": "ENDELIG",
  "melding": "null"
}
 */
