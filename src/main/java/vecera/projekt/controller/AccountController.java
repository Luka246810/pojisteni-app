package vecera.projekt.controller;

import vecera.projekt.entity.Pojisteny;
import vecera.projekt.security.PrihlasenyUzivatel;
import vecera.projekt.service.AccountService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Uživatelský účet a profil.

 * Endpoints:
 * - GET /login → vlastní login stránka (templates/account/login.html)
 * - GET/POST /ucet/profil → správa profilu přihlášeného uživatele (ROLE_USER)
 * - GET /ucet/moje-pojisteni → bezpečný redirect na detail vlastního pojištěnce

 * Pozn.: Autorizace je zajištěna přes @PreAuthorize a SecurityConfig.
 */

@Controller
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    /* ===== Přihlášení =====
       Vrací custom login šablonu v templates/account/login.html
    */
    @GetMapping("/login")
    public String login() {
        return "account/login";
    }

    /* ===== Přesměrování kořene účtu na profil =====
       Dostupné pouze přihlášeným USER
    */
    @GetMapping("/ucet")
    @PreAuthorize("hasRole('USER')")
    public String ucetRoot() {
        return "redirect:/ucet/profil";
    }

    /* ===== Zobrazení profilu přihlášeného uživatele =====
       - Pokud není přihlášen, pošli ho na /login (SPRÁVNÝ redirect s úvodním '/')
       - Na stránce očekáváme model atribut "p" = Pojisteny (nový či existující)
    */
    @GetMapping("/ucet/profil")
    @PreAuthorize("hasRole('USER')")
    public String profil(Authentication auth, Model model, RedirectAttributes ra) {
        if (auth == null || !auth.isAuthenticated()) {
            ra.addFlashAttribute("toastError", "Nejste přihlášen.");
            return "redirect:/login"; // ← opraveno (dříve chyběl /)
        }
        try {
            Pojisteny p = accountService.loadProfileForUser(auth.getName());
            model.addAttribute("p", p);
            return "account/profil";
        } catch (Exception e) {
            ra.addFlashAttribute("toastError", "Nepodařilo se načíst profil: " + e.getMessage());
            return "redirect:/";
        }
    }

    /* ===== Uložení/aktualizace profilu =====
       - Transakce řeší servis
       - Po uložení vracíme zpět na profil s toastem
    */
    @PostMapping("/ucet/profil")
    @PreAuthorize("hasRole('USER')")
    public String uloz(Authentication auth,
                       @ModelAttribute("p") Pojisteny form,
                       RedirectAttributes ra) {

        if (auth == null || !auth.isAuthenticated()) {
            ra.addFlashAttribute("toastError", "Nepodařilo se uložit – uživatel není přihlášen.");
            return "redirect:/login"; // ← opraveno (dříve chyběl /)
        }

        try {
            AccountService.SaveResult res = accountService.saveProfileForUser(auth.getName(), form);
            if (res == AccountService.SaveResult.CREATED) {
                ra.addFlashAttribute("toastSuccess", "Profil byl vytvořen.");
            } else {
                ra.addFlashAttribute("toastSuccess", "Údaje byly uloženy.");
            }
            return "redirect:/ucet/profil";
        } catch (Exception e) {
            ra.addFlashAttribute("toastError", "Uložení selhalo: " + e.getMessage());
            return "redirect:/ucet/profil";
        }
    }

    /* ===== Bezpečný redirect na „Moje pojištění“ =====
       - Slouží pro tlačítko v profilu
       - Najde pojistenyId přihlášeného uživatele a přesměruje na detail
       - Zabrání 403, protože uživatel neleze naslepo na cizí /pojistenci/detail/{id}
    */
    @GetMapping("/ucet/moje-pojisteni")
    @PreAuthorize("hasRole('USER')")
    public String mojePojisteni(Authentication auth, RedirectAttributes ra) {
        if (auth == null || !auth.isAuthenticated()) {
            ra.addFlashAttribute("toastError", "Nejste přihlášen.");
            return "redirect:/login";
        }

        var uOpt = accountService.findUserByUsername(auth.getName());
        if (uOpt.isEmpty()) {
            ra.addFlashAttribute("toastError", "Uživatel nebyl nalezen.");
            return "redirect:/";
        }

        var u = uOpt.get();
        Integer pid = u.getPojistenyId();
        if (pid == null) {
            ra.addFlashAttribute("toastError", "Nejprve uložte svůj profil.");
            return "redirect:/ucet/profil";
        }

        return "redirect:/pojistenci/detail/" + pid;
    }

    /* ===== Pomocník pro případné použití custom principalu ===== */
    @SuppressWarnings("unused")
    private PrihlasenyUzivatel getPrincipal(Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) return null;
        Object p = auth.getPrincipal();
        return (p instanceof PrihlasenyUzivatel pu) ? pu : null;
    }
}
