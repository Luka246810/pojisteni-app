package vecera.projekt.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Form-backing DTO pro registraci uživatele.

 * Účel:
 * - svázání polí HTML formuláře s validací (Bean Validation),
 * - neslouží jako entita – pouze přenos hodnot ze vstupního formuláře.

 * Validace:
 * - username: povinné, délka 3–50,
 * - password: povinné, délka 4–48 (DEMO; v produkci by bylo víc),
 * - passwordAgain: kontrola shody s password se provádí v controlleru/service
 *   nebo pomocí custom class-level validátoru,
 * - email: volitelný; je-li vyplněn, musí mít validní formát.
 */
public class RegisterForm {

    /** Uživatelské jméno (unikátní). */
    @NotBlank(message = "Uživatelské jméno je povinné.")
    @Size(min = 3, max = 50, message = "Uživatelské jméno musí mít 3–50 znaků.")
    private String username;

    /** Heslo (v service uložit jako BCrypt hash). */
    @NotBlank(message = "Heslo je povinné.")
    @Size(min = 4, max = 48, message = "Heslo musí mít 4–48 znaků.")
    private String password;

    /** Potvrzení hesla (musí se rovnat {@code password}). */
    @NotBlank(message = "Zopakujte heslo.")
    private String passwordAgain;

    /** Volitelný e-mail; pokud je vyplněn, validuje se formát. */
    @Email(message = "Neplatný formát e-mailu.")
    private String email;

    // --- get/set ---
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getPasswordAgain() { return passwordAgain; }
    public void setPasswordAgain(String passwordAgain) { this.passwordAgain = passwordAgain; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}
