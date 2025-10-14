package vecera.projekt.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vecera.projekt.entity.PojistnaUdalost;
import vecera.projekt.entity.Pojisteny;
import vecera.projekt.repository.PojistnaUdalostRepo;
import vecera.projekt.repository.PojistenyRepo;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * Aplikační logika pro pojistné události.
 * <p>
 * Zodpovědnosti:
 * <ul>
 *   <li>Čtení, ukládání a mazání událostí (orchestrace nad {@code PojistnaUdalostRepo}).</li>
 *   <li>Chytré vyhledávání podle data – umí rozlišit:
 *       <ul>
 *          <li>konkrétní den (D.M.Y / D-M-Y / D/M/Y / D M Y i ISO yyyy-MM-dd),</li>
 *          <li>měsíc (M/Y, M.Y, M-Y, M Y i Y-M, Y.M, Y M, ISO yyyy-MM),</li>
 *          <li>text v popisu (fallback).</li>
 *       </ul>
 *   </li>
 *   <li>Dosazení navázané entity {@code Pojisteny}, pokud přijde jen {@code pojistenyId} z formuláře.</li>
 * </ul>
 * Pozn.: Třída je transakční; read-only pro čtecí metody, zápisové metody běží v RW transakci.
 */
@Service
@Transactional
public class PojistnaUdalostService {

    private final PojistnaUdalostRepo repo;
    private final PojistenyRepo pojistenyRepo;

    public PojistnaUdalostService(PojistnaUdalostRepo repo, PojistenyRepo pojistenyRepo) {
        this.repo = repo;
        this.pojistenyRepo = pojistenyRepo;
    }

    @Transactional(readOnly = true)
    public Optional<PojistnaUdalost> getById(Integer id) {
        return repo.findById(id);
    }

    @Transactional(readOnly = true)
    public List<PojistnaUdalost> findAll() {
        return repo.findAllByOrderByDatumDescIdDesc();
    }

    @Transactional(readOnly = true)
    public List<PojistnaUdalost> findByPojisteny(Integer pojistenyId) {
        return repo.findByPojisteny_IdOrderByDatumDescIdDesc(pojistenyId);
    }

    /**
     * Hledání událostí: den → měsíc → text v popisu (case-insensitive).
     * Podporované formáty:
     *  - den: D.M.Y / D-M-Y / D/M/Y / D M Y i ISO "yyyy-MM-dd"
     *  - měsíc: M/Y, M.Y, M-Y, M Y i Y-M, Y.M, Y M, případně ISO "yyyy-MM"
     */
    @Transactional(readOnly = true)
    public List<PojistnaUdalost> search(String q) {
        if (q == null || q.isBlank()) {
            return repo.findAllByOrderByDatumDescIdDesc();
        }
        String s = q.trim();

        // 1) přesný den
        LocalDate day = tryParseDay(s);
        if (day != null) {
            return repo.findByDay(day);
        }

        // 2) měsíc
        YearMonth month = tryParseMonth(s);
        if (month != null) {
            LocalDate from = month.atDay(1);
            LocalDate to   = month.plusMonths(1).atDay(1); // <from, to)
            return repo.findByDateRange(from, to);
        }

        // 3) text v popisu
        return repo.searchByText(s);
    }

    public PojistnaUdalost save(PojistnaUdalost u) {
        // pokud přichází jen s pojistenyId (např. z formu), dosadíme entitu
        if (u.getPojisteny() == null && u.getPojistenyId() != null) {
            Pojisteny p = pojistenyRepo.findById(u.getPojistenyId())
                    .orElseThrow(() -> new IllegalArgumentException("Pojištěný " + u.getPojistenyId() + " nenalezen"));
            u.setPojisteny(p);
        }
        return repo.save(u);
    }

    public void deleteById(Integer id) {
        repo.deleteById(id);
    }

    /* ---------- helpers ---------- */

    /** Pokusí se naparsovat den z více formátů: D.M.Y / D-M-Y / D/M/Y / D M Y i ISO Y-M-D. */
    private static LocalDate tryParseDay(String s) {
        if (s == null) return null;
        String in = s.trim();

        // sjednotit oddělovače (., /, mezery) na '-'
        String norm = in.replaceAll("[.\\s/]+", "-");

        // D-M-Y (povolí 1–2 cifry dne/měsíce)
        var dmy = new java.time.format.DateTimeFormatterBuilder()
                .parseLenient()
                .appendPattern("d-M-uuuu")
                .toFormatter(new Locale("cs","CZ"));
        try { return LocalDate.parse(norm, dmy); } catch (Exception ignore) {}

        // Y-M-D (ISO-like, povolí 1–2 cifry dne/měsíce)
        var ymd = new java.time.format.DateTimeFormatterBuilder()
                .parseLenient()
                .appendPattern("uuuu-M-d")
                .toFormatter();
        try { return LocalDate.parse(norm, ymd); } catch (Exception ignore) {}

        // poslední pokus: čisté ISO bez normalizace
        try { return LocalDate.parse(in, DateTimeFormatter.ISO_LOCAL_DATE); } catch (Exception ignore) {}

        return null;
    }

    /** Pokusí se naparsovat měsíc: M/Y, M.Y, M-Y, M Y i Y-M, Y.M, Y M, případně ISO "yyyy-MM". */
    private static YearMonth tryParseMonth(String s) {
        if (s == null) return null;
        String in = s.trim();

        // 1) M-YYYY (oddělovač ., /, -, nebo mezera)
        java.util.regex.Matcher m1 = java.util.regex.Pattern
                .compile("^(\\d{1,2})[.\\-/ ](\\d{4})$")
                .matcher(in);
        if (m1.matches()) {
            int month = Integer.parseInt(m1.group(1));
            int year  = Integer.parseInt(m1.group(2));
            if (month >= 1 && month <= 12) return YearMonth.of(year, month);
            return null;
        }

        // 2) YYYY-M (oddělovač ., /, -, nebo mezera)
        java.util.regex.Matcher m2 = java.util.regex.Pattern
                .compile("^(\\d{4})[.\\-/ ](\\d{1,2})$")
                .matcher(in);
        if (m2.matches()) {
            int year  = Integer.parseInt(m2.group(1));
            int month = Integer.parseInt(m2.group(2));
            if (month >= 1 && month <= 12) return YearMonth.of(year, month);
            return null;
        }

        // 3) fallback ISO "yyyy-MM"
        try {
            return YearMonth.parse(in, DateTimeFormatter.ofPattern("yyyy-MM"));
        } catch (Exception ignore) { }

        return null;
    }
}
