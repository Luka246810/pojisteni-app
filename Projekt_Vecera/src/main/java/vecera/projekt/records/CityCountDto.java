package vecera.projekt.records;

/**
 * Agregace počtu pojištěných podle města (pro tabulky/grafy).
 *
 * @param mesto název města
 * @param pocet počet pojištěných v daném městě
 */

public record CityCountDto(String mesto, long pocet) {}
