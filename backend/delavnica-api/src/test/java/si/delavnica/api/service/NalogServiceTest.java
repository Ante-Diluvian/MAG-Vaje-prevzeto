package si.delavnica.api.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.server.ResponseStatusException;
import si.delavnica.api.dto.NalogDto;
import si.delavnica.api.model.DelovniNalog;
import si.delavnica.api.model.DelovniNalog.Stanje;
import si.delavnica.api.model.PostavkaNaloga;
import si.delavnica.api.model.Racun;
import si.delavnica.api.model.Uporabnik;
import si.delavnica.api.model.Uporabnik.Vloga;
import si.delavnica.api.repository.DelovniNalogRepository;
import si.delavnica.api.repository.PostavkaNalogaRepository;
import si.delavnica.api.repository.RacunRepository;
import si.delavnica.api.repository.RezervniDelRepository;
import si.delavnica.api.repository.UporabnikRepository;

// Poslovna pravila nalogov: odpis zaloge (F5), izdaja računa (F6) in odpiranje naloga (F3).
@ExtendWith(MockitoExtension.class)
class NalogServiceTest {

	@Mock
	private DelovniNalogRepository nalogi;
	@Mock
	private RezervniDelRepository deli;
	@Mock
	private PostavkaNalogaRepository postavke;
	@Mock
	private RacunRepository racuni;
	@Mock
	private UporabnikRepository uporabniki;
	@Mock
	private NalogDostop dostop;
	@Mock
	private PopustService popusti;

	@InjectMocks
	private NalogService storitev;

	private final Uporabnik stranka = Podatki.uporabnik(Vloga.STRANKA);
	private final Uporabnik mehanik = Podatki.uporabnik(Vloga.MEHANIK);
	private final Jwt zeton = Podatki.zeton(mehanik);

	@Test
	void dodajDelZmanjsaZalogoInShraniTrenutnoCenoDela() {
		var nalog = Podatki.nalog(Podatki.vozilo(stranka, "LJ 12-345"), mehanik, Stanje.V_DELU);
		var del = Podatki.del("10.50", 5, 2);
		when(dostop.poisci(nalog.getId(), zeton)).thenReturn(nalog);
		when(deli.findById(del.getId())).thenReturn(Optional.of(del));
		when(postavke.save(any(PostavkaNaloga.class))).thenAnswer(klic -> klic.getArgument(0));

		var postavka = storitev.dodajDel(nalog.getId(), del.getId(), 2, zeton);

		assertThat(del.getZaloga()).isEqualTo(3);
		assertThat(postavka.getNalog()).isSameAs(nalog);
		assertThat(postavka.getKolicina()).isEqualTo(2);
		assertThat(postavka.getCena()).isEqualByComparingTo("10.50");
	}

	@Test
	void dodajDelZavrneKolicinoVecjoOdZaloge() {
		var nalog = Podatki.nalog(Podatki.vozilo(stranka, "LJ 12-345"), mehanik, Stanje.V_DELU);
		var del = Podatki.del("10.50", 1, 2);
		when(dostop.poisci(nalog.getId(), zeton)).thenReturn(nalog);
		when(deli.findById(del.getId())).thenReturn(Optional.of(del));

		assertThatThrownBy(() -> storitev.dodajDel(nalog.getId(), del.getId(), 2, zeton))
				.isInstanceOfSatisfying(ResponseStatusException.class,
						napaka -> assertThat(napaka.getStatusCode()).isEqualTo(HttpStatus.CONFLICT));
		assertThat(del.getZaloga()).isEqualTo(1);
		verify(postavke, never()).save(any());
	}

	@Test
	void izdajRacunSestejeUreDelaInPostavkeTerNalogObracuna() {
		var nalog = Podatki.nalog(Podatki.vozilo(stranka, "LJ 12-345"), mehanik, Stanje.ZAKLJUCEN);
		nalog.setUreDela(new BigDecimal("2.5"));
		when(dostop.poisci(nalog.getId(), zeton)).thenReturn(nalog);
		when(postavke.findByNalogId(nalog.getId()))
				.thenReturn(List.of(Podatki.postavka(nalog, 2, "10.50"), Podatki.postavka(nalog, 1, "4.00")));
		when(popusti.uporabi(eq(stranka), any())).thenAnswer(klic -> klic.getArgument(1));
		when(racuni.save(any(Racun.class))).thenAnswer(klic -> klic.getArgument(0));

		var racun = storitev.izdajRacun(nalog.getId(), zeton);

		// 2,5 h × 50 € + 2 × 10,50 € + 1 × 4,00 €
		assertThat(racun.getZnesek()).isEqualByComparingTo("150.00");
		assertThat(racun.isPlacan()).isFalse();
		assertThat(nalog.getStanje()).isEqualTo(Stanje.OBRACUNAN);
		assertThat(nalog.getDatumZaprtja()).isEqualTo(LocalDate.now());
	}

	@Test
	void izdajRacunZavrneNalogKiNiZakljucen() {
		var nalog = Podatki.nalog(Podatki.vozilo(stranka, "LJ 12-345"), mehanik, Stanje.V_DELU);
		when(dostop.poisci(nalog.getId(), zeton)).thenReturn(nalog);

		assertThatThrownBy(() -> storitev.izdajRacun(nalog.getId(), zeton))
				.isInstanceOfSatisfying(ResponseStatusException.class,
						napaka -> assertThat(napaka.getStatusCode()).isEqualTo(HttpStatus.CONFLICT));
		assertThat(nalog.getStanje()).isEqualTo(Stanje.V_DELU);
		verify(racuni, never()).save(any());
	}

	@Test
	void ustvariZavrneDodelitevUporabnikuKiNiMehanik() {
		var nalog = Podatki.nalog(Podatki.vozilo(stranka, "LJ 12-345"), stranka, Stanje.ODPRT);
		when(uporabniki.findById(stranka.getId())).thenReturn(Optional.of(stranka));

		assertThatThrownBy(() -> storitev.ustvari(nalog))
				.isInstanceOfSatisfying(ResponseStatusException.class,
						napaka -> assertThat(napaka.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST));
		verify(nalogi, never()).save(any());
	}

	@Test
	void ustvariVednoNastaviStanjeOdprt() {
		var nalog = Podatki.nalog(Podatki.vozilo(stranka, "LJ 12-345"), mehanik, Stanje.ZAKLJUCEN);
		when(uporabniki.findById(mehanik.getId())).thenReturn(Optional.of(mehanik));
		when(nalogi.save(any(DelovniNalog.class))).thenAnswer(klic -> klic.getArgument(0));

		var shranjen = storitev.ustvari(nalog);

		assertThat(shranjen.getStanje()).isEqualTo(Stanje.ODPRT);
		assertThat(shranjen.getId()).isNull();
	}

	@Test
	void filterPoRegistrskiIsceDelNizaNeGledeNaVelikeCrke() {
		var iskani = Podatki.nalog(Podatki.vozilo(stranka, "LJ 12-345"), mehanik, Stanje.ODPRT);
		var drugi = Podatki.nalog(Podatki.vozilo(stranka, "MB 99-999"), mehanik, Stanje.ODPRT);
		when(dostop.vidni(zeton)).thenReturn(List.of(iskani, drugi));
		when(postavke.findByNalogId(iskani.getId())).thenReturn(List.of());

		var rezultat = storitev.getAll(null, " lj 12", null, zeton);

		assertThat(rezultat).extracting(NalogDto::registrska).containsExactly("LJ 12-345");
	}
}
