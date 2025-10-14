// src/main/java/vecera/projekt/controller/ReportExportController.java
package vecera.projekt.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import vecera.projekt.records.*;
import vecera.projekt.service.ReportService;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;

/**
 * Export reportů do souboru (CSV/Excel/PDF).

 * Endpoints:
 * - GET /reporty/export        → stáhne CSV (Content-Disposition: attachment)

 * Pozn.:

 * - Parametry filtru validuje service; export běží mimo transakci, streamuje se.
 */

@Controller
@RequiredArgsConstructor
public class ReportExportController {

    private final ReportService reports;

    @GetMapping(value = "/reporty/export", produces = "text/csv; charset=UTF-8")
    public ResponseEntity<byte[]> exportCsv(@RequestParam("typ") String typ) {
        StringBuilder sb = new StringBuilder();
        // UTF-8 BOM pro Excel
        sb.append('\uFEFF');

        String filename = "report-" + typ + "-" + LocalDate.now() + ".csv";

        switch (typ) {
            case "aktivni-podle-typu" -> {
                sb.append("typ;počet\n");
                for (LabelValueDto r : reports.aktivniTypy()) {
                    sb.append(esc(r.label())).append(';').append(r.value()).append('\n');
                }
            }
            case "mesicni-nove" -> {
                sb.append("měsíc;počet\n");
                for (SeriesPoint r : reports.mesicniNove()) {
                    sb.append(esc(r.period())).append(';').append(r.count()).append('\n');
                }
            }
            case "skody-dle-stavu" -> {
                sb.append("stav;počet;suma;průměr\n");
                for (ClaimAggDto r : reports.skodyDleStavu()) {
                    sb.append(esc(r.stav())).append(';')
                            .append(r.pocet()).append(';')
                            .append(r.suma()).append(';')
                            .append(r.prumer()).append('\n');
                }
            }
            case "top-mesta" -> {
                sb.append("město;počet\n");
                for (CityCountDto r : reports.topMesta(100)) {
                    sb.append(esc(r.mesto())).append(';').append(r.pocet()).append('\n');
                }
            }
            default -> {
                // neznámý typ → prázdný CSV s info
                sb.append("info\nNeznámý typ exportu: ").append(esc(typ)).append('\n');
                filename = "report-unknown-" + LocalDate.now() + ".csv";
            }
        }

        byte[] body = sb.toString().getBytes(StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .body(body);
    }

    private static String esc(String s) {
        if (s == null) return "";
        // jednoduchý escape: uvozovky + oddělovač ; → celé pole do uvozovek a zdvojit "
        boolean needQuotes = s.contains(";") || s.contains("\"") || s.contains("\n") || s.contains("\r");
        String v = s.replace("\"", "\"\"");
        return needQuotes ? "\"" + v + "\"" : v;
    }
}
