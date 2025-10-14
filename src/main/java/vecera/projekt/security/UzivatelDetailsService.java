package vecera.projekt.security;

import vecera.projekt.entity.Uzivatel;
import vecera.projekt.repository.UzivatelRepo;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

/**
 * Načítá uživatele pro autentizaci podle uživatelského jména.

 * - Login je case-insensitive (používá {@code findByUsernameIgnoreCase}).
 * - Vrací {@link PrihlasenyUzivatel}, který adaptuje entitu {@link Uzivatel}.
 */

@Service
public class UzivatelDetailsService implements UserDetailsService {

    private final UzivatelRepo uzivatelRepo;

    public UzivatelDetailsService(UzivatelRepo uzivatelRepo) {
        this.uzivatelRepo = uzivatelRepo;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        String u = username == null ? "" : username.trim();
        Uzivatel ent = uzivatelRepo.findByUsernameIgnoreCase(u)
                .orElseThrow(() -> new UsernameNotFoundException("Uživatel neexistuje: " + u));
        return new PrihlasenyUzivatel(ent);
    }
}
