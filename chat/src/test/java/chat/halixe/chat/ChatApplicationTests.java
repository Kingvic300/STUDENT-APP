package chat.halixe.chat;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@Disabled("Requires a live database and Redis — not available in unit test runs")
class ChatApplicationTests {

	@Test
	void contextLoads() {
	}

}
