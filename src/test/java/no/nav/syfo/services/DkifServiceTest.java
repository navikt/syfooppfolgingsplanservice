package no.nav.syfo.services;

import no.nav.syfo.service.DkifService;
import no.nav.tjeneste.virksomhet.digitalkontaktinformasjon.v1.DigitalKontaktinformasjonV1;
import no.nav.tjeneste.virksomhet.digitalkontaktinformasjon.v1.informasjon.WSEpostadresse;
import no.nav.tjeneste.virksomhet.digitalkontaktinformasjon.v1.informasjon.WSMobiltelefonnummer;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
public class DkifServiceTest {

    @Mock
    private DigitalKontaktinformasjonV1 dkifV1;
    @InjectMocks
    private DkifService dkifService;

    @Test
    public void verifisertSiste18mndErNullsafe() {
        boolean harVerifisert = dkifService.harVerfisertSiste18Mnd(new WSEpostadresse(), null);
        assertThat(harVerifisert).isFalse();
        harVerifisert = dkifService.harVerfisertSiste18Mnd(null, null);
        assertThat(harVerifisert).isFalse();
        harVerifisert = dkifService.harVerfisertSiste18Mnd(new WSEpostadresse(), new WSMobiltelefonnummer());
        assertThat(harVerifisert).isFalse();
    }

    @Test
    public void verifisertSiste18mnd() {
        boolean harVerifisert = dkifService.harVerfisertSiste18Mnd(new WSEpostadresse()
                .withSistVerifisert(OffsetDateTime.now().minusDays(2)), new WSMobiltelefonnummer().withSistVerifisert(OffsetDateTime.now().minusDays(4)));
        assertThat(harVerifisert).isTrue();
    }
}
