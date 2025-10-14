package vecera.projekt.entity;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;

/**
 * Aplikační uživatel (přihlašovací účet).

 * Bezpečnost:
 * - heslo uloženo jako BCrypt hash (sloupec např. password_hash),
 * - role přes tabulku uzivatel_role (ROLE_USER / ROLE_ADMIN).

 * Pozn.:
 * - uživatel může být propojen na Pojisteny (např. pohlídání „mých dat“),
 * - nikdy nevracej hash do view/DTO.
 */

@Entity
@Table(name = "uzivatel", uniqueConstraints = {
        @UniqueConstraint(name = "uq_uzivatel_username", columnNames = "username")
})
public class Uzivatel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true, length = 80)
    private String username;

    @Column(name = "password_hash", nullable = false, length = 100)
    private String passwordHash;

    @Column(nullable = false)
    private boolean enabled;

    @Column(name = "pojisteny_id")
    private Integer pojistenyId;

    // Role z tabulky uzivatel_role (uzivatel_id, role_name)
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "uzivatel_role",
            joinColumns = @JoinColumn(name = "uzivatel_id"))
    @Column(name = "role_name", nullable = false, length = 50)
    private Set<String> roleNames = new HashSet<>();

    // --- get/set ---
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    public Integer getPojistenyId() { return pojistenyId; }
    public void setPojistenyId(Integer pojistenyId) { this.pojistenyId = pojistenyId; }

    public Set<String> getRoleNames() { return roleNames; }
    public void setRoleNames(Set<String> roleNames) { this.roleNames = roleNames; }

    // --- convenience/adapters ---

    /** Kompatibilní název pro kód, který očekává getPassword() / setPassword(). */
    public String getPassword() { return this.passwordHash; }

    public void setPassword(String encodedPassword) {
        this.passwordHash = encodedPassword;
    }

    /** Přidej roli do kolekce rolí. */
    public void addRole(String role) {
        if (role != null && !role.isBlank()) {
            this.roleNames.add(role.trim());
        }
    }

    /** Zkontroluj, zda uživatel danou roli má. */
    public boolean hasRole(String role) {
        return role != null && this.roleNames.contains(role);
    }

}
