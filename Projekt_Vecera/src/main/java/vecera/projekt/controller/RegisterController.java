package vecera.projekt.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vecera.projekt.dto.RegisterForm;
import vecera.projekt.service.RegistrationService;

/**
 * Registrace nového uživatele a prvotní nastavení účtu.

 * Endpoints:
 * - GET  /register → registrační formulář
 * - POST /register → validace vstupů, uložení uživatele (BCrypt), přiřazení ROLE_USER

 * Pozn.:
 * - Cesty jsou permitAll.
 * - Duplicate username/e-mail řeší service vrstva, chyby se vrací jako flash/field errors.
 * - Po úspěchu redirect na /login s flash „Účet vytvořen…“.
 */

@Controller
@RequiredArgsConstructor
@RequestMapping("/register")
public class RegisterController {

    private final RegistrationService registrationService;

    @ModelAttribute("form")
    public RegisterForm form() {
        return new RegisterForm();
    }

    @GetMapping
    public String show(Model model) {
        return "account/register";
    }

    @PostMapping
    public String submit(@Valid @ModelAttribute("form") RegisterForm form,
                         BindingResult br,
                         RedirectAttributes ra) {

        // chyby z anotací (@NotBlank, @Size, @Email)
        if (br.hasErrors()) {
            return "account/register";
        }

        // pré-kontrola duplicity (lepší UX – hned uvidíš chybu u pole)
        if (registrationService.usernameExists(form.getUsername())) {
            br.rejectValue("username", "exists", "Uživatelské jméno je už obsazené.");
            return "account/register";
        }

        try {
            registrationService.register(form);
        } catch (IllegalArgumentException ex) {
            // rozlišíme běžné chyby – ideálně mapovat na konkrétní pole:
            String msg = ex.getMessage() == null ? "Registrace selhala." : ex.getMessage();
            // hrubé mapování:
            if (msg.toLowerCase().contains("hesl")) {
                br.rejectValue("password", "invalid", msg);
            } else if (msg.toLowerCase().contains("uživatelsk")) {
                br.rejectValue("username", "invalid", msg);
            } else {
                br.reject("global", msg);
            }
            return "account/register";
        }

        ra.addFlashAttribute("toastSuccess", "Účet byl vytvořen. Přihlas se.");
        return "redirect:/login";
    }
}
