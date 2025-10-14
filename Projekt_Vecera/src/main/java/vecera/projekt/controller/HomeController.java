package vecera.projekt.controller;

import vecera.projekt.service.SpravcePojistenych;
import org.springframework.ui.Model;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Domovská stránka a základní navigace.

 * Endpoints:
 * - GET /            → úvodní dashboard / homepage
 * - GET /o-aplikaci  → statická „O aplikaci“ stránka

 * Pozn.:
 * - Bez omezení přístupu (permitAll), pouze čtení šablon.
 */

@Controller
public class HomeController {

    private final SpravcePojistenych spravce;

    public HomeController(SpravcePojistenych spravce) {
        this.spravce = spravce;
    }

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("countPojistenci", spravce.vypisVsechny().size());
        model.addAttribute("countPojistky", spravce.vypisVsechnaPojisteni().size()); // pokud máš takovou metodu
        return "index";
    }
}