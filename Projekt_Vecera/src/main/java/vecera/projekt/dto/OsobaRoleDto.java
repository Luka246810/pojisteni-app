package vecera.projekt.dto;

/**
 * DTO pro zobrazení osoby a její role v konkrétní pojistce.

 * Účel:
 * - lehká projekce pro výpis (seznam osob u pojistky, badge s rolí apod.),
 * - neobsahuje JPA vazby ani zbytečná pole – vhodné do Thymeleaf šablon.

 * Pozn.:
 * - {@code id} je ID osoby (pojištěného),
 * - {@code role} bývá "POJISTNIK" nebo "POJISTENY" (viz enum RoleVPojistce),
 * - obvykle se plní z projekce/view (např. OsobaRoleView) v repository vrstvě.

 * Neměnné (immutable) DTO – pouze gettery.
 */

public class OsobaRoleDto {
    /** ID pojištěné osoby. */
    private final int id;

    /** Křestní jméno osoby. */
    private final String jmeno;

    /** Příjmení osoby. */
    private final String prijmeni;

    /** Role osoby v pojistce: "POJISTNIK" | "POJISTENY". */
    private final String role;

    public OsobaRoleDto(int id, String jmeno, String prijmeni, String role) {
        this.id = id;
        this.jmeno = jmeno;
        this.prijmeni = prijmeni;
        this.role = role;
    }

    public int getId() { return id; }
    public String getJmeno() { return jmeno; }
    public String getPrijmeni() { return prijmeni; }
    public String getRole() { return role; }
}
