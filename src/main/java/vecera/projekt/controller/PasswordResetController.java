package vecera.projekt.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import vecera.projekt.service.PasswordResetService;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Zapomenuté heslo / obnova hesla (DEMO varianta bez e-mailu).
 *
 * Flow:
 * - GET/POST /forgot-password  → generuje jednorázový token (TTL 30 min) a ukáže odkaz ve flash zprávě
 * - GET /reset-password?token  → formulář pro nové heslo
 * - POST /reset-password       → validace (min. délka, shoda), uložení (BCrypt) a zneplatnění tokenu
 *
 * Bezpečnost:
 * - Cesty jsou permitAll (veřejné), CSRF chrání POSTy (Thymeleaf hidden token)
 * - Z bezpečnostních důvodů nikde neprozrazuje existenci/neexistenci uživatele
 *
 * Pozn.:
 * - Tokeny se drží in-memory (ConcurrentHashMap) pouze pro DEMO.
 * - V produkci se posílá odkaz e-mailem a token se ukládá do DB.
 */

@Controller
public class PasswordResetController {

    private static final Logger log = LoggerFactory.getLogger(PasswordResetController.class);
    private static final Duration TOKEN_TTL = Duration.ofMinutes(30);

    // In-memory store tokenů (pro demo)
    private final Map<String, ResetToken> tokens = new ConcurrentHashMap<>();

    private final PasswordResetService passwordResetService;

    public PasswordResetController(PasswordResetService passwordResetService) {
        this.passwordResetService = passwordResetService;
    }

    /* ===== KROK 1: Zadání username ===== */

    @GetMapping("/forgot-password")
    public String forgotPasswordForm() {
        return "auth/forgot-password"; // šablona: očekávej input name="username"
    }

    @PostMapping("/forgot-password")
    public String processForgotPassword(@RequestParam("username") String username,
                                        RedirectAttributes ra) {
        String token = UUID.randomUUID().toString();
        tokens.put(token, new ResetToken(username, Instant.now().plus(TOKEN_TTL)));

        // ABSOLUTNÍ URL
        String link = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/reset-password")
                .queryParam("token", token)
                .toUriString();

        log.info("Password reset link for username='{}' -> {}", username, link);

        ra.addFlashAttribute("toastInfo", "Pokud uživatel existuje, vygeneroval jsem odkaz pro obnovu (demo níže).");
        ra.addFlashAttribute("resetLink", link);
        return "redirect:/login";
    }

    /* ===== KROK 2: Formulář pro nové heslo ===== */

    @GetMapping("/reset-password")
    public String resetPasswordForm(@RequestParam("token") String token,
                                    Model model,
                                    RedirectAttributes ra) {
        ResetToken rt = tokens.get(token);
        if (rt == null || rt.isExpired()) {
            ra.addFlashAttribute("toastError", "Odkaz pro obnovu je neplatný nebo vypršel.");
            return "redirect:/login";
        }
        model.addAttribute("token", token);
        return "auth/reset-password"; // šablona: input name="password", name="confirm"
    }

    @PostMapping("/reset-password")
    public String processResetPassword(@RequestParam("token") String token,
                                       @RequestParam("password") String password,
                                       @RequestParam("confirm") String confirm,
                                       RedirectAttributes ra) {
        ResetToken rt = tokens.get(token);
        if (rt == null || rt.isExpired()) {
            ra.addFlashAttribute("toastError", "Odkaz pro obnovu je neplatný nebo vypršel.");
            return "redirect:/login";
        }

        // základní validace
        if (password == null || password.length() < 4) {
            ra.addFlashAttribute("toastError", "Heslo musí mít alespoň 4 znaky.");
            return "redirect:/reset-password?token=" + token;
        }
        if (!password.equals(confirm)) {
            ra.addFlashAttribute("toastError", "Hesla se neshodují.");
            return "redirect:/reset-password?token=" + token;
        }

        boolean ok = passwordResetService.resetByUsername(rt.username(), password);

        // jednorázový token
        tokens.remove(token);

        if (!ok) {
            // Z bezpečnostních důvodů neprozrazujeme, zda user existuje.
            ra.addFlashAttribute("toastInfo", "Pokud uživatel existuje, heslo bylo změněno.");
        } else {
            ra.addFlashAttribute("toastSuccess", "Heslo bylo změněno. Přihlas se novým heslem.");
        }
        return "redirect:/login";
    }

    /* ===== Pomocná třída ===== */
    private record ResetToken(String username, Instant expiresAt) {
        boolean isExpired() { return Instant.now().isAfter(expiresAt); }
    }
}
