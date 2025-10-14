package vecera.projekt.records;

/**
 * Obecný pár label → value (pro grafy, metriky, výběrové seznamy).
 *
 * @param label popisek položky (osa grafu / název metriky)
 * @param value numerická hodnota
 */

public record LabelValueDto(String label, long value) { }
