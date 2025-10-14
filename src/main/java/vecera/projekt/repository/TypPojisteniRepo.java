package vecera.projekt.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vecera.projekt.entity.TypPojisteni;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repozitář pro entitu {@link TypPojisteni}.

 * Účel:
 * - CRUD nad pojistkami/typy pojištění,
 * - pomocné dotazy pro šablony (fetch join k pojištěnému),
 * - vyhledávání (fulltextově nad názvem/částkou/ID).
 */

public interface TypPojisteniRepo extends JpaRepository<TypPojisteni, Integer> {

    // seznam pojistek konkrétního pojištěného
    List<TypPojisteni> findByPojisteny_Id(Integer pojistenyId);

    // ===== DOPLNĚNO: fetch join kvůli šablonám =====

    // Seznam všech pojistek vč. navázaného pojištěného
    @Query("select t from TypPojisteni t join fetch t.pojisteny")
    List<TypPojisteni> findAllWithPojisteny();

    // Detail pojistky vč. navázaného pojištěného
    @Query("select t from TypPojisteni t join fetch t.pojisteny where t.id = :id")
    Optional<TypPojisteni> findByIdWithPojisteny(@Param("id") Integer id);

    // Vyhledávání + fetch join (aby v šabloně byl k dispozici t.pojisteny.*)
    @Query("""
        select t
        from TypPojisteni t
        join fetch t.pojisteny
        where lower(t.nazev) like lower(concat('%', :q, '%'))
           or cast(t.id as string) like concat('%', :q, '%')
           or cast(t.castka as string) like concat('%', :q, '%')
        order by t.id desc
        """)
    List<TypPojisteni> searchWithPojisteny(@Param("q") String q);
}
