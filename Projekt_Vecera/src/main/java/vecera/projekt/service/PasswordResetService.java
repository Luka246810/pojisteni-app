package vecera.projekt.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vecera.projekt.repository.UzivatelRepo;

/**
 * Služba pro bezpečné nastavení nového hesla (reset) uživateli.
 * <p>
 * Zodpovědnosti:
 * <ul>
 *   <li>Vyhledání uživatele dle username (case-sensitive/insensitive dle repozitáře) a nastavení nového hesla.</li>
 *   <li>Hashování hesla přes {@link PasswordEncoder} (např. bcrypt v rámci delegujícího encoderu).</li>
 *   <li>Neprozrazuje UI, zda uživatel existuje – vrací pouze boolean (vhodné pro neutrální flash zprávy).</li>
 * </ul>
 * Pozn.: Spoléhá na JPA „dirty checking“ – není nutné explicitně volat {@code save()} po změně entity.
 */
@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private final UzivatelRepo uzivatelRepo;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public boolean resetByUsername(String username, String newRawPassword) {
        return uzivatelRepo.findByUsername(username)
                .map(u -> {
                    u.setPasswordHash(passwordEncoder.encode(newRawPassword));
                    // JPA dirty checking → save() netřeba
                    return true;
                })
                .orElse(false);
    }
}
