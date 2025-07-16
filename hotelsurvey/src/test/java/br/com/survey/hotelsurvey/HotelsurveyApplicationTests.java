package br.com.survey.hotelsurvey;

import br.com.survey.hotelsurvey.security.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@SpringBootTest
@Import(SecurityConfig.class)
class HotelsurveyApplicationTests {

	@Test
	void contextLoads() {
	}

}
