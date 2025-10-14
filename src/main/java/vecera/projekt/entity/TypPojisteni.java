package vecera.projekt.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Katalog/typ pojištění (např. povinné ručení, majetek, úraz).

 * Účel:
 * - normalizuje typ produktu napříč pojistkami,
 * - umožňuje jednodušší filtrování/reporting.

 * Pozn.:
 * - vazba z Pojistka bývá M:1 (každá pojistka má jeden typ).
 */

@Entity
@Table(name = "typ_pojisteni")
public class TypPojisteni {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "pojisteny_id", nullable = false)
    private Pojisteny pojisteny;

    @Column(nullable = false, length = 100)
    private String nazev;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal castka;

    @Column(name = "platnost_do", nullable = false)
    private LocalDate platnostDo;

    @Column(name = "platnost_od", nullable = false)
    private LocalDate platnostOd;

    // --- get/set ---
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Pojisteny getPojisteny() { return pojisteny; }
    public void setPojisteny(Pojisteny pojisteny) { this.pojisteny = pojisteny; }

    public String getNazev() { return nazev; }
    public void setNazev(String nazev) { this.nazev = nazev; }

    public BigDecimal getCastka() { return castka; }
    public void setCastka(BigDecimal castka) { this.castka = castka; }

    public LocalDate getPlatnostDo() { return platnostDo; }
    public void setPlatnostDo(LocalDate platnostDo) { this.platnostDo = platnostDo; }

    public LocalDate getPlatnostOd() { return platnostOd; }
    public void setPlatnostOd(LocalDate platnostOd) { this.platnostOd = platnostOd; }
}
