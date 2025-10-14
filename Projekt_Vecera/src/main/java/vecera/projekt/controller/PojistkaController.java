package vecera.projekt.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import vecera.projekt.entity.TypPojisteni;
import vecera.projekt.service.PojistkaService;

/**
 * Správa pojistek (CRUD) včetně vazby osob a přehledů.

 * Endpoints (typicky):
 * - GET  /pojistky                 → seznam pojistek (ADMIN)
 * - GET  /pojistky/novy            → formulář pro vytvoření (ADMIN)
 * - POST /pojistky/novy            → uložení (ADMIN)
 * - GET  /pojistky/detail/{id}     → detail (USER/ADMIN; USER jen čtení své)
 * - GET  /pojistky/edit/{id}       → formulář pro editaci (ADMIN)
 * - POST /pojistky/edit/{id}       → uložení změn (ADMIN)
 * - POST /pojistky/delete/{id}     → smazání (ADMIN)
 * - POST /pojistky/pridat-k-pojistenci/{id} → přidání osoby/role (ADMIN)

 * Práva:
 * - ADMIN: plný CRUD + správa osob v pojistce
 * - USER: čtení vlastních detailů (bez editace, bez mazání)

 * Pozn.:
 * - Vazby pojistka ↔ osoby řeší service (kontrola duplicit, role).
 * - Při mazání pozor na kaskády (události apod.) – řeší se v entitách/service.
 */

@Controller
@RequiredArgsConstructor
@RequestMapping("/pojistky")
public class PojistkaController {

    private final PojistkaService pojistkaService;

    /** Seznam + vyhledávání */
    @GetMapping({"", "/"})
    public String list(@RequestParam(value = "q", required = false) String q, Model model) {
        var data = pojistkaService.list(q);
        model.addAttribute("pojistky", data);
        model.addAttribute("q", q);
        return "pojistky/list";
    }

    /** Detail (USER/ADMIN povolen v SecurityConfig) */
    @GetMapping("/detail/{id}")
    public String detail(@PathVariable int id, Model model) {
        var d = pojistkaService.getDetail(id);
        model.addAttribute("t", d.pojistka());
        model.addAttribute("osobyKpojistce", d.osoby());
        model.addAttribute("udalosti", d.udalosti());
        return "pojistky/detail";
    }

    /** Nový formulář k pojištěnci */
    @GetMapping("/novy-k-pojistenci/{pojistenyId}")
    public String novyForm(@PathVariable int pojistenyId, Model model) {
        model.addAttribute("t", new TypPojisteni());
        model.addAttribute("pojistenyId", pojistenyId);
        return "pojistky/form";
    }

    /** Vytvoření nové pojistky k pojištěnci */
    @PostMapping("/pridat-k-pojistenci/{pojistenyId}")
    public String vytvorKpojistenci(@PathVariable int pojistenyId,
                                    @ModelAttribute TypPojisteni t) {
        var saved = pojistkaService.createForPojisteny(pojistenyId, t);
        var pid = saved.getPojisteny() != null ? saved.getPojisteny().getId() : pojistenyId;
        return "redirect:/pojistenci/detail/" + pid;
    }

    /** Edit formulář */
    @GetMapping("/edit/{id}")
    public String edit(@PathVariable int id, Model model) {
        var d = pojistkaService.getDetail(id);
        model.addAttribute("t", d.pojistka());
        if (d.pojistka().getPojisteny() != null) {
            model.addAttribute("pojistenyId", d.pojistka().getPojisteny().getId());
        }
        model.addAttribute("osobyKpojistce", d.osoby());
        model.addAttribute("udalosti", d.udalosti());
        return "pojistky/form";
    }

    /**
     * Uložení editu pojistky.
     * Volitelně přijímá ?pojistnikId=... :
     *  - smaže stávající záznam(y) role 'POJISTNIK' a nastaví nového.
     */
    @PostMapping("/edit/{id}")
    public String ulozEdit(@PathVariable int id,
                           @RequestParam(value = "pojistnikId", required = false) Integer pojistnikId,
                           @ModelAttribute TypPojisteni t) {
        pojistkaService.saveEdit(id, pojistnikId, t);
        return "redirect:/pojistky/edit/" + id;
    }

    /** Přidat existující osobu k pojistce */
    @PostMapping("/{id}/pridat-osobu")
    public String pridatOsobuKPojsitce(@PathVariable int id,
                                       @RequestParam int osobaId,
                                       @RequestParam String role) {
        pojistkaService.addOsoba(id, osobaId, role);
        return "redirect:/pojistky/edit/" + id + "#osoby";
    }

    /** Odebrat osobu z pojistky */
    @PostMapping("/{id}/odebrat-osobu")
    public String odebratOsobuZPojistky(@PathVariable int id,
                                        @RequestParam int osobaId,
                                        @RequestParam String role) {
        pojistkaService.removeOsoba(id, osobaId, role);
        return "redirect:/pojistky/edit/" + id + "#osoby";
    }

    /** Smazání pojistky */
    @PostMapping("/delete/{id}")
    public String smaz(@PathVariable int id) {
        pojistkaService.deletePojistka(id);
        return "redirect:/pojistky";
    }
}
