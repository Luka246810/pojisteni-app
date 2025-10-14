package vecera.projekt.entity;

/**
 * Vazební entita osoba ↔ pojistka s rolí v pojistce.

 * Účel:
 * - reprezentuje M:N vztah (pojistka – osoby) s atributem role,
 * - role definovaná enumem (např. POJISTNIK / POJISTENY).

 * Pozn.:
 * - ošetři unikátnost kombinace (pojistka_id, osoba_id, role),
 * - změny rolí prováděj přes service s kontrolou duplicit.
 */


public class PojistkaOsoba {
    private int pojistkaId;
    private int osobaId;
    private RoleVPojistce role;

    public int getPojistkaId() {
        return pojistkaId;
    }

    public void setPojistkaId(int pojistkaId) {
        this.pojistkaId = pojistkaId;
    }

    public int getOsobaId() {
        return osobaId;
    }

    public void setOsobaId(int osobaId) {
        this.osobaId = osobaId;
    }

    public RoleVPojistce getRole() {
        return role;
    }

    public void setRole(RoleVPojistce role) {
        this.role = role;
    }

    @Override
    public String toString() {
        return "PojistkaOsoba{" +
                "pojistkaId=" + pojistkaId +
                ", osobaId=" + osobaId +
                ", role=" + role +
                '}';
    }
}
