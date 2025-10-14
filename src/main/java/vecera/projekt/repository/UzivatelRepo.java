package vecera.projekt.repository;

import vecera.projekt.entity.Uzivatel;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

/**
 * Spring Data JPA repozitář pro entitu {@link Uzivatel}.

 * Účel:
 * - vyhledání uživatele pro autentizaci (username, case-insensitive),
 * - práce s vazbou na profil pojištěného (pojistenyId).
 */

public interface UzivatelRepo extends JpaRepository<Uzivatel, Integer> {

    Optional<Uzivatel> findByUsername(String username);

    // volitelné: pro case-insensitive login (pozor na DB kolace)
    Optional<Uzivatel> findByUsernameIgnoreCase(String username);

    boolean existsByUsernameIgnoreCase(String username);

    @Modifying
    @Query("update Uzivatel u set u.pojistenyId = :pid where u.username = :uname")
    int linkPojistenyByUsername(@Param("pid") Integer pojistenyId,
                                @Param("uname") String username);
}
