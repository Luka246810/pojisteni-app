package vecera.projekt.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vecera.projekt.entity.PojistnaUdalost;
import vecera.projekt.entity.TypPojisteni;
import vecera.projekt.repository.PojistkaOsobaRepo;
import vecera.projekt.repository.PojistnaUdalostRepo;
import vecera.projekt.repository.PojistenyRepo;
import vecera.projekt.repository.TypPojisteniRepo;
import vecera.projekt.projection.OsobaRoleView;

import java.util.List;

/**
 * Aplikační logika pro „pojistky“ (entita {@link TypPojisteni}) a jejich vazby.
 * <p>
 * Zodpovědnosti:
 * <ul>
 *   <li>Výpis a vyhledávání pojistek (včetně eager načtení pojištěného pro UI).</li>
 *   <li>Detail pojistky: data pojistky + osoby v pojistce (projekce) + související události.</li>
 *   <li>CRUD nad pojistkou včetně bezpečného přidávání/odebírání osob do M:N tabulky {@code pojistka_osoba}.</li>
 *   <li>Mazání pojistky včetně kaskády pomocí repozitářů (vazby a události před smazáním typu).</li>
 * </ul>
 * Pozn.: Třída je transakční; čtecí operace jsou read-only, zápisy používají RW transakce.
 */

@Service
@RequiredArgsConstructor
public class PojistkaService {

    private final TypPojisteniRepo typPojisteniRepo;
    private final PojistenyRepo pojistenyRepo;
    private final PojistkaOsobaRepo pojistkaOsobaRepo;
    private final PojistnaUdalostRepo pojistnaUdalostRepo;

    /* ===== ČTENÍ ===== */

    @Transactional(readOnly = true)
    public List<TypPojisteni> list(String q) {
        if (q == null || q.isBlank()) {
            return typPojisteniRepo.findAllWithPojisteny();
        }
        return typPojisteniRepo.searchWithPojisteny(q.trim());
    }

    /** Kompozit pro detail/edit obrazovku. */
    public record Detail(
            TypPojisteni pojistka,
            List<OsobaRoleView> osoby,             // projekce osob s rolí
            List<PojistnaUdalost> udalosti
    ) {}

    @Transactional(readOnly = true)
    public Detail getDetail(int pojistkaId) {
        TypPojisteni t = typPojisteniRepo.findByIdWithPojisteny(pojistkaId)
                .orElseThrow(() -> new IllegalArgumentException("Pojistka nenalezena: " + pojistkaId));
        var osoby = pojistkaOsobaRepo.findOsobyRoleByPojistkaId(pojistkaId);
        var udalosti = pojistnaUdalostRepo.findByTypPojisteniIdOrderByDatumDesc(pojistkaId);
        return new Detail(t, osoby, udalosti);
    }

    /* ===== ZÁPIS ===== */

    @Transactional
    public TypPojisteni createForPojisteny(int pojistenyId, TypPojisteni t) {
        var p = pojistenyRepo.findById(pojistenyId)
                .orElseThrow(() -> new IllegalArgumentException("Pojištěný " + pojistenyId + " nenalezen"));
        t.setPojisteny(p);
        return typPojisteniRepo.save(t);
    }

    /**
     * Uloží edit pojistky a volitelně nastaví nového pojistníka (role 'POJISTNIK').
     * Pokud v {@code t} není nastaven pojištěný, načte se původní vazba z DB.
     */
    @Transactional
    public void saveEdit(int pojistkaId, Integer pojistnikId, TypPojisteni t) {
        t.setId(pojistkaId);

        if (t.getPojisteny() == null) {
            var exist = typPojisteniRepo.findByIdWithPojisteny(pojistkaId)
                    .orElseThrow(() -> new IllegalArgumentException("Pojistka nenalezena: " + pojistkaId));
            t.setPojisteny(exist.getPojisteny());
        }
        typPojisteniRepo.save(t);

        if (pojistnikId != null) {
            var current = pojistkaOsobaRepo.findOsobaIdsByPojistkaIdAndRole(pojistkaId, "POJISTNIK");
            for (Integer osId : current) {
                pojistkaOsobaRepo.removeOsobaFromPojistka(pojistkaId, osId, "POJISTNIK");
            }
            pojistkaOsobaRepo.addOsobaToPojistka(pojistkaId, pojistnikId, "POJISTNIK");
        }
    }

    @Transactional
    public void addOsoba(int pojistkaId, int osobaId, String role) {
        pojistkaOsobaRepo.addOsobaToPojistka(pojistkaId, osobaId, role);
    }

    @Transactional
    public void removeOsoba(int pojistkaId, int osobaId, String role) {
        pojistkaOsobaRepo.removeOsobaFromPojistka(pojistkaId, osobaId, role);
    }

    @Transactional
    public void deletePojistka(int pojistkaId) {
        pojistkaOsobaRepo.deleteByPojistkaId(pojistkaId);
        pojistnaUdalostRepo.deleteByTypPojisteniId(pojistkaId);
        typPojisteniRepo.deleteById(pojistkaId);
    }
}
