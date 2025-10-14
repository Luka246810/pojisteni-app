package vecera.projekt.security;

import vecera.projekt.entity.Uzivatel;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.stream.Collectors;

/**
 * Adaptér entity {@link Uzivatel} na rozhraní {@link UserDetails}.

 * Účel:
 * - poskytuje Spring Security potřebné údaje pro autentizaci/autorizaci,
 * - mapuje názvy rolí z DB na {@link GrantedAuthority} (doplní prefix {@code ROLE_}, pokud chybí),
 * - zpřístupňuje id navázaného pojištěného pro pohodlné ověřování práv v aplikaci.
 */

public class PrihlasenyUzivatel implements UserDetails {
    private final Uzivatel u;

    public PrihlasenyUzivatel(Uzivatel u) {
        this.u = u;
    }

    /** ID profilu pojištěného navázaného na přihlášeného uživatele (může být {@code null}). */

    public Integer getPojistenyId() {
        return u.getPojistenyId();
    }

    /**
     * Zda má uživatel danou roli (plný název včetně prefixu, např. {@code ROLE_ADMIN}).
     * Použití: rychlé ověření v aplikační logice mimo @PreAuthorize.
     */

    public boolean hasRole(String fullRole) {
        return u.getRoleNames() != null && u.getRoleNames().stream().anyMatch(fullRole::equals);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        var roles = u.getRoleNames();
        if (roles == null || roles.isEmpty()) return java.util.List.of();

        // doplní "ROLE_" pokud by v DB bylo jen "ADMIN"/"USER"
        return roles.stream()
                .map(r -> r != null && r.startsWith("ROLE_") ? r : "ROLE_" + r)
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }

    @Override public String getPassword() { return u.getPasswordHash(); }
    @Override public String getUsername() { return u.getUsername(); }
    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return u.isEnabled(); }
}
