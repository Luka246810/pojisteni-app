package vecera.projekt.records;

import java.math.BigDecimal;

/**
 * Sumarizační snímek pro dashboard.
 *
 * @param pocetPojistenych   celkový počet pojištěných osob
 * @param pojisteniAktivni   počet aktivních pojistek
 * @param pojisteniExpirovane počet expirovaných pojistek
 * @param sumaSkodYTD        součet škod year-to-date (BigDecimal)
 */

public record SnapshotDto(
        long pocetPojistenych,
        long pojisteniAktivni,
        long pojisteniExpirovane,
        BigDecimal sumaSkodYTD
) {}
