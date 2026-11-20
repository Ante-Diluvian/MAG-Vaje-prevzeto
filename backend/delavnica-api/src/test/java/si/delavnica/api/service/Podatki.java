package si.delavnica.api.service;

import java.math.BigDecimal;
import java.util.UUID;
import org.springframework.security.oauth2.jwt.Jwt;
import si.delavnica.api.model.DelovniNalog;
import si.delavnica.api.model.DelovniNalog.Stanje;
import si.delavnica.api.model.PostavkaNaloga;
import si.delavnica.api.model.RezervniDel;
import si.delavnica.api.model.Uporabnik;
import si.delavnica.api.model.Uporabnik.Vloga;
import si.delavnica.api.model.Vozilo;

// Testni podatki za enotske teste servisov (entitete z že nastavljenim ID-jem).
final class Podatki {

	private Podatki() {}

	static Uporabnik uporabnik(Vloga vloga) {
		var uporabnik = new Uporabnik();
		uporabnik.setId(UUID.randomUUID());
		uporabnik.setIme("Ime");
		uporabnik.setPriimek("Priimek");
		uporabnik.setVloga(vloga);
		return uporabnik;
	}

	static Vozilo vozilo(Uporabnik lastnik, String registrska) {
		var vozilo = new Vozilo();
		vozilo.setId(UUID.randomUUID());
		vozilo.setLastnik(lastnik);
		vozilo.setZnamka("VW");
		vozilo.setModel("Golf");
		vozilo.setLetnik(2015);
		vozilo.setRegistrska(registrska);
		return vozilo;
	}

	static DelovniNalog nalog(Vozilo vozilo, Uporabnik mehanik, Stanje stanje) {
		var nalog = new DelovniNalog();
		nalog.setId(UUID.randomUUID());
		nalog.setVozilo(vozilo);
		nalog.setMehanik(mehanik);
		nalog.setOpis("Ropot pri zaviranju");
		nalog.setStanje(stanje);
		return nalog;
	}

	static RezervniDel del(String cena, int zaloga, int minZaloga) {
		var del = new RezervniDel();
		del.setId(UUID.randomUUID());
		del.setSifra("D-" + zaloga);
		del.setNaziv("Zavorna ploščica");
		del.setCena(new BigDecimal(cena));
		del.setZaloga(zaloga);
		del.setMinZaloga(minZaloga);
		return del;
	}

	static PostavkaNaloga postavka(DelovniNalog nalog, int kolicina, String cena) {
		var postavka = new PostavkaNaloga();
		postavka.setNalog(nalog);
		postavka.setDel(del(cena, 10, 0));
		postavka.setKolicina(kolicina);
		postavka.setCena(new BigDecimal(cena));
		return postavka;
	}

	// JWT z enakimi zahtevki, kot jih izda PrijavaService (subject = ID, "vloga")
	static Jwt zeton(Uporabnik uporabnik) {
		return Jwt.withTokenValue("testni-zeton")
				.header("alg", "HS256")
				.subject(uporabnik.getId().toString())
				.claim("vloga", uporabnik.getVloga().name())
				.build();
	}
}
