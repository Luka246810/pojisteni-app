package vecera.projekt.entity;

import java.util.List;

/**
 * Rozšířené detaily k pojištěné osobě (adresy, doplňkové informace).

 * Vztahy:
 * - 1:1 k entitě Pojisteny (sdílený klíč / foreign key),
 * - oddělení větších/slaběji používaných dat kvůli výkonu a přehlednosti.
 */

public class PojistenyDetail {
    private final Pojisteny pojisteny;
    private final List<TypPojisteni> pojistky;

    public PojistenyDetail(Pojisteny pojisteny, List<TypPojisteni> pojistky) {
        this.pojisteny = pojisteny;
        this.pojistky = List.copyOf(pojistky);
    }

    public Pojisteny getPojisteny() { return pojisteny; }
    public List<TypPojisteni> getPojistky() { return pojistky; }

}