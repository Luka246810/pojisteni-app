package vecera.projekt.controller;

import vecera.projekt.entity.Pojisteny;
import vecera.projekt.entity.PojistenyDetail;
import vecera.projekt.entity.RoleVPojistce;
import vecera.projekt.service.PojistnaUdalostService;
import vecera.projekt.service.PojistkaOsobaService;
import vecera.projekt.service.SpravcePojistenych;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

/**
 * Správa pojištěnců (CRUD) a jejich detailů.

 * Endpoints (typicky):
 * - GET  /pojistenci              → stránkovaný seznam (ADMIN)
 * - GET  /pojistenci/novy         → formulář pro vytvoření (ADMIN)
 * - POST /pojistenci/novy         → uložení nového záznamu (ADMIN)
 * - GET  /pojistenci/detail/{id}  → detail pojištěnce (USER/ADMIN; USER vidí jen svůj)
 * - GET  /pojistenci/edit/{id}    → formulář pro editaci (ADMIN)
 * - POST /pojistenci/edit/{id}    → uložení změn (ADMIN)
 * - POST /pojistenci/delete/{id}  → smazání (ADMIN)

 * Práva:
 * - ADMIN: plný CRUD
 * - USER: pouze čtení vlastního detailu (kontrola v service / @PreAuthorize)

 * Pozn.:
 * - Controller pouze orchestrace: validace + flash zprávy + redirecty.
 * - Byznys pravidla, vlastnictví a transakce řeší service vrstva.
 */


@Controller
@RequestMapping("/pojistenci")
public class PojistenyController {

    private final SpravcePojistenych spravce;
    private final PojistnaUdalostService udalostService;
    private final PojistkaOsobaService pojistkaOsobaService;

    public PojistenyController(SpravcePojistenych spravce,
                               PojistnaUdalostService udalostService,
                               PojistkaOsobaService pojistkaOsobaService) {
        this.spravce = spravce;
        this.udalostService = udalostService;
        this.pojistkaOsobaService = pojistkaOsobaService;
    }

    // seznam všech – jen ADMIN
    @GetMapping({"", "/"})
    @PreAuthorize("hasRole('ADMIN')")
    public String list(@RequestParam(value = "q", required = false) String q, Model model) {
        var data = (q == null || q.isBlank())
                ? spravce.vypisVsechny()
                : spravce.hledejPojisteneho(q);
        model.addAttribute("pojistenci", data);
        model.addAttribute("q", q);
        return "pojistenci/list";
    }

    // přidání – jen ADMIN
    @GetMapping("/novy")
    @PreAuthorize("hasRole('ADMIN')")
    public String novyForm(@RequestParam(value = "back", required = false) String back,
                           @RequestParam(value = "pojistkaId", required = false) Integer pojistkaId,
                           @RequestParam(value = "role", required = false) RoleVPojistce role,
                           Model model) {
        // ⬇️ použij bezparametrický konstruktor; defaulty si nastav ve formuláři
        Pojisteny p = new Pojisteny();
        p.setVek(30); // volitelně výchozí věk
        model.addAttribute("p", p);
        model.addAttribute("back", back);
        model.addAttribute("pojistkaId", pojistkaId);
        model.addAttribute("role", role);
        return "pojistenci/form";
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public String vytvor(@ModelAttribute("p") Pojisteny p,
                         @RequestParam(value = "pohlavi", required = false) String pohlavi,
                         @RequestParam(value = "back", required = false) String back,
                         @RequestParam(value = "pojistkaId", required = false) Integer pojistkaId,
                         @RequestParam(value = "role", required = false) RoleVPojistce role,
                         RedirectAttributes ra) {

        // ⬇️ JPA styl: uložíme celou entitu; vrátí se s ID
        Pojisteny saved = spravce.save(p);
        int id = saved.getId();

        ra.addFlashAttribute("toastSuccess", "Pojištěnec byl uložen.");
        ra.addFlashAttribute("pohlaviView", (pohlavi == null || pohlavi.isBlank()) ? "jine" : pohlavi.toLowerCase());

        // pokud přišel pojistkaId + role → rovnou navážeme
        if (pojistkaId != null && role != null) {
            pojistkaOsobaService.add(pojistkaId, id, role);
        }

        if (back != null) back = back.trim();
        if (back != null && !back.isEmpty() && back.startsWith("/") && !"/".equals(back)) {
            return "redirect:" + back;
        }
        ra.addAttribute("id", id);
        return "redirect:/pojistenci/detail/{id}";
    }

    // detail – ADMIN všechno, USER jen sám sebe
    @PreAuthorize("hasRole('ADMIN') or @sec.canSeePojisteny(authentication, #id)")
    @GetMapping("/detail/{id}")
    public String detail(@PathVariable int id, Model model) {
        Optional<PojistenyDetail> d = spravce.findDetail(id); // ⬅️ JPA varianta
        if (d.isEmpty()) return "redirect:/pojistenci";

        model.addAttribute("p", d.get().getPojisteny());
        model.addAttribute("pojistky", d.get().getPojistky());

        List<?> udalosti = udalostService.findByPojisteny(id);
        model.addAttribute("udalosti", udalosti);

        return "pojistenci/detail";
    }

    // editace – jen ADMIN
    @GetMapping("/edit/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String editForm(@PathVariable int id, Model model) {
        Optional<PojistenyDetail> d = spravce.findDetail(id); // ⬅️ JPA varianta
        if (d.isEmpty()) return "redirect:/pojistenci";
        model.addAttribute("p", d.get().getPojisteny());
        return "pojistenci/form";
    }

    @PostMapping("/edit/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String ulozEdit(@PathVariable int id,
                           @ModelAttribute("p") Pojisteny p,
                           @RequestParam(value = "pohlavi", required = false) String pohlavi,
                           RedirectAttributes ra) {
        p.setId(id);
        spravce.save(p); // ⬅️ jedna metoda pro create/update
        ra.addFlashAttribute("toastSuccess", "Změny byly uloženy.");
        ra.addFlashAttribute("pohlaviView", (pohlavi == null || pohlavi.isBlank()) ? "jine" : pohlavi.toLowerCase());
        return "redirect:/pojistenci/detail/" + id;
    }

    // mazání – jen ADMIN
    @PostMapping("/delete/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String smaz(@PathVariable int id, RedirectAttributes ra) {
        spravce.deleteById(id); // ⬅️ JPA varianta
        ra.addFlashAttribute("toastError", "Pojištěnec byl odstraněn.");
        return "redirect:/pojistenci";
    }
}
