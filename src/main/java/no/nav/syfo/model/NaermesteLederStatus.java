package no.nav.syfo.model;

import lombok.Data;
import lombok.experimental.Accessors;
import java.io.Serializable;
import java.time.LocalDate;

@Data
@Accessors(fluent = true)
public class NaermesteLederStatus implements Serializable {
    public boolean erAktiv;
    public LocalDate aktivFom;
    public LocalDate aktivTom;
}
