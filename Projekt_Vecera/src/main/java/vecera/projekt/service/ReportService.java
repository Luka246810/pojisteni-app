// ReportService.java
package vecera.projekt.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vecera.projekt.repository.ReportRepo;
import vecera.projekt.records.CityCountDto;
import vecera.projekt.records.ClaimAggDto;
import vecera.projekt.records.LabelValueDto;
import vecera.projekt.records.SeriesPoint;
import vecera.projekt.records.SnapshotDto;

import java.util.List;

/**
 * Reporty a agregace pro dashboard a exporty.
 * <p>
 * Zodpovědnosti:
 * <ul>
 *   <li>Načtení přehledového snapshotu (počty, sumy) – {@link #snapshot()}.</li>
 *   <li>Agregace aktivních typů pojistění do dvojic "label → value" – {@link #aktivniTypy()}.</li>
 *   <li>Časová řada nových položek po měsících – {@link #mesicniNove()}.</li>
 *   <li>Souhrny škod podle stavu (počet, suma, průměr) – {@link #skodyDleStavu()}.</li>
 *   <li>Top města podle počtu – {@link #topMesta(int)}.</li>
 * </ul>
 * Pozn.:
 * <ul>
 *   <li>Třída je primárně read-only nad {@link ReportRepo} a mapuje výsledky do vlastních immutable
 *       {@code record} DTO (viz balíček {@code vecera.projekt.records}).</li>
 *   <li>Logika je tenká; těžiště dotazů je v repository (nativní/JPQL).</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
public class ReportService {

    private final ReportRepo repo;

    @Transactional(readOnly = true)
    public SnapshotDto snapshot() {
        return new SnapshotDto(
                repo.countPojistenych(),
                repo.countPojisteniAktivni(),
                repo.countPojisteniExpirovane(),
                repo.sumaSkodYTD()
        );
    }

    @Transactional(readOnly = true)
    public List<LabelValueDto> aktivniTypy() {
        return repo.aktivniTypy().stream()
                .map(r -> new LabelValueDto(r.getLabel(), r.getValue()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<SeriesPoint> mesicniNove() {
        return repo.mesicniNove().stream()
                .map(r -> new SeriesPoint(r.getPeriod(), r.getCount()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ClaimAggDto> skodyDleStavu() {
        return repo.skodyDleStavu().stream()
                .map(r -> new ClaimAggDto(r.getStav(), r.getPocet(), r.getSuma(), r.getPrumer()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CityCountDto> topMesta(int limit) {
        return repo.topMesta(limit).stream()
                .map(r -> new CityCountDto(r.getMesto(), r.getPocet()))
                .toList();
    }
}
