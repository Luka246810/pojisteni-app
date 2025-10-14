package vecera.projekt.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vecera.projekt.entity.Pojisteny;
import vecera.projekt.repository.PojistenyRepo;

import java.util.List;

/**
 * Aplikační logika nad entitou {@code Pojisteny} (správa pojištěnců).
 * <p>
 * Zodpovědnosti:
 * <ul>
 *   <li>Jednoduché CRUD operace nad pojištěnými přes {@link PojistenyRepo}.</li>
 *   <li>Vyhození srozumitelné výjimky při nenalezení záznamu v metodě {@code getById}.</li>
 * </ul>
 */

@Service
@Transactional
public class PojistenyService {

    private final PojistenyRepo repo;

    public PojistenyService(PojistenyRepo repo) {
        this.repo = repo;
    }

    public List<Pojisteny> findAll() {
        return repo.findAll();
    }

    public Pojisteny getById(Integer id) {
        return repo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Pojištěný " + id + " nenalezen"));
    }

    public Pojisteny save(Pojisteny p) {
        return repo.save(p);
    }

    public void delete(Integer id) {
        repo.deleteById(id);
    }
}
