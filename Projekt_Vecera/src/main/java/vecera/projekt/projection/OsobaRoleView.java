package vecera.projekt.projection;

/**
 * Projekce osoby a její role u pojistky (interface-based projection).
 * DŮLEŽITÉ: aliasy ve SELECTu musí odpovídat názvům getterů (id, jmeno, prijmeni, role).
 */

public interface OsobaRoleView {
    Integer getId();
    String getJmeno();
    String getPrijmeni();
    String getRole(); // "POJISTNIK" | "POJISTENY"
}
