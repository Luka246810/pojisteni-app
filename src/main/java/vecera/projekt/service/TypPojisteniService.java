package vecera.projekt.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vecera.projekt.entity.Pojisteny;
import vecera.projekt.entity.TypPojisteni;
import vecera.projekt.repository.PojistenyRepo;
import vecera.projekt.repository.TypPojisteniRepo;

import java.util.List;

/**
 * Tenká servisní vrstva pro práci s typy pojištění / pojistkami.
 * <p>
 * Zodpovědnosti:
 * <ul>
 *   <li>CRUD nad entitou {@link TypPojisteni} a napojení na {@link Pojisteny} při vytváření.</li>
 *   <li>Jednoduché čtecí metody pro controller – včetně variant s eager vazbou na pojištěného
 *       (přes repo metody s {@code join fetch}).</li>
 * </ul>
 */
@Service
@Transactional
public class TypPojisteniService {

    private final TypPojisteniRepo repo;
    private final PojistenyRepo pojistenyRepo;

    public TypPojisteniService(TypPojisteniRepo repo, PojistenyRepo pojistenyRepo) {
        this.repo = repo;
        this.pojistenyRepo = pojistenyRepo;
    }

    public List<TypPojisteni> findByPojisteny(Integer pojistenyId) {
        return repo.findByPojisteny_Id(pojistenyId);
    }

    public TypPojisteni getById(Integer id) {
        return repo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Pojistka " + id + " nenalezena"));
    }

    public TypPojisteni createForPojisteny(Integer pojistenyId, TypPojisteni pojistka) {
        Pojisteny p = pojistenyRepo.findById(pojistenyId)
                .orElseThrow(() -> new IllegalArgumentException("Pojištěný " + pojistenyId + " nenalezen"));
        pojistka.setPojisteny(p);
        return repo.save(pojistka);
    }

    public TypPojisteni save(TypPojisteni pojistka) {
        return repo.save(pojistka);
    }

    public void delete(Integer id) {
        repo.deleteById(id);
    }

    // nově:
    public List<TypPojisteni> findAll() {
        return repo.findAllWithPojisteny(); // ⬅ join fetch, ať je dostupný t.pojisteny.* v šablonách
    }
    public List<TypPojisteni> search(String q) {
        return repo.searchWithPojisteny(q); // ⬅ přejmenovaná metoda v Repu
    }
}
