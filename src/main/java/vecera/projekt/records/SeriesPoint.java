package vecera.projekt.records;

/**
 * Bod časové řady pro přehledy/grafy.
 *
 * @param period časový úsek jako řetězec (např. "2025-10", "2025-W40", "2025-10-09")
 * @param count  počet záznamů v daném období
 */

public record SeriesPoint(String period, long count) { }
