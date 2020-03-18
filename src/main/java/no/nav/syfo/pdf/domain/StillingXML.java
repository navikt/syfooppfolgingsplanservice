package no.nav.syfo.pdf.domain;


import javax.xml.bind.annotation.*;
import java.math.BigDecimal;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
        name = "stilling",
        propOrder = {
                "yrke",
                "prosent",
        }
)
public class StillingXML {
    public String yrke;
    public BigDecimal prosent;

    public StillingXML withYrke(String yrke) {
        this.yrke = yrke;
        return this;
    }

    public StillingXML withProsent(BigDecimal prosent) {
        this.prosent = prosent;
        return this;
    }
}
