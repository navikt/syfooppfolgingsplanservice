package no.nav.syfo.aareg;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(fluent = true)
public class Arbeidsavtale {
    public double antallTimerPrUke;
    public String arbeidstidsordning;
    public double beregnetAntallTimerPrUke;
    public Bruksperiode bruksperiode;
    public Gyldighetsperiode gyldighetsperiode;
    public String sistLoennsendring;
    public String sistStillingsendring;
    public Sporingsinformasjon sporingsinformasjon;
    public double stillingsprosent;
    public String yrke;
}
