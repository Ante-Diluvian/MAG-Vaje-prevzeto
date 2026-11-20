package si.delavnica.api.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import si.delavnica.api.model.Uporabnik;
import si.delavnica.api.model.Uporabnik.Vloga;
import si.delavnica.api.repository.RacunRepository;

// Redna stranka (vsaj 4 prejšnji računi) dobi 10 % popusta.
@ExtendWith(MockitoExtension.class)
class PopustServiceTest {

	@Mock
	private RacunRepository racuni;

	@InjectMocks
	private PopustService popusti;

	private final Uporabnik stranka = Podatki.uporabnik(Vloga.STRANKA);

	@Test
	void brezPopustaPriTrehPrejsnjihRacunih() {
		when(racuni.countByNalogVoziloLastnikId(stranka.getId())).thenReturn(3L);

		assertThat(popusti.uporabi(stranka, new BigDecimal("150.00"))).isEqualByComparingTo("150.00");
	}

	@Test
	void desetOdstotkovPopustaOdPetegaRacunaNaprej() {
		when(racuni.countByNalogVoziloLastnikId(stranka.getId())).thenReturn(4L);

		assertThat(popusti.uporabi(stranka, new BigDecimal("150.00"))).isEqualByComparingTo("135.00");
	}
}
