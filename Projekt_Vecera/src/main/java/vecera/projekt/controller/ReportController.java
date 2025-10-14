package vecera.projekt.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import vecera.projekt.service.ReportService;

/**
 * Reportovací obrazovky (přehledy, agregace, filtrace).

 * Endpoints:
 * - GET /reporty        → přehled/dash (tabulky, grafy, filtry)

 * Pozn.:
 * - Přístup typicky ADMIN
 * - Složitější dotazy drž v ReportService/ReportRepo (projekce/DTO).
 */


@Controller
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reports;

    @GetMapping("/reporty")
    public String overview(Model model) {
        model.addAttribute("snap", reports.snapshot());
        model.addAttribute("aktivniTypy", reports.aktivniTypy());
        model.addAttribute("mesicniNove", reports.mesicniNove());
        model.addAttribute("skody", reports.skodyDleStavu());
        model.addAttribute("mesta", reports.topMesta(10));
        return "reporty/index";
    }
}
