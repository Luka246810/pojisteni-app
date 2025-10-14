package vecera.projekt.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vecera.projekt.entity.RoleVPojistce;
import vecera.projekt.repository.PojistkaOsobaRepo;

import java.util.List;

/**
 * Služba pro práci s vazbami osoba ↔ pojistka v tabulce {@code pojistka_osoba}.
 * <p>
 * Zodpovědnosti:
 * <ul>
 *   <li>Čtení projekcí „osoba + role“ pro danou pojistku (UI seznam účastníků).</li>
 *   <li>Vyhledání ID osob dle role (např. aktuální pojistník) pro business logiku.</li>
 *   <li>Přidávání/odebírání osob v pojistce s konkrétní rolí.</li>
 * </ul>
 * Pozn.: Repo je postavené na nativních SQL dotazech nad join tabulkou; entita joinu se nemapuje.
 */
@Service
@Transactional
public class PojistkaOsobaService {

    private final PojistkaOsobaRepo repo;

    public PojistkaOsobaService(PojistkaOsobaRepo repo) {
        this.repo = repo;
    }

    public java.util.List<vecera.projekt.projection.OsobaRoleView> findOsobyVPojistce(int pojistkaId) {
        return repo.findOsobyRoleByPojistkaId(pojistkaId);
    }

    public List<Integer> findOsobyIdsByPojistkaAndRole(int pojistkaId, RoleVPojistce role) {
        return repo.findOsobaIdsByPojistkaIdAndRole(pojistkaId, role.name());
    }

    public void add(int pojistkaId, int osobaId, RoleVPojistce role) {
        repo.addOsobaToPojistka(pojistkaId, osobaId, role.name());
    }

    public void remove(int pojistkaId, int osobaId, RoleVPojistce role) {
        repo.removeOsobaFromPojistka(pojistkaId, osobaId, role.name());
    }
}
