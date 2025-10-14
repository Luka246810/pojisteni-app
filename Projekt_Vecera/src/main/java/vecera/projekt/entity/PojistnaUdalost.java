package vecera.projekt.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Pojistná událost navázaná na konkrétní pojistku.

 * Vztahy:
 * - M:1 k Pojistka; stav řeší enum (např. NOVA/RESENA/ZAMITNUTA),
 * - obsahuje částky, popis, datum vzniku/hlášení.

 * Pozn.:
 * - přechody stavů validuj v service (práva, konsistence).
 */

@Entity
@Table(name = "pojistna_udalost")
public class PojistnaUdalost {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // povinná vazba na pojištěného
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pojisteny_id", nullable = false)
    private Pojisteny pojisteny;

    // volitelná vazba na typ pojištění (v DB je FK s ON DELETE SET NULL)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "typ_pojisteni_id")
    private TypPojisteni typPojisteni;

    @Column(nullable = false)
    private LocalDate datum;

    @Column(nullable = false, length = 1000)
    private String popis;

    // POZOR: v DB je "skoda" (ne "castka")
    @Column(name = "skoda", nullable = false, precision = 12, scale = 2)
    private BigDecimal skoda;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private StavUdalosti stav = StavUdalosti.NOVA;

    // --- Helper pro binding přes ID (není mapováno do DB) ---
    @Transient
    private Integer pojistenyId;

    // --- get/set ---
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Pojisteny getPojisteny() { return pojisteny; }
    public void setPojisteny(Pojisteny pojisteny) { this.pojisteny = pojisteny; }

    public TypPojisteni getTypPojisteni() { return typPojisteni; }
    public void setTypPojisteni(TypPojisteni typPojisteni) { this.typPojisteni = typPojisteni; }

    public LocalDate getDatum() { return datum; }
    public void setDatum(LocalDate datum) { this.datum = datum; }

    public String getPopis() { return popis; }
    public void setPopis(String popis) { this.popis = popis; }

    public BigDecimal getSkoda() { return skoda; }
    public void setSkoda(BigDecimal skoda) { this.skoda = skoda; }

    public StavUdalosti getStav() { return stav; }
    public void setStav(StavUdalosti stav) { this.stav = stav; }

    // helper pro formuláře
    public Integer getPojistenyId() {
        return (pojistenyId != null) ? pojistenyId : (pojisteny != null ? pojisteny.getId() : null);
    }
    public void setPojistenyId(Integer pojistenyId) { this.pojistenyId = pojistenyId; }
}
