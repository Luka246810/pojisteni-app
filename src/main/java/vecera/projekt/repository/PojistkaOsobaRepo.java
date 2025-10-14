package vecera.projekt.repository;

import vecera.projekt.entity.Pojisteny;
import vecera.projekt.projection.OsobaRoleView;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Repozitář nad vazební tabulkou osoba ↔ pojistka (bez JPA entity).
 *
 * Účel:
 * - nativní SQL nad tabulkou pojistka_osoba,
 * - projekce osob a jejich rolí (OsobaRoleView) pro UI,
 * - atomické přidání/odebrání osoby v pojistce.
 *
 * Pozn.:
 * - Generika odkazuje na existující entitu {@link Pojisteny}, aby Spring Data
 *   mohl bean zaregistrovat (sami tu entitu nepotřebujeme).
 */
public interface PojistkaOsobaRepo extends Repository<Pojisteny, Integer> {

    /** Seznam osob v pojistce včetně role (projekce pro UI). */
    @Query(value = """
        SELECT o.id       AS id,
               o.jmeno    AS jmeno,
               o.prijmeni AS prijmeni,
               po.role    AS role
        FROM pojistka_osoba po
        JOIN pojisteny o ON o.id = po.osoba_id
        WHERE po.pojistka_id = :pojistkaId
        ORDER BY o.prijmeni, o.jmeno
        """, nativeQuery = true)
    List<OsobaRoleView> findOsobyRoleByPojistkaId(@Param("pojistkaId") int pojistkaId);

    /** ID osob v pojistce pro danou roli (např. POJISTNIK / POJISTENY). */
    @Query(value = """
        SELECT osoba_id
        FROM pojistka_osoba
        WHERE pojistka_id = :pojistkaId AND role = :role
        """, nativeQuery = true)
    List<Integer> findOsobaIdsByPojistkaIdAndRole(@Param("pojistkaId") int pojistkaId,
                                                  @Param("role") String role);

    /** Přidá osobu do pojistky s danou rolí. Vrací počet ovlivněných řádků (0/1). */
    @Modifying
    @Transactional
    @Query(value = """
        INSERT INTO pojistka_osoba (pojistka_id, osoba_id, role)
        VALUES (:pojistkaId, :osobaId, :role)
        """, nativeQuery = true)
    int addOsobaToPojistka(@Param("pojistkaId") int pojistkaId,
                           @Param("osobaId") int osobaId,
                           @Param("role") String role);

    /** Odebere osobu z pojistky (konkrétní role). Vrací 0/1. */
    @Modifying
    @Transactional
    @Query(value = """
        DELETE FROM pojistka_osoba
        WHERE pojistka_id = :pojistkaId
          AND osoba_id    = :osobaId
          AND role        = :role
        """, nativeQuery = true)
    int removeOsobaFromPojistka(@Param("pojistkaId") int pojistkaId,
                                @Param("osobaId") int osobaId,
                                @Param("role") String role);

    /** Smaže všechny vazby pro danou pojistku. */
    @Modifying
    @Transactional
    @Query(value = "DELETE FROM pojistka_osoba WHERE pojistka_id = :pojistkaId", nativeQuery = true)
    int deleteByPojistkaId(@Param("pojistkaId") int pojistkaId);

    /** Ověří, že osoba je členem dané pojistky (libovolná role). */
    @Query(value = """
        SELECT EXISTS(
            SELECT 1 FROM pojistka_osoba
            WHERE pojistka_id = :pojistkaId AND osoba_id = :osobaId
        )
        """, nativeQuery = true)
    boolean existsMember(@Param("pojistkaId") int pojistkaId,
                         @Param("osobaId") int osobaId);
}
