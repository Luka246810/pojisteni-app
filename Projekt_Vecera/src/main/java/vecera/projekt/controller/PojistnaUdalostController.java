package vecera.projekt.controller;

import vecera.projekt.entity.PojistnaUdalost;
import vecera.projekt.security.PrihlasenyUzivatel;
import vecera.projekt.service.PojistnaUdalostService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import static org.springframework.http.HttpStatus.NOT_FOUND;

/**
 * Správa pojistných událostí (CRUD) navázaných na pojistky/osoby.

 * Endpoints (typicky):
 * - GET  /udalosti                  → seznam událostí (USER/ADMIN; USER filtrované na jeho)
 * - GET  /udalosti/novy             → formulář pro vytvoření (ADMIN*)
 * - POST /udalosti/novy             → uložení (ADMIN*)
 * - GET  /udalosti/detail/{id}      → detail události (USER/ADMIN; vlastnictví)
 * - GET  /udalosti/edit/{id}        → formulář pro editaci (ADMIN)
 * - POST /udalosti/edit/{id}        → uložení změn
 * - POST /udalosti/delete/{id}      → smazání

 * Práva:
 * - Detail/list dostupný USER/ADMIN (USER pouze své).
 * - Změny dle byznys pravidel (např. USER může zakládat jen k „své“ pojistce).

 * Pozn.:
 * - Stav události řeší enum (např. NOVA/RESENA/ZAMITNUTA).
 * - Service vrstva hlídá vlastnictví a přechody stavů.
 */

@Controller
@RequestMapping("/udalosti")
public class PojistnaUdalostController {

    private final PojistnaUdalostService udalostService;

    public PojistnaUdalostController(PojistnaUdalostService udalostService) {
        this.udalostService = udalostService;
    }

    // DETAIL – ADMIN vše, USER jen svoje (guard přes @sec)
    @GetMapping("/detail/{id}")
    @PreAuthorize("hasRole('ADMIN') or @sec.canSeeUdalost(authentication, #id)")
    public String detail(@PathVariable int id, Model model) {
        var u = udalostService.getById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND));
        model.addAttribute("u", u);
        return "udalosti/detail";
    }

    // NOVÁ – k danému pojištěnci
    @GetMapping("/novy-k-pojistenci/{pojistenyId}")
    @PreAuthorize("@sec.canEditPojisteny(authentication, #pojistenyId)") // admin nebo vlastník
    public String novyForm(@PathVariable int pojistenyId, Model model) {
        var u = new PojistnaUdalost();
        u.setPojistenyId(pojistenyId); // viz pomocná setter metoda v entitě níže
        model.addAttribute("u", u);
        return "udalosti/form";
    }

    // EDIT
    @GetMapping("/edit/{id}")
    @PreAuthorize("hasRole('ADMIN') or @sec.canEditUdalost(authentication, #id)")
    public String editForm(@PathVariable int id, Model model) {
        var u = udalostService.getById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND));
        model.addAttribute("u", u);
        return "udalosti/form";
    }

    // SAVE – create/update v jednom
    @PostMapping("/save")
    @PreAuthorize("@sec.canSaveUdalost(authentication, #u)")
    public String save(@ModelAttribute("u") PojistnaUdalost u, RedirectAttributes ra) {
        boolean nova = (u.getId() == null);
        var saved = udalostService.save(u);
        ra.addFlashAttribute("toastSuccess", nova ? "Událost byla založena." : "Událost byla upravena.");
        return "redirect:/udalosti/detail/" + saved.getId();
    }

    // DELETE
    @PostMapping("/delete/{id}")
    @PreAuthorize("hasRole('ADMIN') or @sec.canEditUdalost(authentication, #id)")
    public String delete(@PathVariable int id, RedirectAttributes ra) {
        var pojId = udalostService.getById(id)
                .map(PojistnaUdalost::getPojistenyId)
                .orElse(0);
        udalostService.deleteById(id);
        ra.addFlashAttribute("toastInfo", "Událost byla odstraněna.");
        return "redirect:/pojistenci/detail/" + pojId;
    }

    @GetMapping({"", "/"})
    @PreAuthorize("isAuthenticated()")
    public String list(@RequestParam(value = "q", required = false) String q,
                       Authentication auth,
                       Model model) {

        model.addAttribute("active", "udalosti");

        boolean admin = auth.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));

        if (admin) {
            var data = (q != null && !q.isBlank())
                    ? udalostService.search(q)
                    : udalostService.findAll();
            model.addAttribute("udalosti", data);
        } else {
            Integer myId = (auth.getPrincipal() instanceof PrihlasenyUzivatel up) ? up.getPojistenyId() : null;
            model.addAttribute("udalosti", (myId != null) ? udalostService.findByPojisteny(myId) : java.util.List.of());
        }

        model.addAttribute("q", q);
        return "udalosti/list";
    }
}
