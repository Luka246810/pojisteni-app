package vecera.projekt.entity;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Pojištěná osoba (tabulka `pojisteny`).
 * Sloupce dle SQL: id, jmeno, prijmeni, telefon, vek, email, pohlavi,
 * mesto, ulice, cislo_popisne, psc.
 */

@Entity
@Table(name = "pojisteny")
public class Pojisteny {


    public Pojisteny() { } // JPA no-args

    public Pojisteny(String jmeno, String prijmeni, Integer vek, String telefon) {
        this.jmeno = jmeno;
        this.prijmeni = prijmeni;
        this.vek = vek;
        this.telefon = telefon;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 50)
    private String jmeno;

    @Column(nullable = false, length = 50)
    private String prijmeni;

    @Column(nullable = false, length = 50)
    private String telefon;

    @Column(nullable = false)
    private Integer vek;

    @Column(length = 255)
    private String email;

    @Column(length = 20)
    private String pohlavi;

    @Column(length = 100)
    private String mesto;

    @Column(length = 120)
    private String ulice;

    @Column(name = "cislo_popisne", length = 20)
    private String cisloPopisne;

    @Column(length = 10)
    private String psc;

    /** Jedna osoba může mít více pojistek (FK typ_pojisteni.pojisteny_id). */
    @OneToMany(mappedBy = "pojisteny", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TypPojisteni> pojisteni = new ArrayList<>();

    // --- get/set ---
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getJmeno() { return jmeno; }
    public void setJmeno(String jmeno) { this.jmeno = jmeno; }

    public String getPrijmeni() { return prijmeni; }
    public void setPrijmeni(String prijmeni) { this.prijmeni = prijmeni; }

    public String getTelefon() { return telefon; }
    public void setTelefon(String telefon) { this.telefon = telefon; }

    public Integer getVek() { return vek; }
    public void setVek(Integer vek) { this.vek = vek; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPohlavi() { return pohlavi; }
    public void setPohlavi(String pohlavi) { this.pohlavi = pohlavi; }

    public String getMesto() { return mesto; }
    public void setMesto(String mesto) { this.mesto = mesto; }

    public String getUlice() { return ulice; }
    public void setUlice(String ulice) { this.ulice = ulice; }

    public String getCisloPopisne() { return cisloPopisne; }
    public void setCisloPopisne(String cisloPopisne) { this.cisloPopisne = cisloPopisne; }

    public String getPsc() { return psc; }
    public void setPsc(String psc) { this.psc = psc; }

    public List<TypPojisteni> getPojisteni() { return pojisteni; }
    public void setPojisteni(List<TypPojisteni> pojisteni) { this.pojisteni = pojisteni; }

    // Pomocné metody pro oboustrannou vazbu
    public void addPojisteni(TypPojisteni p) {
        pojisteni.add(p);
        p.setPojisteny(this);
    }
    public void removePojisteni(TypPojisteni p) {
        pojisteni.remove(p);
        p.setPojisteny(null);
    }
}
