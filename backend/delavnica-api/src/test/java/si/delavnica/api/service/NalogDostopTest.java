package si.delavnica.api.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import si.delavnica.api.model.DelovniNalog.Stanje;
import si.delavnica.api.model.Uporabnik;
import si.delavnica.api.model.Uporabnik.Vloga;
import si.delavnica.api.repository.DelovniNalogRepository;

// Kdo vidi katere naloge: stranka svoje, mehanik dodeljene, vodja vse.
@ExtendWith(MockitoExtension.class)
class NalogDostopTest {

	@Mock
	private DelovniNalogRepository nalogi;

	@InjectMocks
	private NalogDostop dostop;

	private final Uporabnik mehanik = Podatki.uporabnik(Vloga.MEHANIK);

	@Test
	void strankaNeVidiNalogaTujegaVozila() {
		var lastnik = Podatki.uporabnik(Vloga.STRANKA);
		var druga = Podatki.uporabnik(Vloga.STRANKA);
		var nalog = Podatki.nalog(Podatki.vozilo(lastnik, "LJ 12-345"), mehanik, Stanje.ODPRT);
		when(nalogi.findById(nalog.getId())).thenReturn(Optional.of(nalog));

		assertThat(dostop.poisci(nalog.getId(), Podatki.zeton(lastnik))).isSameAs(nalog);
		assertThatThrownBy(() -> dostop.poisci(nalog.getId(), Podatki.zeton(druga)))
				.isInstanceOfSatisfying(ResponseStatusException.class,
						napaka -> assertThat(napaka.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND));
	}

	@Test
	void mehanikVidiSamoDodeljeneNaloge() {
		var nalog = Podatki.nalog(Podatki.vozilo(Podatki.uporabnik(Vloga.STRANKA), "LJ 12-345"), mehanik, Stanje.ODPRT);
		when(nalogi.findByMehanikId(mehanik.getId())).thenReturn(List.of(nalog));

		assertThat(dostop.vidni(Podatki.zeton(mehanik))).containsExactly(nalog);
		verify(nalogi, never()).findAll();
	}
}
