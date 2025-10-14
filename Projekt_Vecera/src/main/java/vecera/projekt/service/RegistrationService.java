package vecera.projekt.service;

import vecera.projekt.entity.Uzivatel;
import vecera.projekt.repository.UzivatelRepo;
import vecera.projekt.dto.RegisterForm;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Registrace nového uživatele (uživatelský účet + základní role).
 * <p>
 * Zodpovědnosti:
 * <ul>
 *   <li>Validace vstupu (username, heslo, opakování hesla) a kontrola duplicity uživatelského jména.</li>
 *   <li>Hashování hesla přes {@link PasswordEncoder} a uložení uživatele s rolí {@code ROLE_USER}.</li>
 *   <li>Převod databázových konfliktů (unique username) na srozumitelnou výjimku pro UI.</li>
 * </ul>
 * Pozn.: V entitě můžeš mít pole pro e-mail; zde stačí případně doplnit {@code u.setEmail(...)}.
 */
@Service
public class RegistrationService {

    private final UzivatelRepo uzivatelRepo;
    private final PasswordEncoder encoder;

    public RegistrationService(UzivatelRepo uzivatelRepo, PasswordEncoder encoder) {
        this.uzivatelRepo = uzivatelRepo;
        this.encoder = encoder;
    }

    /** Case-insensitive kontrola existence uživatele. */
    public boolean usernameExists(String username) {
        if (username == null) return false;
        return uzivatelRepo.existsByUsernameIgnoreCase(username.trim());
    }

    /**
     * Vytvoří nového uživatele s ROLE_USER a BCrypt heslem.
     * Vyhazuje IllegalArgumentException na běžné validační chyby
     * a DataIntegrityViolationException (přemapované) při unikátním konfliktu.
     */
    @Transactional
    public void register(RegisterForm f) {
        if (f == null) throw new IllegalArgumentException("Formulář je prázdný.");
        String username = safeTrim(f.getUsername());
        String password = safeTrim(f.getPassword());
        String passwordAgain = safeTrim(f.getPasswordAgain());

        if (username == null || username.length() < 3) {
            throw new IllegalArgumentException("Uživatelské jméno je příliš krátké.");
        }
        if (password == null || password.length() < 4) {
            throw new IllegalArgumentException("Heslo je příliš krátké (min. 4 znaky).");
        }
        if (!password.equals(passwordAgain)) {
            throw new IllegalArgumentException("Hesla se neshodují.");
        }
        if (uzivatelRepo.existsByUsernameIgnoreCase(username)) {
            throw new IllegalArgumentException("Uživatelské jméno je už obsazené.");
        }

        Uzivatel u = new Uzivatel();
        u.setUsername(username);                      // necháme původní case; DB kontrolujeme ignore-case
        u.setPassword(encoder.encode(password));      // POZOR: setPassword, ne setPasswordHash
        u.getRoleNames().add("ROLE_USER");            // výchozí role
        u.setEnabled(true);
        // pokud máš v entitě email: u.setEmail(safeTrim(f.getEmail()));

        try {
            uzivatelRepo.save(u);
        } catch (DataIntegrityViolationException ex) {
            // fallback na případ, kdy DB má unique index na username
            throw new IllegalArgumentException("Uživatelské jméno je obsazené.", ex);
        }
    }

    private static String safeTrim(String s) {
        return s == null ? null : s.trim();
    }
}
