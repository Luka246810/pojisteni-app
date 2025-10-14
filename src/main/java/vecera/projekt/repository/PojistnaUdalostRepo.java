package vecera.projekt.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vecera.projekt.entity.PojistnaUdalost;

import java.time.LocalDate;
import java.util.List;

/**
 * Spring Data JPA repozitář pro entitu {@link PojistnaUdalost}.
 *
 * Účel:
 * - CRUD nad pojistnými událostmi,
 * - přehledy/filtrace pro UI (řazení, vyhledávání),
 * - cílené dotazy podle typu pojistky apod.
 */
public interface PojistnaUdalostRepo extends JpaRepository<PojistnaUdalost, Integer> {

    void deleteByTypPojisteniId(Integer typPojisteniId);

    // přehledy – seřazené novější první
    List<PojistnaUdalost> findAllByOrderByDatumDescIdDesc();

    List<PojistnaUdalost> findByPojisteny_IdOrderByDatumDescIdDesc(Integer pojistenyId);

    // alias pro controller (pojistkaId → události)
    List<PojistnaUdalost> findByTypPojisteni_IdOrderByDatumDesc(Integer typPojisteniId);

    // fulltext v popisu (case-insensitive)
    @Query("""
           select u
           from PojistnaUdalost u
           where lower(u.popis) like lower(concat('%', :q, '%'))
           order by u.datum desc, u.id desc
           """)
    List<PojistnaUdalost> searchByText(@Param("q") String q);

    @Query("""
       select u
       from PojistnaUdalost u
       where u.typPojisteni.id = :id
       order by u.datum desc, u.id desc
       """)
    List<PojistnaUdalost> findByTypPojisteniIdOrderByDatumDesc(@Param("id") int id);

    /** Přesný den (datum je LocalDate). */
    @Query("""
        select u
        from PojistnaUdalost u
        where u.datum = :d
        order by u.datum desc, u.id desc
        """)
    List<PojistnaUdalost> findByDay(@Param("d") LocalDate d);

    /** Interval dat (typicky měsíc): <from, to). */
    @Query("""
        select u
        from PojistnaUdalost u
        where u.datum >= :from and u.datum < :to
        order by u.datum desc, u.id desc
        """)
    List<PojistnaUdalost> findByDateRange(@Param("from") LocalDate from,
                                          @Param("to")   LocalDate to);
}
