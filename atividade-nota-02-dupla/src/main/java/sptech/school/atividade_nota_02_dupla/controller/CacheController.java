package sptech.school.atividade_nota_02_dupla.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sptech.school.atividade_nota_02_dupla.cache.ConteudoCache;
import sptech.school.atividade_nota_02_dupla.dto.EstatisticasCacheResponse;

import java.util.Map;

@RestController
@RequestMapping("/cache")
public class CacheController {

    private final ConteudoCache cache;

    public CacheController(ConteudoCache cache) {
        this.cache = cache;
    }

    @GetMapping("/estatisticas")
    public ResponseEntity<EstatisticasCacheResponse> estatisticas() {
        Map<String, Object> stats = cache.estatisticas();

        EstatisticasCacheResponse dto = new EstatisticasCacheResponse(
                (int) stats.get("tamanhoAtual"),
                (int) stats.get("limite"),
                (long) stats.get("hits"),
                (long) stats.get("misses"),
                (long) stats.get("evictions"),
                (double) stats.get("hitRatio")
        );
        return ResponseEntity.ok(dto);
    }

    @DeleteMapping
    public ResponseEntity<Map<String, String>> limpar() {
        cache.limpar();
        return ResponseEntity.ok(Map.of("mensagem", "Cache limpo com sucesso"));
    }
}