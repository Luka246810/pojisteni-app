package vecera.projekt.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vecera.projekt.entity.Pojisteny;
import vecera.projekt.entity.PojistenyDetail;
import vecera.projekt.entity.TypPojisteni;
import vecera.projekt.entity.RoleVPojistce;
import vecera.projekt.repository.PojistenyRepo;
import vecera.projekt.repository.TypPojisteniRepo;
import vecera.projekt.repository.PojistkaOsobaRepo;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Orchestrátor nad pojištěnými a jejich pojistkami.
 * <p>
 * Zodpovědnosti:
 * <ul>
 *   <li>CRUD nad {@link Pojisteny} včetně pomocných aliasů pro controllery.</li>
 *   <li>Hledání pojištěných: nejprve přesné ID, jinak „fulltext“ v paměti
 *       (jméno/příjmení/telefon/město) s českým řazením.</li>
 *   <li>CRUD nad {@link TypPojisteni} (vypsání, přidání, smazání, napojení na pojištěného).</li>
 *   <li>Správa vazeb v tabulce {@code pojistka_osoba} (přidání rolí POJISTENY/POJISTNIK) – přes repo.</li>
 * </ul>
 * Pozn.:
 * <ul>
 *   <li>Třída historicky sdružuje starší API (bridge metody) i nové aliasy, aby byl controller čistý.</li>
 *   <li>„Fulltext“ je záměrně v paměti kvůli jednoduchosti – pro větší dataset by šel přes JPQL/FTS.</li>
 * </ul>
 */
@Service
@Transactional
public class SpravcePojistenych {

    private final PojistenyRepo pojistenyRepo;
    private final TypPojisteniRepo typPojisteniRepo;
    private final PojistkaOsobaRepo pojistkaOsobaRepo;

    public SpravcePojistenych(PojistenyRepo pojistenyRepo,
                              TypPojisteniRepo typPojisteniRepo,
                              PojistkaOsobaRepo pojistkaOsobaRepo) {
        this.pojistenyRepo = pojistenyRepo;
        this.typPojisteniRepo = typPojisteniRepo;
        this.pojistkaOsobaRepo = pojistkaOsobaRepo;
    }

    // ===== POJIŠTĚNÍ (osoby) =================================================

    /** Seznam všech pojištěných. */
    public List<Pojisteny> vypisVsechny() {
        return pojistenyRepo.findAll();
    }

    /** Uprav existující pojištění. */
    public void upravPojisteni(TypPojisteni t) {
        typPojisteniRepo.save(t);
    }

    /** Hledání: ID (pokud je číslo) → jinak fulltext v paměti (jméno/příjmení/telefon/město). */
    public List<Pojisteny> hledejPojisteneho(String q) {
        if (q == null || q.isBlank()) return vypisVsechny();

        String trimmed = q.trim();
        String normalizedDigits = onlyDigits(trimmed); // pro telefon/ID

        // 1) přesné ID (pokud je dotaz jen číslo)
        if (!normalizedDigits.isEmpty() && normalizedDigits.equals(trimmed)) {
            try {
                int id = Integer.parseInt(normalizedDigits);
                return pojistenyRepo.findById(id).map(List::of).orElseGet(List::of);
            } catch (NumberFormatException ignore) { /* spadni na fulltext */ }
        }

        String needle = trimmed.toLowerCase();

        // 2) fulltext v paměti (bez JPQL) – jméno, příjmení, telefon (bez mezer/+/-), město
        return pojistenyRepo.findAll().stream()
                .filter(p -> {
                    String j = safeLower(p.getJmeno());
                    String pr = safeLower(p.getPrijmeni());
                    String m = safeLower(p.getMesto());
                    String telDigits = onlyDigits(p.getTelefon());

                    boolean matchName = j.contains(needle) || pr.contains(needle);
                    boolean matchCity = m.contains(needle);
                    boolean matchPhone = !normalizedDigits.isEmpty() && telDigits.contains(normalizedDigits);

                    return matchName || matchCity || matchPhone;
                })
                .sorted(java.util.Comparator
                        .comparing(Pojisteny::getPrijmeni, java.text.Collator.getInstance(new java.util.Locale("cs","CZ")))
                        .thenComparing(Pojisteny::getJmeno,   java.text.Collator.getInstance(new java.util.Locale("cs","CZ")))
                        .thenComparing(Pojisteny::getId))
                .toList();
    }

    /** Vrátí jen číslice z řetězce (null → ""). */
    private static String onlyDigits(String s) {
        if (s == null) return "";
        return s.replaceAll("[^0-9]", "");
    }

    /** lower-case s ošetřením null (null → ""). */
    private static String safeLower(String s) {
        return s == null ? "" : s.toLowerCase();
    }

