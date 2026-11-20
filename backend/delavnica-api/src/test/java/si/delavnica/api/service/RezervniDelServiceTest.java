package si.delavnica.api.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import si.delavnica.api.dto.PredlogNarocilaDto;
import si.delavnica.api.repository.RezervniDelRepository;

// Predlog naročila (F12) in naročanje delov (F9).
@ExtendWith(MockitoExtension.class)
class RezervniDelServiceTest {

	@Mock
	private RezervniDelRepository deli;

	@InjectMocks
	private RezervniDelService storitev;

	@Test
	void predlogNarocilaIzracunaManjkajocoKolicino() {
		var del = Podatki.del("10.50", 1, 4);
		when(deli.findPodMinimalnoZalogo()).thenReturn(List.of(del));

		assertThat(storitev.predlogNarocila()).extracting(PredlogNarocilaDto::manjka).containsExactly(3);
	}

	@Test
	void narociloZKolicinoNicJeZavrnjeno() {
		assertThatThrownBy(() -> storitev.naroci(UUID.randomUUID(), 0))
				.isInstanceOfSatisfying(ResponseStatusException.class,
						napaka -> assertThat(napaka.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST));
		verify(deli, never()).povecajZalogo(any(), anyInt());
	}

	@Test
	void narociloNeobstojecegaDelaVrne404() {
		var id = UUID.randomUUID();
		when(deli.povecajZalogo(id, 5)).thenReturn(0);

		assertThatThrownBy(() -> storitev.naroci(id, 5))
				.isInstanceOfSatisfying(ResponseStatusException.class,
						napaka -> assertThat(napaka.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND));
	}
}
