package sptech.school.atividade_nota_02_dupla.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sptech.school.atividade_nota_02_dupla.cache.ConteudoCache;
import sptech.school.atividade_nota_02_dupla.cache.ConteudoCacheItem;
import sptech.school.atividade_nota_02_dupla.entity.Conteudo;
import sptech.school.atividade_nota_02_dupla.repository.ConteudoRepository;

import java.util.Optional;



// Service pede ao ConteudoCache.
// Se HIT: devolve direto (rápido).
// Se MISS: busca no ConteudoRepository (lento, com Thread.sleep), armazena no cache e devolve.
@Service
public class ConteudoService {

    private final ConteudoRepository repository;
    private final ConteudoCache cache;
    private final long atrasoMs;

    // Time.sleep vem do application.properties (efeito lento)
    public ConteudoService(ConteudoRepository repository, ConteudoCache cache,
                           @Value("${app.fonte.atraso-ms:2000}") long atrasoMs) {
        this.repository = repository;
        this.cache = cache;
        this.atrasoMs = atrasoMs;
    }

    // Busca no cache. Se não tem, busca na fonte (com atraso), guarda no cache e devolve.
    public BuscaResultado buscarPorId(Long id) {
        Optional<ConteudoCacheItem> cached = cache.buscar(id);
        if (cached.isPresent()) {
            return new BuscaResultado((Conteudo) cached.get(), true);
        }

        Optional<Conteudo> doBanco = buscarNaFonte(id);

        if (doBanco.isPresent()) {
            cache.armazenar(doBanco.get());
            return new BuscaResultado(doBanco.get(), false);
        }

        return new BuscaResultado(null, false);
    }

    private Optional<Conteudo> buscarNaFonte(Long id) {
        try {
            Thread.sleep(atrasoMs);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrompido durante atraso artificial", e);
        }
        return repository.findById(id);
    }


    @Transactional
    public Conteudo cadastrar(Conteudo conteudo) {
        return repository.save(conteudo);
    }

    @Transactional
    public Conteudo atualizar(Long id, Conteudo dadosNovos) {
        Conteudo existente = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Conteudo nao encontrado: " + id));

        existente.setTitulo(dadosNovos.getTitulo());
        existente.setCorpo(dadosNovos.getCorpo());
        existente.setAutor(dadosNovos.getAutor());

        Conteudo salvo = repository.save(existente);
        cache.invalidar(id); // invalida o cache para forçar refresh na próxima busca
        return salvo;
    }

    @Transactional
    public void excluir(Long id) {
        if (!repository.existsById(id)) {
            throw new IllegalArgumentException("Conteudo nao encontrado: " + id);
        }
        repository.deleteById(id);
        cache.invalidar(id); // invalida o cache
    }
}