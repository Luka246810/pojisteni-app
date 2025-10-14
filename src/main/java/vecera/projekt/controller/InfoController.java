package vecera.projekt.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Statické informační stránky aplikace (např. O aplikaci).

 * Endpoints:
 * - GET /o-aplikaci  → informační stránka s popisem projektu

 * Pozn.:
 * - Pouze čtení šablon, bez byznys logiky a bez závislosti na DB.
 */


@Controller
public class InfoController {

    @GetMapping("/o-aplikaci")
    public String about() {
        return "about";
    }
}
