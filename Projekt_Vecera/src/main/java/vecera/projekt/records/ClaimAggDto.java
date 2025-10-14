package vecera.projekt.records;

import java.math.BigDecimal;

/**
 * Agregace pojistných událostí podle stavu (count/sum/avg).
 *
 * @param stav   stav události (např. NOVA/RESENA/ZAMITNUTA)
 * @param pocet  počet událostí v daném stavu
 * @param suma   součet vyplacených/požadovaných částek (přesná aritmetika – BigDecimal)
 * @param prumer průměrná částka na událost (BigDecimal)
 */

public record ClaimAggDto(String stav, long pocet, BigDecimal suma, BigDecimal prumer) {}