    public Optional<Pojisteny> najdi(int id) {
        return pojistenyRepo.findById(id);
    }

    /** Ulož (create/update) pojištěného. */
    public void uloz(Pojisteny p) {
        pojistenyRepo.save(p);
    }

    /** Smaz pojištěného podle ID. */
    public void smazPojisteneho(int id) {
        pojistenyRepo.deleteById(id);
    }

    // --- BRIDGE (původní API volané starým kódem) ----------------------------

    /** Původní API: založ pojištěného a vrať jeho ID. */
    public int pridatPojisteneho(String jmeno, String prijmeni, Integer vek, String telefon) {
        Pojisteny p = new Pojisteny(jmeno, prijmeni, vek, telefon);
        pojistenyRepo.save(p);
        return p.getId();
    }

    /** Původní API: detail pojištěného (entita + pojistky). */
    public Optional<PojistenyDetail> detailPojisteneho(int id) {
        return pojistenyRepo.findById(id).map(p -> {
            List<TypPojisteni> pojistky = typPojisteniRepo.findByPojisteny_Id(id);
            return new PojistenyDetail(p, pojistky);
        });
    }

    /** Původní API: úprava pojištěného. */
    public void upravPojisteneho(Pojisteny p) {
        pojistenyRepo.save(p);
    }

    // --- NOVÉ aliasy pro controller (save/findDetail/deleteById) -------------

    /** Alias pro controller: vrátí uloženou entitu (s ID). */
    public Pojisteny save(Pojisteny p) {
        return pojistenyRepo.save(p);
    }

    /** Alias pro controller: detail pojištěného. */
    public Optional<PojistenyDetail> findDetail(int id) {
        return detailPojisteneho(id);
    }

    /** Alias pro controller: smaž pojištěného. */
    public void deleteById(int id) {
        smazPojisteneho(id);
    }

    // ===== POJISTKY ==========================================================

    /** Všechny pojistky (např. pro admin seznam). */
    public List<TypPojisteni> vypisVsechnaPojisteni() {
        return typPojisteniRepo.findAll();
    }

    /** Pojistky konkrétního pojištěného. */
    public List<TypPojisteni> pojistkyProPojisteneho(int pojistenyId) {
        return typPojisteniRepo.findByPojisteny_Id(pojistenyId);
    }

    public Optional<TypPojisteni> najdiPojisteni(int id) {
        return typPojisteniRepo.findById(id);
    }

    public void smazPojisteni(int id) {
        typPojisteniRepo.deleteById(id);
    }

    public int pocetPojistek() {
        return (int) typPojisteniRepo.count();
    }

    /**
     * Přidání pojistky k pojištěnci + zápis rolí do pojistka_osoba (pojištěný + default pojistník).
     */
    public int pridatPojistku(int pojistenyId, String nazev, double castka,
                              LocalDate platnostOd, LocalDate platnostDo) {

        Pojisteny p = pojistenyRepo.findById(pojistenyId)
                .orElseThrow(() -> new IllegalArgumentException("Pojištěný " + pojistenyId + " nenalezen"));

        TypPojisteni t = new TypPojisteni();
        t.setPojisteny(p);
        t.setNazev(nazev);
        t.setCastka(BigDecimal.valueOf(castka));
        t.setPlatnostOd(platnostOd);
        t.setPlatnostDo(platnostDo);

        TypPojisteni saved = typPojisteniRepo.save(t);

        if (pojistkaOsobaRepo != null) {
            pojistkaOsobaRepo.addOsobaToPojistka(saved.getId(), pojistenyId, RoleVPojistce.POJISTENY.name());
            pojistkaOsobaRepo.addOsobaToPojistka(saved.getId(), pojistenyId, RoleVPojistce.POJISTNIK.name());
        }
        return saved.getId();
    }

    /**
     * Overload: založ pojistku a případně nastav jiného pojistníka než je pojištěný.
     */
    public int pridatPojistku(int pojistenyId, Integer pojistnikId, String nazev, double castka,
                              LocalDate platnostOd, LocalDate platnostDo) {

        int newId = pridatPojistku(pojistenyId, nazev, castka, platnostOd, platnostDo);

        int realPojistnikId = (pojistnikId != null) ? pojistnikId : pojistenyId;
        if (pojistkaOsobaRepo != null && realPojistnikId != pojistenyId) {
            // případně bys mohl odstranit implicitního pojistníka
            // pojistkaOsobaRepo.removeOsobaFromPojistka(newId, pojistenyId, RoleVPojistce.POJISTNIK.name());
            pojistkaOsobaRepo.addOsobaToPojistka(newId, realPojistnikId, RoleVPojistce.POJISTNIK.name());
        }
        return newId;


    }
}
