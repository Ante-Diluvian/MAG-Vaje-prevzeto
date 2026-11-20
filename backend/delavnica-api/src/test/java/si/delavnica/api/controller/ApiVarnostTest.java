package si.delavnica.api.controller;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

// API končne točke s pravo varnostno konfiguracijo (SecurityConfig) in bazo H2 (profil "test").
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ApiVarnostTest {

	@Autowired
	private MockMvc mvc;

	// uporabnik z dano vlogo, kot ga ustvari JwtAuthenticationConverter iz zahtevka "vloga"
	private static JwtRequestPostProcessor prijavljen(String vloga) {
		return jwt().jwt(zeton -> zeton.subject(UUID.randomUUID().toString()).claim("vloga", vloga))
				.authorities(new SimpleGrantedAuthority("ROLE_" + vloga));
	}

	@Test
	void prijavaZNapacnimGeslomVrne401() throws Exception {
		mvc.perform(post("/api/prijava").contentType(MediaType.APPLICATION_JSON)
				.content("{\"email\":\"vodja@delavnica.si\",\"geslo\":\"napacno\"}"))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void prijavaZacetnegaVodjeVrneZeton() throws Exception {
		mvc.perform(post("/api/prijava").contentType(MediaType.APPLICATION_JSON)
				.content("{\"email\":\"vodja@delavnica.si\",\"geslo\":\"vodja123\"}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.token").isNotEmpty());
	}

	@Test
	void zahtevaBrezZetonaVrne401() throws Exception {
		mvc.perform(get("/api/nalogi")).andExpect(status().isUnauthorized());
	}

	@Test
	void mehanikNeMoreOdpretiNaloga() throws Exception {
		mvc.perform(post("/api/nalogi").with(prijavljen("MEHANIK"))
				.contentType(MediaType.APPLICATION_JSON).content("{}"))
				.andExpect(status().isForbidden());
	}

	@Test
	void strankaNeMoreVidetiPorocila() throws Exception {
		mvc.perform(get("/api/porocilo").with(prijavljen("STRANKA"))).andExpect(status().isForbidden());
	}

	@Test
	void vodjaVidiPorocilo() throws Exception {
		mvc.perform(get("/api/porocilo").with(prijavljen("VODJA")))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.prihodekPoMesecih").isArray())
				.andExpect(jsonPath("$.prodaniDeli").isArray());
	}

	@Test
	void corsDovoliZahtevoIzDovoljenegaIzvora() throws Exception {
		mvc.perform(options("/api/nalogi").header("Origin", "http://localhost:4200")
				.header("Access-Control-Request-Method", "GET")
				.header("Access-Control-Request-Headers", "authorization"))
				.andExpect(status().isOk())
				.andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:4200"));
	}

	@Test
	void corsZavrneZahtevoIzTujegaIzvora() throws Exception {
		mvc.perform(options("/api/nalogi").header("Origin", "https://tuja-stran.example")
				.header("Access-Control-Request-Method", "GET"))
				.andExpect(status().isForbidden());
	}
}
