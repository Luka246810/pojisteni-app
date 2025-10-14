package vecera.projekt.entity;

/**
 * Role osoby v pojistce.
 * POJISTNIK = vlastník smlouvy, POJISTENY = krytá osoba.
 */
public enum RoleVPojistce {

    /** Vlastník pojistné smlouvy (plátce). */ POJISTNIK,
    /** Osoba krytá pojistkou. */              POJISTENY
}
