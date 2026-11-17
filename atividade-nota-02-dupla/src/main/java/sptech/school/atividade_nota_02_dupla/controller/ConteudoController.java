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

@RestController
@RequestMapping("/conteudos")
public class ConteudoController {

    private final ConteudoService service;

    @Autowired
    public ConteudoController(ConteudoService service) {
        this.service = service;
    }

    @GetMapping("/{id}")
    public ResponseEntity<ConteudoResponse> buscarPorId(@PathVariable Long id) {
        BuscaResultado resultado = service.buscarPorId(id);

        if (resultado.conteudo() == null) {
            return ResponseEntity.notFound().build();
        }

        ConteudoResponse dto = ConteudoMapper.paraResponse(resultado.conteudo());

        String cacheHeader = resultado.hit() ? "HIT" : "MISS";
        return ResponseEntity.ok()
                .header("X-Cache", cacheHeader)
                .body(dto);
    }

    @PostMapping
    public ResponseEntity<ConteudoResponse> cadastrar(@RequestBody @Valid ConteudoRequest request) {
        Conteudo entidade = ConteudoMapper.paraEntidade(request);
        Conteudo salvo = service.cadastrar(entidade);
        ConteudoResponse dto = ConteudoMapper.paraResponse(salvo);
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ConteudoResponse> atualizar(@PathVariable Long id, @RequestBody @Valid ConteudoRequest request) {
        Conteudo dadosNovos = ConteudoMapper.paraEntidade(request);
        Conteudo salvo = service.atualizar(id, dadosNovos);
        ConteudoResponse dto = ConteudoMapper.paraResponse(salvo);
        return ResponseEntity.ok(dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }
}