package vecera.projekt.entity;

/**
 * Stav životního cyklu pojistné události.
 * Typický flow: NOVA → RESENA | ZAMITNUTA.
 */

public enum StavUdalosti {

    /** Událost nahlášena, čeká na zpracování. */ NOVA,
    /** Událost vyřízena (schválena/uzavřena). */  RESENA,
    /** Událost uzavřena. */                      UZAVRENA
}
