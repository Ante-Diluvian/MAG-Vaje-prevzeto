package si.delavnica.api.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import si.delavnica.api.model.Uporabnik;
import si.delavnica.api.model.Uporabnik.Vloga;
import si.delavnica.api.repository.UporabnikRepository;

class UporabnikServiceTest {

	@Test
	void slovenskaStevilkaSPredponoPlus386() {
		assertThat(UporabnikService.slovenskiTelefon("+386 41 111-111")).isEqualTo("041111111");
	}

	@Test
	void slovenskaStevilkaSPredpono00386() {
		assertThat(UporabnikService.slovenskiTelefon("00386 40/222 333")).isEqualTo("040222333");
	}

	@Test
	void tujaStevilkaObdrziPredponoPrazenTelefonOstanePrazen() {
		assertThat(UporabnikService.slovenskiTelefon("+43 660 123 4567")).isEqualTo("+436601234567");
		assertThat(UporabnikService.slovenskiTelefon(null)).isNull();
	}

	@Test
	void ustvariShraniGesloKotBcryptZgoscenoVrednost() {
		var repozitorij = mock(UporabnikRepository.class);
		when(repozitorij.save(any(Uporabnik.class))).thenAnswer(klic -> klic.getArgument(0));
		var encoder = new BCryptPasswordEncoder();
		var storitev = new UporabnikService(repozitorij, encoder);
		var uporabnik = Podatki.uporabnik(Vloga.STRANKA);
		uporabnik.setGeslo("skrivnost");

		var shranjen = storitev.ustvari(uporabnik);

		assertThat(shranjen.getGeslo()).startsWith("$2").isNotEqualTo("skrivnost");
		assertThat(encoder.matches("skrivnost", shranjen.getGeslo())).isTrue();
	}
}
