package vecera.projekt.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vecera.projekt.entity.Pojisteny;
import java.math.BigDecimal;
import java.util.List;

/**
 * Repozitář pro reportovací a agregační dotazy (read-only).

 * Účel:
 * - agregace a projekce pro dashboardy a exporty (CityCount, ClaimAgg, SeriesPoint, Snapshot),
 * - kombinace JPQL a native SQL podle potřeby výkonu a dostupných funkcí.

 * Pozn.:
 * - neslouží ke změnám dat; zapisovací logika je v běžných repozitářích,
 */

public interface ReportRepo extends JpaRepository<Pojisteny, Integer> {

    public interface LabelValue { String getLabel(); Long getValue(); }
    public interface MonthCountRow { String getPeriod(); Long getCount(); }
    public interface StavStatsRow { String getStav(); Long getPocet(); BigDecimal getSuma(); BigDecimal getPrumer(); }
    public interface MestoCountRow { String getMesto(); Long getPocet(); }

    // Snapshot
    @Query(value = "SELECT COUNT(*) FROM pojisteny", nativeQuery = true)
    long countPojistenych();

    @Query(value = """
        SELECT COUNT(*)
        FROM typ_pojisteni t
        WHERE (t.platnost_od IS NULL OR t.platnost_od <= CURRENT_DATE())
          AND (t.platnost_do IS NULL OR t.platnost_do >= CURRENT_DATE())
        """, nativeQuery = true)
    long countPojisteniAktivni();

    @Query(value = """
        SELECT COUNT(*)
        FROM typ_pojisteni t
        WHERE (t.platnost_do IS NOT NULL AND t.platnost_do < CURRENT_DATE())
        """, nativeQuery = true)
    long countPojisteniExpirovane();

    @Query(value = """
        SELECT COALESCE(SUM(u.skoda),0)
        FROM pojistna_udalost u
        WHERE YEAR(u.datum) = YEAR(CURRENT_DATE())
        """, nativeQuery = true)
    BigDecimal sumaSkodYTD();

    // Aktivní pojistky podle typu (label/value)
    @Query(value = """
        SELECT t.nazev AS label, COUNT(*) AS value
        FROM typ_pojisteni t
        WHERE (t.platnost_od IS NULL OR t.platnost_od <= CURRENT_DATE())
          AND (t.platnost_do IS NULL OR t.platnost_do >= CURRENT_DATE())
        GROUP BY t.nazev
        ORDER BY value DESC
        """, nativeQuery = true)
    List<LabelValue> aktivniTypy();

    // Měsíční trend nových pojistek
    @Query(value = """
        SELECT DATE_FORMAT(t.platnost_od, '%Y-%m') AS period,
               COUNT(*) AS count
        FROM typ_pojisteni t
        WHERE t.platnost_od IS NOT NULL
        GROUP BY DATE_FORMAT(t.platnost_od, '%Y-%m')
        ORDER BY period
        """, nativeQuery = true)
    List<MonthCountRow> mesicniNove();

    // Škody dle stavu
    @Query(value = """
        SELECT u.stav AS stav,
               COUNT(*) AS pocet,
               COALESCE(SUM(u.skoda),0) AS suma,
               COALESCE(AVG(u.skoda),0) AS prumer
        FROM pojistna_udalost u
        GROUP BY u.stav
        ORDER BY pocet DESC
        """, nativeQuery = true)
    List<StavStatsRow> skodyDleStavu();

    // Top města
    @Query(value = """
        SELECT p.mesto AS mesto, COUNT(*) AS pocet
        FROM pojisteny p
        WHERE p.mesto IS NOT NULL AND p.mesto <> ''
        GROUP BY p.mesto
        ORDER BY pocet DESC
        LIMIT :limit
        """, nativeQuery = true)
    List<MestoCountRow> topMesta(@Param("limit") int limit);

    // (volitelné) Události po rocích
    @Query(value = """
        SELECT YEAR(u.datum) AS label, COUNT(*) AS value
        FROM pojistna_udalost u
        GROUP BY YEAR(u.datum)
        ORDER BY label
        """, nativeQuery = true)
    List<LabelValue> claimsByYear();
}
