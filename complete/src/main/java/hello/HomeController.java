package hello;

import java.security.Principal;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {

    @RequestMapping("/")
    public String index() {
        return "Welcome to the home page!";
    }

    @RequestMapping("/home")
    public String home(Principal user) {
        return "Welcome to your home page, " + user.getName() + "!";
    }
}
