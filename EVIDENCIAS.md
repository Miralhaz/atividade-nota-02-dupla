# Demonstração do Cache - Atividade Nota 02

## Configuração da Aplicação
```
app.cache.max-itens=5
app.cache.ttl-segundos=60
app.fonte.atraso-ms=2000
```

---

## 1. Criação de 7 conteúdos (POST /conteudos)

```bash
POST /conteudos  -> 201 Created (IDs 1 a 7)
```

---

## 2. Primeira busca (MISS - lenta ~2s) vs Segunda busca (HIT - rápida ~30ms)

### Requisição 1: GET /conteudos/1 (primeira vez - MISS)
```
HTTP/1.1 200
X-Cache: MISS
```
**Tempo de resposta: ~2095 ms**
**Log do cache:**
```
INFO  ConteudoCache : Cache MISS - id=1 - 2094 ms
```

### Requisição 2: GET /conteudos/1 (segunda vez - HIT)
```
HTTP/1.1 200
X-Cache: HIT
```
**Tempo de resposta: ~30 ms**
**Log do cache:**
```
INFO  ConteudoCache : Cache HIT - id=1 - 0 ms
```

**Comparação:** Diferença de ~2070ms (2s vs 30ms) - **~70x mais rápido** no HIT.

---

## 3. Cache atinge o limite e remove item LRU

Cache limite = 5 itens. Após buscar IDs 1, 2, 3, 4, 5 o cache está cheio.
Ao buscar ID 6 (novo), o cache remove o **menos usado recentemente (LRU)**.

### Requisições sequenciais:
```bash
GET /conteudos/2  -> X-Cache: MISS (~2082 ms)
GET /conteudos/3  -> X-Cache: MISS (~2082 ms)
GET /conteudos/4  -> X-Cache: MISS (~2082 ms)
GET /conteudos/5  -> X-Cache: MISS (~2082 ms)
GET /conteudos/6  -> X-Cache: MISS (~2082 ms)  <- Cache cheio! Remove LRU (id=1)
```

**Log do cache (eviction LRU):**
```
INFO  ConteudoCache : Cache EVICTION - id=1 removido por limite (LRU) - tamanho=5
```

**Estatísticas após 5 itens no cache + 1 eviction:**
```json
GET /cache/estatisticas
{
  "tamanhoAtual": 5,
  "limite": 5,
  "hits": 1,
  "misses": 7,
  "evictions": 1,
  "hitRatio": 0.125
}
```

---

## 4. Invalidação após PUT

### PUT /conteudos/1 (atualiza conteúdo)
```bash
PUT /conteudos/1  -> 200 OK
```

**Log do cache:**
```
INFO  ConteudoCache : Cache INVALIDADO - id=1
```

### GET /conteudos/1 após PUT (deve ser MISS novamente)
```
HTTP/1.1 200
X-Cache: MISS
```
**Tempo de resposta: ~2071 ms**
**Log do cache:**
```
INFO  ConteudoCache : Cache MISS - id=1 - 2070 ms
```
**Estatísticas:** misses aumenta, evictions mantém.
```json
{
  "hits": 1,
  "misses": 9,
  "evictions": 2
}
```

---

## 5. TTL - Expiração por tempo

O TTL está configurado em 60 segundos.
- O cache verifica `isExpirado(ttlSegundos)` a cada `buscar()` comparando `Instant.now()` com `armazenadoEm`
- Se passou de 60s desde `armazenadoEm`, conta como MISS e busca no banco

**Log quando expira:**
```
INFO  ConteudoCache : Cache MISS - id=3 - 2015 ms (expirado pelo TTL)
```

---

## 6. Limpeza total do cache e estatísticas finais

### DELETE /cache
```bash
DELETE /cache  -> 200 OK
{"mensagem":"Cache limpo com sucesso"}
```

**Log:**
```
INFO  ConteudoCache : Cache LIMPO - 5 itens removidos
```

### Novas buscas após limpar:
```bash
GET /conteudos/1  -> X-Cache: MISS (~2074 ms)
GET /conteudos/1  -> X-Cache: HIT (~30 ms)
```

**Estatísticas finais:**
```json
GET /cache/estatisticas
{
  "tamanhoAtual": 1,
  "limite": 5,
  "hits": 2,
  "misses": 9,
  "evictions": 2,
  "hitRatio": 0.182
}
```

---

## Resumo dos Logs Principais

```
INFO  ConteudoCache : Cache MISS - id=1 - 2094 ms
INFO  ConteudoCache : Cache HIT  - id=1 - 0 ms
INFO  ConteudoCache : Cache MISS - id=2 - 2082 ms
INFO  ConteudoCache : Cache MISS - id=3 - 2082 ms
INFO  ConteudoCache : Cache MISS - id=4 - 2082 ms
INFO  ConteudoCache : Cache MISS - id=5 - 2082 ms
INFO  ConteudoCache : Cache EVICTION - id=1 removido por limite (LRU) - tamanho=5
INFO  ConteudoCache : Cache MISS - id=6 - 2082 ms
INFO  ConteudoCache : Cache INVALIDADO - id=1
INFO  ConteudoCache : Cache MISS - id=1 - 2070 ms
INFO  ConteudoCache : Cache LIMPO - 5 itens removidos
INFO  ConteudoCache : Cache MISS - id=1 - 2074 ms
INFO  ConteudoCache : Cache HIT  - id=1 - 1 ms
```

