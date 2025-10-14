package vecera.projekt.security;

import vecera.projekt.entity.PojistnaUdalost;
import vecera.projekt.entity.Pojisteny;
import vecera.projekt.repository.PojistkaOsobaRepo;
import vecera.projekt.repository.PojistnaUdalostRepo;
import vecera.projekt.service.SpravcePojistenych;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * Vlastní bezpečnostní pravidla pro @PreAuthorize.

 * Registruje se jako bean jménem {@code "sec"} a lze jej volat v
 * anotacích, např. {@code @PreAuthorize("@sec.canSeePojisteny(authentication, #id)")}
 *
 * Principy:
 * - ADMIN vidí/edituje vše,
 * - běžný USER může pracovat jen se „svými“ daty (podle navázaného {@code pojistenyId}),
 * - u pojistky stačí být členem v libovolné roli (POJISTNIK/POJISTENY),
 * - u události se kontroluje vlastnictví pojištěného.
 */

@Component("sec") // bean jména "sec" pro @PreAuthorize("... @sec.method(...) ...")
public class SecurityService {

    private final SpravcePojistenych spravce;
    private final PojistnaUdalostRepo udalostRepo;
    private final PojistkaOsobaRepo pojistkaOsobaRepo;

    public SecurityService(SpravcePojistenych spravce,
                           PojistnaUdalostRepo udalostRepo,
                           PojistkaOsobaRepo pojistkaOsobaRepo) {
        this.spravce = spravce;
        this.udalostRepo = udalostRepo;
        this.pojistkaOsobaRepo = pojistkaOsobaRepo;
    }

    /* ---------- interní pomocné metody ---------- */

    /** Zda má uživatel roli ADMIN. */
    private boolean isAdmin(Authentication a) {
        return a != null && a.isAuthenticated() &&
                a.getAuthorities().stream().anyMatch(x -> "ROLE_ADMIN".equals(x.getAuthority()));
    }

    /** ID pojištěného aktuálně přihlášeného uživatele (nebo {@code null}, pokud není přiřazen). */
    private Integer myPojId(Authentication a) {
        if (a == null || !a.isAuthenticated()) return null;
        Object p = a.getPrincipal();
        return (p instanceof PrihlasenyUzivatel up) ? up.getPojistenyId() : null;
    }

    /** Získá ID vlastníka z události – z entity nebo z transientního pole. */
    private Integer extractPojistenyId(PojistnaUdalost u) {
        if (u == null) return null;
        // 1) když je nastavena vazba na entitu:
        Pojisteny p = u.getPojisteny();
        if (p != null && p.getId() != null) return p.getId();
        // 2) fallback: transientní pojistenyId (když vazba není nahraná)
        return u.getPojistenyId();
    }

    /* ---------- POJIŠTĚNÝ ---------- */

    /** Zda smí uživatel vidět detail pojištěného (ADMIN vše, USER jen sám sebe). */
    public boolean canSeePojisteny(Authentication a, int pojistenyId) {
        if (isAdmin(a)) return true;
        Integer mine = myPojId(a);
        return Objects.equals(mine, pojistenyId);
    }

    /** Zda smí uživatel upravovat profil pojištěného (stejné jako vidět). */
    public boolean canEditPojisteny(Authentication a, int pojistenyId) {
        return canSeePojisteny(a, pojistenyId);
    }

    /* ---------- POJISTKA ---------- */

    /**
     * Zda smí uživatel vidět pojistku.
     * ADMIN vždy, USER pokud je u pojistky evidován v libovolné roli.
     */
    public boolean canSeePojisteni(Authentication a, int pojistkaId) {
        if (isAdmin(a)) return true;
        Integer mine = myPojId(a);
        return mine != null && pojistkaOsobaRepo.existsMember(pojistkaId, mine);
    }

    /** Zda smí uživatel editovat pojistku (zde povoleno ADMIN, případně vlastník). */
    public boolean canEditPojisteni(Authentication a, int pojistkaId) {
        return isAdmin(a) || canSeePojisteni(a, pojistkaId);
    }

    /* ---------- POJISTNÁ UDÁLOST ---------- */

    /** Zda smí uživatel vidět konkrétní pojistnou událost (ADMIN nebo vlastník). */
    public boolean canSeeUdalost(Authentication a, int udalostId) {
        if (isAdmin(a)) return true;
        Integer mine = myPojId(a);
        if (mine == null) return false;

        return udalostRepo.findById(udalostId)
                .map(u -> {
                    Pojisteny p = u.getPojisteny();
                    Integer ownerId = (p != null ? p.getId() : null);
                    if (ownerId == null) ownerId = u.getPojistenyId(); // fallback, pokud není načtená vazba
                    return Objects.equals(mine, ownerId);
                })
                .orElse(false);
    }

    /** Zda smí uživatel editovat událost (stejné jako vidět). */
    public boolean canEditUdalost(Authentication a, int udalostId) {
        return canSeeUdalost(a, udalostId);
    }

    /**
     * Zda smí uživatel uložit (vytvořit/aktualizovat) událost.
     * ADMIN vždy, USER pouze pro svou událost (podle vlastníka/pojistenyId).
     */
    public boolean canSaveUdalost(Authentication a, PojistnaUdalost u) {
        if (isAdmin(a)) return true;
        Integer mine = myPojId(a);
        Integer ownerId = extractPojistenyId(u);
        return mine != null && Objects.equals(mine, ownerId);
    }
}
