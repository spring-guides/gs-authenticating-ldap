package ldapauthentication;

import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class HomeController {

	@Secured("ROLE_DEVELOPERS")
	@RequestMapping("/")
	public @ResponseBody String index() {
		return "Welcome to the home page!";
	}
}
