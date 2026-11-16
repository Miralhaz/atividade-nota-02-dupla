package sptech.school.atividade_nota_02_dupla.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sptech.school.atividade_nota_02_dupla.cache.ConteudoCache;
import sptech.school.atividade_nota_02_dupla.cache.ConteudoCacheItem;
import sptech.school.atividade_nota_02_dupla.entity.Conteudo;
import sptech.school.atividade_nota_02_dupla.repository.ConteudoRepository;

import java.util.Optional;

/**
 * SERVICE: contém a regra de negócio e ORQUESTRA o uso do cache.
 *
 * <p>Regra do padrão da disciplina:
 * <ul>
 *     <li>O Service recebe e devolve ENTIDADES (não DTOs).</li>
 *     <li>O Controller converte Entidade -> DTO (via Mapper).</li>
 * </ul>
 *
 * <p>Fluxo do GET:
 * <ol>
 *     <li>Service pede ao {@link ConteudoCache}.</li>
 *     <li>Se HIT: devolve direto (rápido).</li>
 *     <li>Se MISS: busca no {@link ConteudoRepository} (lento, com Thread.sleep), armazena no cache e devolve.</li>
 * </ol>
 *
 * <p>Fluxo do PUT/DELETE:
 * <ol>
 *     <li>Atualiza/Exclui no banco.</li>
 *     <li>Invalida a entrada no cache.</li>
 * </ol>
 */
@Service
public class ConteudoService {

    private final ConteudoRepository repository;
    private final ConteudoCache cache;
    private final long atrasoMs;

    /**
     * O atraso artificial vem do application.properties (padrão 2000ms = 2s).
     * Isso simula uma fonte lenta (ex.: chamada a API externa, consulta pesada).
     */
    public ConteudoService(ConteudoRepository repository, ConteudoCache cache,
                           @Value("${app.fonte.atraso-ms:2000}") long atrasoMs) {
        this.repository = repository;
        this.cache = cache;
        this.atrasoMs = atrasoMs;
    }

    /**
     * GET: busca no cache; se não tem, busca na fonte (com atraso), guarda no cache e devolve.
     *
     * @return resultado com o conteúdo e flag hit/miss.
     */
    public BuscaResultado buscarPorId(Long id) {
        // 1) Tenta o cache primeiro (rápido)
        Optional<ConteudoCacheItem> cached = cache.buscar(id);
        if (cached.isPresent()) {
            // HIT: o cache já devolve o item (Conteudo implements ConteudoCacheItem)
            return new BuscaResultado((Conteudo) cached.get(), true);
        }

        // 2) MISS: busca na fonte (lenta)
        Optional<Conteudo> doBanco = buscarNaFonte(id);

        // 3) Se achou no banco, guarda no cache para as próximas chamadas
        if (doBanco.isPresent()) {
            cache.armazenar(doBanco.get());
            return new BuscaResultado(doBanco.get(), false);
        }

        return new BuscaResultado(null, false);
    }

    /**
     * Simula a lentidão da fonte (banco, API externa, etc.).
     *
     * @param id identificador do conteúdo.
     * @return o conteúdo do banco.
     */
    private Optional<Conteudo> buscarNaFonte(Long id) {
        // Atraso artificial ANTES da chamada ao repositório.
        try {
            Thread.sleep(atrasoMs);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrompido durante atraso artificial", e);
        }
        return repository.findById(id);
    }

    /**
     * POST: salva no banco e devolve a entidade salva.
     * O cache NÃO é populado aqui (só no GET, quando for necessário).
     */
    @Transactional
    public Conteudo cadastrar(Conteudo conteudo) {
        return repository.save(conteudo);
    }

    /**
     * PUT: atualiza no banco e INVALIDA o cache (para a próxima busca ir no banco).
     */
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

    /**
     * DELETE: exclui do banco e INVALIDA o cache.
     */
    @Transactional
    public void excluir(Long id) {
        if (!repository.existsById(id)) {
            throw new IllegalArgumentException("Conteudo nao encontrado: " + id);
        }
        repository.deleteById(id);
        cache.invalidar(id); // invalida o cache
    }
}