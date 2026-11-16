package sptech.school.atividade_nota_02_dupla.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sptech.school.atividade_nota_02_dupla.dto.ConteudoRequest;
import sptech.school.atividade_nota_02_dupla.dto.ConteudoResponse;
import sptech.school.atividade_nota_02_dupla.entity.Conteudo;
import sptech.school.atividade_nota_02_dupla.mapper.ConteudoMapper;
import sptech.school.atividade_nota_02_dupla.service.ConteudoService;
import sptech.school.atividade_nota_02_dupla.service.BuscaResultado;

/**
 * CONTROLLER de CONTEÚDOS: endpoints REST.
 *
 * <p>Responsabilidades do Controller (padrão da disciplina):
 * <ul>
 *     <li>Recebe requisições HTTP (DTOs, parâmetros de path, headers).</li>
 *     <li>Chama o Service (que devolve Entidades).</li>
 *     <li>Converte Entidade -> DTO via {@link ConteudoMapper}.</li>
 *     <li>Devolve ResponseEntity com o DTO + headers (ex.: X-Cache).</li>
 * </ul>
 *
 * <p>O Service NÃO sabe que existe HTTP, Header, DTO, etc.
 */
@RestController
@RequestMapping("/conteudos")
public class ConteudoController {

    private final ConteudoService service;

    @Autowired
    public ConteudoController(ConteudoService service) {
        this.service = service;
    }

    /**
     * GET /conteudos/{id}
     * <p>Busca no cache (service). Retorna header X-Cache: HIT ou X-Cache: MISS.
     *
     * @param id identificador do conteúdo.
     * @return 200 com o DTO + header X-Cache, ou 404 se não existir.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ConteudoResponse> buscarPorId(@PathVariable Long id) {
        BuscaResultado resultado = service.buscarPorId(id);

        if (resultado.conteudo() == null) {
            return ResponseEntity.notFound().build();
        }

        ConteudoResponse dto = ConteudoMapper.paraResponse(resultado.conteudo());

        // Header X-Cache: HIT ou MISS
        String cacheHeader = resultado.hit() ? "HIT" : "MISS";
        return ResponseEntity.ok()
                .header("X-Cache", cacheHeader)
                .body(dto);
    }

    /**
     * POST /conteudos
     * <p>Cadastra um conteúdo. Retorna 201 Created com o DTO do criado.
     */
    @PostMapping
    public ResponseEntity<ConteudoResponse> cadastrar(@RequestBody @Valid ConteudoRequest request) {
        Conteudo entidade = ConteudoMapper.paraEntidade(request);
        Conteudo salvo = service.cadastrar(entidade);
        ConteudoResponse dto = ConteudoMapper.paraResponse(salvo);
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    /**
     * PUT /conteudos/{id}
     * <p>Atualiza um conteúdo. Invalida o cache.
     */
    @PutMapping("/{id}")
    public ResponseEntity<ConteudoResponse> atualizar(@PathVariable Long id, @RequestBody @Valid ConteudoRequest request) {
        Conteudo dadosNovos = ConteudoMapper.paraEntidade(request);
        Conteudo salvo = service.atualizar(id, dadosNovos);
        ConteudoResponse dto = ConteudoMapper.paraResponse(salvo);
        return ResponseEntity.ok(dto);
    }

    /**
     * DELETE /conteudos/{id}
     * <p>Exclui um conteúdo. Invalida o cache.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }
}