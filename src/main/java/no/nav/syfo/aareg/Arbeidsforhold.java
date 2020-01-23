package no.nav.syfo.aareg;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(fluent = true)
public class Arbeidsforhold {
    public Ansettelsesperiode ansettelsesperiode;
    public List<AntallTimerForTimeloennet> antallTimerForTimeloennet;
    public List<Arbeidsavtale> arbeidsavtaler;
    public String arbeidsforholdId;
    public OpplysningspliktigArbeidsgiver arbeidsgiver;
    public Person arbeidstaker;
    public boolean innrapportertEtterAOrdningen;
    public int navArbeidsforholdId;
    public OpplysningspliktigArbeidsgiver opplysningspliktig;
    public List<PermisjonPermittering> permisjonPermitteringer;
    public String registrert;
    public String sistBekreftet;
    public Sporingsinformasjon sporingsinformasjon;
    public String type;
    public List<Utenlandsopphold> utenlandsopphold;
}