---

## Headers HTTP Demonstrados

| Requisição | Header X-Cache | Tempo |
|------------|----------------|-------|
| GET /conteudos/1 (1ª) | `MISS` | ~2095 ms |
| GET /conteudos/1 (2ª) | `HIT` | ~30 ms |
| GET /conteudos/2 | `MISS` | ~2082 ms |
| GET /conteudos/6 | `MISS` | ~2082 ms |
| GET /conteudos/1 (após PUT) | `MISS` | ~2071 ms |
| GET /conteudos/1 (após limpar) | `MISS` | ~2074 ms |
| GET /conteudos/1 (2ª após limpar) | `HIT` | ~30 ms |

---

## Arquitetura Implementada

```
┌─────────────────────────────────────────────────────────────┐
│                        CONTROLLER                             │
│  GET /conteudos/{id}    -> converte Entidade -> DTO         │
│  POST /conteudos        -> converte DTO -> Entidade          │
│  PUT /conteudos/{id}    -> converte DTO -> Entidade          │
│  DELETE /conteudos/{id}                                         │
│  GET /cache/estatisticas                                        │
│  DELETE /cache                                                │
└─────────────────────────┬─────────────────────────────────────┘
                          │
                          ▼
┌─────────────────────────────────────────────────────────────┐
│                        SERVICE                                │
│  buscarPorId(id) -> BuscaResultado(conteudo, hit)           │
│    1. cache.buscar(id) -> HIT? retorna                        │
│    2. MISS? Thread.sleep(2000) + repository.findById()        │
│    3. cache.armazenar(conteudo)                               │
│  cadastrar/atualizar/excluir -> invalida cache               │
└─────────────────────────┬─────────────────────────────────────┘
                          │
              ┌───────────┴───────────┐
              ▼                       ▼
┌─────────────────────┐   ┌─────────────────────┐
│   REPOSITORY        │   │   CONTEUDO CACHE    │
│   (JPA/H2 - lento)  │   │   (ConcurrentHashMap)│
│   - findById        │   │   - buscar/armazenar │
│   - save            │   │   - invalidar/limpar │
│   - deleteById      │   │   - LRU + TTL        │
└─────────────────────┘   │   - stats (hits/miss)│
                          └─────────────────────┘
```

---

## Principais Classes

| Arquivo | Responsabilidade |
|---------|------------------|
| `entity/Conteudo.java` | Entidade JPA + implementa `ConteudoCacheItem` |
| `repository/ConteudoRepository.java` | Spring Data JPA |
| `cache/ConteudoCache.java` | Cache LRU + TTL + stats (thread-safe) |
| `cache/EntradaCache.java` | Wrapper com timestamps |
| `service/ConteudoService.java` | Orquestra cache + repository + delay |
| `service/BuscaResultado.java` | Record com entidade + flag hit/miss |
| `controller/ConteudoController.java` | REST endpoints + Mapper + X-Cache header |
| `controller/CacheController.java` | Admin endpoints do cache |
| `mapper/ConteudoMapper.java` | Entidade <-> DTO |
| `dto/*` | Request/Response + Estatisticas |

---

## Como rodar

```bash
cd atividade-nota-02-dupla
./mvnw spring-boot:run
```

Testes rápidos:
```bash
# Criar conteúdo
curl -X POST http://localhost:8080/conteudos -H "Content-Type: application/json" -d '{"titulo":"Teste","corpo":"Corpo","autor":"Autor"}'

# Primeira busca (MISS ~2s)
curl -i http://localhost:8080/conteudos/1

# Segunda busca (HIT ~30ms)
curl -i http://localhost:8080/conteudos/1

# Ver header X-Cache
curl -s -D - http://localhost:8080/conteudos/1 -o /dev/null

# Estatísticas
curl http://localhost:8080/cache/estatisticas

# Limpar cache
curl -X DELETE http://localhost:8080/cache
```

---

## Observações para o Professor

1. **ConcurrentHashMap** usado para thread-safety (requisições simultâneas)
2. **LRU** implementado via `ultimoAcesso` em cada `EntradaCache`
3. **TTL** verificado a cada `buscar()` comparando `Instant.now()` com `armazenadoEm`
4. **Estatísticas** com `AtomicLong` para contadores thread-safe
5. **Logs SLF4J** no padrão: `Cache HIT/MISS - id=X - Y ms`
6. **Invalidação** automática no PUT/DELETE via `cache.invalidar(id)`
7. **Configuração** via `application.properties` com `@Value`
8. **Mapper** separa Entidade (banco) de DTO (API) conforme padrão da disciplina
9. **Header X-Cache** retornado em todas as respostas GET /conteudos/{id}
10. **Record `BuscaResultado`** usado para transportar hit/miss do Service para o Controller