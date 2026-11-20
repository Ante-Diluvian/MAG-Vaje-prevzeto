package si.delavnica.api;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;
import si.delavnica.api.model.Uporabnik;
import si.delavnica.api.model.Uporabnik.Vloga;
import si.delavnica.api.repository.UporabnikRepository;

@SpringBootApplication
public class DelavnicaApiApplication {

	public static void main(String[] args) {
		System.out.println("PRISTANICNIK dela: api se zaganja");
		SpringApplication.run(DelavnicaApiApplication.class, args);
	}

	// geslo pride iz zacetni-vodja.geslo (ZACETNI_VODJA_GESLO), da na javnem strežniku ni znano
	@Bean
	CommandLineRunner zacetniVodja(UporabnikRepository uporabniki, PasswordEncoder encoder,
			@Value("${zacetni-vodja.geslo}") String geslo) {
		return args -> {
			if (uporabniki.findByEmail("vodja@delavnica.si").isEmpty()) {
				var uporabnik = new Uporabnik();
				uporabnik.setEmail("vodja@delavnica.si");
				uporabnik.setGeslo(encoder.encode(geslo));
				uporabnik.setIme("Vodja");
				uporabnik.setPriimek("Delavnice");
				uporabnik.setVloga(Vloga.VODJA);
				uporabniki.save(uporabnik);
			}
		};
	}

}
