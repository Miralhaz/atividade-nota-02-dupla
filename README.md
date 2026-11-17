# atividade-nota-02-dupla

Projeto para atividade valendo nota 02 - Técnicas Avançadas de Backend

## Como rodar

**Pré-requisitos:**
- Java 21+
- Maven (ou use o wrapper `./mvnw`)

**Passos:**

```bash
cd atividade-nota-02-dupla

./mvnw spring-boot:run
```

A aplicação sobe em `http://localhost:8080`.

**Console do H2 (banco em memória):**
- URL: `http://localhost:8080/h2-console`
- JDBC URL: `jdbc:h2:mem:atividade`
- User: `sa` / Password: *(vazio)*

---

## Endpoints principais

| Método | Rota | Descrição |
|--------|------|-----------|
| GET | `/conteudos/{id}` | Busca conteúdo (usa cache) |
| POST | `/conteudos` | Cadastra conteúdo |
| PUT | `/conteudos/{id}` | Atualiza conteúdo (invalida cache) |
| DELETE | `/conteudos/{id}` | Exclui conteúdo (invalida cache) |
| GET | `/cache/estatisticas` | Mostra stats do cache |
| DELETE | `/cache` | Limpa o cache todo |

**Header de resposta:** `X-Cache: HIT` ou `MISS` indica se veio do cache.

---

## Como o cache funciona

O cache é **em memória** (mapa `ConcurrentHashMap`), implementado na classe `ConteudoCache`.

### Configuração (`application.properties`)
```properties
app.cache.max-itens=5       # limite de itens no cache
app.cache.ttl-segundos=60   # tempo de vida de cada item (segundos)
app.fonte.atraso-ms=2000    # atraso artificial ao buscar no "banco" (simula lentidão)
```

### Políticas

1. **TTL (Time To Live)**  
   Cada item tem validade de 60s (configurável). Se passar disso, vira **MISS** e é removido.

2. **LRU (Least Recently Used)**  
   Quando o cache enche (máx. 5 itens), o item **menos acessado recentemente** sai para dar espaço ao novo. Conta como **eviction**.

3. **Invalidação**  
   Ao atualizar (`PUT`) ou excluir (`DELETE`) um conteúdo, a entrada correspondente é removida do cache. Na próxima busca, virá do banco (MISS) e será guardada novamente.

### Estatísticas
- **HIT**: achou no cache (rápido)
- **MISS**: não achou / expirou → buscou no banco (lento, 2s)
- **Evictions**: itens removidos por limite (LRU)
- **Hit Ratio**: `hits / (hits + misses)`

Acesse `GET /cache/estatisticas` para ver tudo isso em JSON.

### Logs
Cada busca loga no console:
```
Cache HIT - id=1 - 2 ms
Cache MISS - id=2 - 1 ms (expirado pelo TTL)
Cache EVICTION - id=3 removido por limite (LRU) - tamanho=5
Cache INVALIDADO - id=1
```

---

## Estrutura rápida

```
src/main/java/sptech/school/atividade_nota_02_dupla/
├── cache/           # ConteudoCache, EntradaCache, ConteudoCacheItem
├── controller/      # ConteudoController, CacheController
├── service/         # ConteudoService (lógica de cache + banco)
├── repository/      # ConteudoRepository (JPA)
├── entity/          # Conteudo (entidade JPA)
├── dto/             # Request/Response DTOs
└── mapper/          # Conversão entity <-> DTO
```

---

## Testando manualmente (exemplo)

```bash
# Cadastro
curl -X POST http://localhost:8080/conteudos \
  -H "Content-Type: application/json" \
  -d '{"titulo":"Teste","corpo":"Conteúdo","autor":"Aluno"}'

# Busca (primeira vez = MISS, demora ~2s)
curl -i http://localhost:8080/conteudos/1

# Busca de novo (HIT, instantâneo)
curl -i http://localhost:8080/conteudos/1

# Veja stats
curl http://localhost:8080/cache/estatisticas
```

Observe o header `X-Cache` nas respostas.