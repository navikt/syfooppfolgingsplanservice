package no.nav.syfo.service;

import lombok.extern.slf4j.Slf4j;
import no.nav.syfo.domain.sykmelding.Periode;
import no.nav.tjeneste.virksomhet.sykefravaersoppfoelging.v1.HentSykeforlopperiodeSikkerhetsbegrensning;
import no.nav.tjeneste.virksomhet.sykefravaersoppfoelging.v1.SykefravaersoppfoelgingV1;
import no.nav.tjeneste.virksomhet.sykefravaersoppfoelging.v1.informasjon.WSSykeforlopperiode;
import no.nav.tjeneste.virksomhet.sykefravaersoppfoelging.v1.meldinger.WSHentSykeforlopperiodeRequest;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class SykeforloepService {

    private SykefravaersoppfoelgingV1 sykefravaersoppfoelgingV1;

    @Inject
    public SykeforloepService(SykefravaersoppfoelgingV1 sykefravaersoppfoelgingV1) {
        this.sykefravaersoppfoelgingV1 = sykefravaersoppfoelgingV1;
    }


    public List<Periode> hentSykeforlopperiode(String aktoerid, String orgnr) {
        try {
            List<Periode> sammenslaattePerioder = new ArrayList<>();
            sykefravaersoppfoelgingV1.hentSykeforlopperiode(new WSHentSykeforlopperiodeRequest().withAktoerId(aktoerid).withOrgnummer(orgnr)).getSykeforlopperiodeListe()
                    .stream()
                    .map(wsSykeforlopperiode -> tilSykeforloepsPeriode(wsSykeforlopperiode))
                    .forEach(periode -> {
                        Periode sistePeriode = !sammenslaattePerioder.isEmpty() ? sammenslaattePerioder.get(sammenslaattePerioder.size() - 1) : null;
                        if (sistePeriode == null || !skalSlaaesSammen(sistePeriode, periode)) {
                            sammenslaattePerioder.add(periode);
                        } else {
                            sistePeriode.tom = periode.tom.isAfter(sistePeriode.tom) ? periode.tom : sistePeriode.tom;
                            sistePeriode.fom = periode.fom.isBefore(sistePeriode.fom) ? periode.fom : sistePeriode.fom;
                        }
                    });
            return sammenslaattePerioder;
        } catch (HentSykeforlopperiodeSikkerhetsbegrensning e) {
            log.error("Sikkerhetsbegrensing ved innhenting av sykeforloepsperioder");
            throw new RuntimeException("Sikkerhetsbegrensing ved innhenting av sykeforloepsperioder", e);
        }
    }

    private Periode tilSykeforloepsPeriode(WSSykeforlopperiode wsSykeforlopperiode) {
        String aktivitet = wsSykeforlopperiode.getAktivitet();

        return new Periode()
                .withFom(wsSykeforlopperiode.getFom())
                .withTom(wsSykeforlopperiode.getTom())
                .withGrad(wsSykeforlopperiode.getGrad())
                .withBehandlingsdager(aktivitet.contains("BEHANDLINGSDAGER"))
                .withReisetilskudd(aktivitet.contains("REISETILSKUDD"))
                .withAvventende(aktivitet.contains("AVVENTENDE"));
    }

    private boolean skalSlaaesSammen(Periode siste, Periode ny) {
        return (siste.grad.equals(ny.grad)
                && (siste.behandlingsdager == null ? ny.behandlingsdager == null : siste.behandlingsdager.equals(ny.behandlingsdager))
                && (siste.reisetilskudd == null ? ny.reisetilskudd == null : siste.reisetilskudd.equals(ny.reisetilskudd))
                && (siste.avventende == ny.avventende));
    }
}
