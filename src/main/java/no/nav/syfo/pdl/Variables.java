package no.nav.syfo.pdl;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(fluent = true)
@JsonSerialize
public class Variables {
    public String ident;
    public boolean navnHistorikk = false;
}
