package vecera.projekt.service;

import vecera.projekt.entity.Pojisteny;
import vecera.projekt.entity.Uzivatel;
import vecera.projekt.repository.UzivatelRepo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;
import java.util.Optional;

/**
 * Aplikační logika pro uživatelský účet a navázaný profil pojištěnce.
 * <p>
 * Zodpovědnosti:
 * <ul>
 *   <li>Načtení uživatele dle username (read-only) a dohledání jeho profilu {@code Pojisteny}.</li>
 *   <li>Uložení profilu přihlášeného uživatele:
 *       <ul>
 *         <li>pokud profil ještě neexistuje → vytvoří se a propojí s účtem,</li>
 *         <li>pokud existuje → aktualizuje se.</li>
 *       </ul>
 *   </li>
 *   <li>Repozitáře se z controlleru nevolají přímo – orchestrace probíhá přes tuto službu.</li>
 * </ul>
 * Pozn.: Metody pro čtení jsou označeny {@code readOnly}, zápisy běží v běžné transakci.
 */
@Service
public class AccountService {

    private final UzivatelRepo uzivatelRepo;
    private final PojistenyService pojistenyService;

    public AccountService(UzivatelRepo uzivatelRepo, PojistenyService pojistenyService) {
        this.uzivatelRepo = uzivatelRepo;
        this.pojistenyService = pojistenyService;
    }

    /** Výsledek uložení profilu – vytvořen nový vs. aktualizován stávající. */
    public enum SaveResult { CREATED, UPDATED }

    /** Najde uživatele podle username (read-only). */
    @Transactional(readOnly = true)
    public Optional<Uzivatel> findUserByUsername(String username) {
        return uzivatelRepo.findByUsername(username);
    }

    /**
     * Vrátí profil pojištěnce přihlášeného uživatele.
     * Když není svázán žádný pojištěnec, vrací se prázdný objekt pro první vyplnění.
     */
    @Transactional(readOnly = true)
    public Pojisteny loadProfileForUser(String username) {
        Uzivatel u = uzivatelRepo.findByUsername(username)
                .orElseThrow(() -> new NoSuchElementException("Uživatel nenalezen: " + username));

        Integer pojistenyId = u.getPojistenyId();
        if (pojistenyId == null) {
            return new Pojisteny(); // prázdný formulář
        }
        return pojistenyService.getById(pojistenyId);
    }

    /**
     * Uloží profil pro přihlášeného uživatele.
     * - Pokud nemá přiřazeného pojištěnce, založí se nový a propojí se s účtem.
     * - Pokud má, provede se update existujícího.
     */
    @Transactional
    public SaveResult saveProfileForUser(String username, Pojisteny form) {
        Uzivatel u = uzivatelRepo.findByUsername(username)
                .orElseThrow(() -> new NoSuchElementException("Uživatel nenalezen: " + username));

        Integer pojistenyId = u.getPojistenyId();
        if (pojistenyId == null) {
            Pojisteny saved = pojistenyService.save(form);
            uzivatelRepo.linkPojistenyByUsername(saved.getId(), username);
            return SaveResult.CREATED;
        } else {
            form.setId(pojistenyId);
            pojistenyService.save(form);
            return SaveResult.UPDATED;
        }
    }
}
