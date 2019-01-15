package no.nav.syfo.repository.domain;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(fluent = true)
public class Dokument {
    public byte[] pdf;
    public String xml;
    public String uuid;
}
