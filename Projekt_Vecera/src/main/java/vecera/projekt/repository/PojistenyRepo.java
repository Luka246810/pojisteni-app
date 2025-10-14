package vecera.projekt.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vecera.projekt.entity.Pojisteny;

import java.util.List;

/**
 * Spring Data JPA repozitář pro entitu {@link Pojisteny}.

 * Účel:
 * - standardní CRUD a odvozené dotazy nad pojištěnými,
 * - fulltextové vyhledávání podle ID / jména / příjmení.
 */

public interface PojistenyRepo extends JpaRepository<Pojisteny, Integer> {

    List<Pojisteny> findByMestoIgnoreCase(String mesto);

    List<Pojisteny> findByPrijmeniIgnoreCaseOrderByJmenoAsc(String prijmeni);

    /**
     * Fulltextové vyhledávání (bez telefonu — přidáme až potvrdíme pole).
     */
    @Query("""
        select p
        from Pojisteny p
        where
               cast(p.id as string) like concat('%', :q, '%')
            or lower(p.jmeno)       like lower(concat('%', :q, '%'))
            or lower(p.prijmeni)    like lower(concat('%', :q, '%'))
        order by p.prijmeni, p.jmeno, p.id
        """)
    List<Pojisteny> search(@Param("q") String q);
}
