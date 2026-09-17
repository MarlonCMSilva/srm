# SRM Credit Engine

API para cadastro, simulação e liquidação de recebíveis.

O projeto representa uma operação de aquisição/antecipação de recebíveis, aplicando taxa base, spread específico por tipo de ativo e conversão cambial para operações em USD.

---

## Tecnologias

### Backend

- Java 21
- Spring Boot 3.5.5
- Spring Web
- Spring Data JPA
- Hibernate
- Bean Validation
- PostgreSQL
- Maven

### Frontend

- Next.js
- React
- TypeScript
- Tailwind CSS

### Ferramentas

- Docker
- DBeaver
- Postman
- Git

---

## Arquitetura

O projeto está dividido em backend e frontend:

```text
srm-credit-engine/
├── backend/
└── frontend/
```

### Backend

```text
src/main/java/srmcreditengine/
├── controllers/
│   ├── ReceivableController
│   └── SettlementController
├── entities/
│   ├── Receivable
│   ├── Settlement
│   └── ExchangeRate
├── entities/enums/
│   ├── Currency
│   ├── ReceivableStatus
│   ├── ReceivableType
├── models/
│   ├── ReceivableDTO
│   ├── SettlementDTO
│   ├── SettlementRequestDTO
│   ├── SimulationDTO
│   ├── SimulationRequestDTO
│   └── PricingResult
├── repositories/
│   ├── ExchangeRateRepository
│   ├── ReceivableRepository
│   ├── SettlementRepository
├── services/
│   ├── ReceivableService
│   ├── SettlementService
│   ├── SimulationService
│   └── PricingService
└── strategy/
    ├── PricingStrategy
    ├── DuplicateMercantilePricingStrategy
    └── PreDatedCheckPricingStrategy
```

### Frontend

```text
src/
├── app/
│   ├── page.tsx
│   ├── receivables/
│   │   └── new/
│   ├── simulations/
│   └── settlements/
├── components/
│   ├── ReceivableForm
│   ├── SimulationForm
│   └── SettlementForm
├── lib/
│   ├── api.ts
│   ├── receivableApi.ts
│   ├── simulationApi.ts
│   └── settlementApi.ts
└── types/
    └── index.ts
```

---

## Conceitos principais

### Receivable

Representa o recebível cadastrado no sistema.

Um recebível possui:

- Cedente;
- Tipo;
- Valor de face;
- Prazo;
- Data de vencimento;
- Status.

Status disponíveis:

```text
AVAILABLE
SETTLED
```

### Settlement

Representa a liquidação do recebível pela SRM.

A liquidação registra:

- Recebível relacionado;
- Valor de face;
- Valor presente em BRL;
- Valor liquidado;
- Moeda do pagamento;
- Taxa base utilizada;
- Spread aplicado;
- Taxa de câmbio;
- Data da operação.

### Simulation

A simulação calcula os valores da operação sem persistir dados no banco.

O fluxo é:

```text
Usuário informa os dados
        ↓
Backend calcula o valor presente
        ↓
Frontend exibe os resultados
        ↓
Usuário decide se deseja continuar
```

---

## Regra de cálculo

A taxa base utilizada é:

```text
1,00%
```

O spread depende do tipo de recebível:

| Tipo | Spread |
|---|---:|
| Duplicata mercantil | 1,50% |
| Cheque pré-datado | 2,50% |

A taxa total é calculada por:

```text
taxaTotal = taxaBase + spread
```

O valor presente é calculado por:

```text
valorPresente =
valorDeFace /
(1 + taxaTotal) ^ prazoEmMeses
```

O resultado é arredondado para duas casas decimais.

Para liquidação em BRL:

```text
valorLiquidado = valorPresenteBrl
```

Para liquidação em USD:

```text
valorLiquidado =
valorPresenteBrl / taxaDeCambio
```

A taxa de câmbio é obtida pelo backend.

---

## Pré-requisitos

Antes de executar o projeto, instale:

- Java 21;
- Maven;
- Node.js;
- PostgreSQL ou Docker;
- DBeaver, opcional;
- Git.

Verifique as versões:

```bash
java -version
mvn -version
node -v
npm -v
docker --version
```

---

# Backend

## Configuração do PostgreSQL

Crie um banco de dados:

```sql
CREATE DATABASE srm_credit_engine;
```

A aplicação utiliza as seguintes configurações:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/srm_credit_engine
spring.datasource.username=postgres
spring.datasource.password=postgres
spring.datasource.driver-class-name=org.postgresql.Driver
```

Altere usuário, senha, porta ou nome do banco conforme seu ambiente.

---

## Profile da aplicação

### `application.properties`

```properties
spring.profiles.active=postgres
spring.jpa.open-in-view=false
```

### `application-postgres.properties`

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/srm_credit_engine
spring.datasource.username=postgres
spring.datasource.password=postgres
spring.datasource.driver-class-name=org.postgresql.Driver

spring.jpa.hibernate.ddl-auto=create

spring.sql.init.mode=always
spring.sql.init.continue-on-error=false
spring.jpa.defer-datasource-initialization=true

spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.properties.hibernate.jdbc.time_zone=UTC
```

> Em ambiente de produção, não é recomendado utilizar `spring.jpa.hibernate.ddl-auto=create`, pois as tabelas podem ser recriadas durante a inicialização. Para produção, prefira migrations com Flyway ou Liquibase.

---

## Dependência PostgreSQL

No `pom.xml`, adicione:

```xml
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
    <scope>runtime</scope>
</dependency>
```

---

## Inicialização do banco com Docker

O PostgreSQL pode ser executado com Docker.

### `docker-compose.yml`

```yaml
services:
  postgres:
    image: postgres:16
    container_name: srm-credit-postgres
    restart: unless-stopped
    environment:
      POSTGRES_DB: srm_credit_engine
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: postgres
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data
      - ./docker/init-db.sh:/docker-entrypoint-initdb.d/init-db.sh:ro

volumes:
  postgres_data:
```

---

## Script de dados inicial

Você pode manter um script próprio para dados iniciais, por exemplo:

```text
docker/seed-data.sql
```

Esse script deve conter apenas os inserts:

```sql
INSERT INTO tb_exchange_rate (
    id,
    source_currency,
    target_currency,
    rate,
    effective_at,
    created_at
)
VALUES (
    1,
    'BRL',
    'USD',
    5.4321,
    TIMESTAMP '2026-01-01 09:00:00',
    TIMESTAMP '2026-01-01 09:00:00'
);

INSERT INTO tb_receivable (
    id,
    assignor,
    type,
    face_value,
    term_in_months,
    status,
    due_date
)
VALUES (
    1,
    'Empresa Alpha Ltda',
    'DUPLICATE_MERCANTILE',
    100000.00,
    3,
    'AVAILABLE',
    TIMESTAMP '2026-04-01 00:00:00'
);
```

---

## Executando o backend

Entre na pasta do backend:

```bash
cd backend
```

Execute:

```bash
mvn clean spring-boot:run
```

Ou, utilizando o Maven Wrapper:

```bash
./mvnw clean spring-boot:run
```

A API ficará disponível em:

```text
http://localhost:8080
```

---

# Endpoints da API

## Receivables

### Criar recebível

```http
POST /receivable
```

Body:

```json
{
  "assignor": "Empresa Alpha Ltda",
  "type": "DUPLICATE_MERCANTILE",
  "faceValue": 100000.00,
  "termInMonths": 3,
  "dueDate": "2026-04-01T09:00:00"
}
```

Resposta esperada:

```json
{
  "id": 8,
  "assignor": "Empresa Alpha Ltda",
  "type": "DUPLICATE_MERCANTILE",
  "faceValue": 100000.00,
  "termInMonths": 3,
  "status": "AVAILABLE",
  "dueDate": "2026-04-01T09:00:00"
}
```

### Buscar recebível por ID

```http
GET /receivable/{id}
```

Exemplo:

```http
GET /receivable/1
```

### Listar recebíveis

```http
GET /receivable
```

Com paginação:

```http
GET /receivable?page=0&size=20&sort=id,asc
```

Filtro por cedente:

```http
GET /receivable?assignor=Empresa&page=0&size=20
```

---

## Simulações

### Simular liquidação

```http
POST /settlements/simulation
```

Body:

```json
{
  "assignor": "Empresa Alpha Ltda",
  "type": "DUPLICATE_MERCANTILE",
  "faceValue": 100000.00,
  "termInMonths": 3,
  "dueDate": "2026-04-01T09:00:00",
  "paymentCurrency": "BRL"
}
```

A simulação não cria registros no banco.

Resposta:

```json
{
  "faceValue": 100000.00,
  "type": "DUPLICATE_MERCANTILE",
  "presentValueBrl": 92859.94,
  "settledAmount": 92859.94,
  "paymentCurrency": "BRL",
  "baseRateUsed": 0.0100,
  "spreadUsed": 0.0150,
  "exchangeRateUsed": null,
  "termInMonths": 3
}
```

Para USD:

```json
{
  "assignor": "Empresa Alpha Ltda",
  "type": "DUPLICATE_MERCANTILE",
  "faceValue": 100000.00,
  "termInMonths": 3,
  "dueDate": "2026-04-01T09:00:00",
  "paymentCurrency": "USD"
}
```

---

## Settlements

### Criar liquidação

A liquidação utiliza um recebível já existente:

```http
POST /settlements
```

Body:

```json
{
  "receivableId": 1,
  "paymentCurrency": "BRL"
}
```

Fluxo:

```text
1. Busca o recebível
2. Verifica se está AVAILABLE
3. Calcula o valor presente
4. Cria o Settlement
5. Atualiza o Receivable para SETTLED
```

### Buscar liquidação por ID

```http
GET /settlements/{id}
```

Exemplo:

```http
GET /settlements/1
```

---

# Fluxo completo da aplicação

## Simulação

```text
Frontend
  ↓
POST /settlements/simulation
  ↓
SimulationService
  ↓
PricingService
  ↓
Resultado da simulação
```

A simulação não persiste dados.

## Liquidação

```text
Frontend
  ↓
GET /receivable
  ↓
Usuário seleciona um recebível
  ↓
POST /settlements
  ↓
SettlementService
  ↓
Settlement criado
  ↓
Receivable atualizado para SETTLED
```

---

# Frontend

## Instalação

Entre na pasta:

```bash
cd frontend
```

Instale as dependências:

```bash
npm install
```

Execute:

```bash
npm run dev
```

A aplicação ficará disponível em:

```text
http://localhost:3000
```

---

## Variável de ambiente

Crie o arquivo:

```text
.env.local
```

Com:

```env
NEXT_PUBLIC_API_URL=http://localhost:8080
```

---

## Páginas disponíveis

```text
/
```

Página inicial com navegação.

```text
/receivables/new
```

Cadastro de recebível.

```text
/simulations
```

Simulação da liquidação.

```text
/settlements
```

Listagem de recebíveis e botão para liquidação.

---

# CORS

Como o frontend utiliza a porta `3000` e o backend utiliza a porta `8080`, o backend deve permitir requisições de:

```text
http://localhost:3000
```

Exemplo:

```java
@Configuration
public class CorsConfig {

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {

            @Override
            public void addCorsMappings(
                    CorsRegistry registry
            ) {
                registry.addMapping("/**")
                        .allowedOrigins(
                                "http://localhost:3000"
                        )
                        .allowedMethods(
                                "GET",
                                "POST",
                                "PUT",
                                "DELETE",
                                "OPTIONS"
                        )
                        .allowedHeaders("*");
            }
        };
    }
}
```

---

# Testes rápidos com Postman

## Criar recebível

```http
POST http://localhost:8080/receivable
Content-Type: application/json
```

```json
{
  "assignor": "Empresa Beta S.A.",
  "type": "PRE_DATED_CHECK",
  "faceValue": 25000.00,
  "termInMonths": 2,
  "dueDate": "2026-03-01T09:00:00"
}
```

## Simular em BRL

```http
POST http://localhost:8080/settlements/simulation
Content-Type: application/json
```

```json
{
  "assignor": "Empresa Alpha Ltda",
  "type": "DUPLICATE_MERCANTILE",
  "faceValue": 100000.00,
  "termInMonths": 3,
  "dueDate": "2026-04-01T09:00:00",
  "paymentCurrency": "BRL"
}
```

## Liquidar recebível

```http
POST http://localhost:8080/settlements
Content-Type: application/json
```

```json
{
  "receivableId": 1,
  "paymentCurrency": "BRL"
}
```

## Consultar recebíveis

```http
GET http://localhost:8080/receivable?page=0&size=20
```

---

# Status HTTP esperados

| Operação | Status |
|---|---:|
| Criar recebível | `201 Created` |
| Criar liquidação | `201 Created` |
| Simular liquidação | `200 OK` |
| Buscar recurso | `200 OK` |
| Recurso não encontrado | `404 Not Found` |
| Dados inválidos | `400 Bad Request` |
| Recebível já liquidado | `409 Conflict` |

---

# Decisões de projeto

## Simulação separada da liquidação

A simulação utiliza um DTO e um service próprios:

```text
SimulationRequestDTO
SimulationDTO
SimulationService
```

Ela reutiliza o `PricingService`, mas não salva dados no banco.

## Liquidação baseada em recebível existente

A liquidação real utiliza:

```json
{
  "receivableId": 1,
  "paymentCurrency": "BRL"
}
```

Isso garante que a operação esteja relacionada a um recebível existente.

## Estratégia de precificação

O spread é selecionado por uma estratégia compatível com o tipo do recebível:

```text
PricingStrategy
├── DuplicateMercantilePricingStrategy
└── PreDatedCheckPricingStrategy
```

Essa abordagem evita colocar vários `if` ou `switch` dentro do `PricingService`.

## Idempotência

A operação garante uma única liquidação por recebível por meio do status:

```text
AVAILABLE → SETTLED
```

e da verificação de existência de liquidação para o recebível.

---

# Possíveis melhorias futuras

- Adicionar autenticação e autorização;
- Implementar migrations com Flyway;
- Criar testes de integração com PostgreSQL;
- Criar paginação no frontend;
- Adicionar filtro por status;
- Adicionar confirmação antes da liquidação;
- Criar tratamento global de exceções;
- Adicionar documentação OpenAPI/Swagger;
- Criar Dockerfile para backend;
- Criar Dockerfile para frontend;
- Configurar Docker Compose para subir toda a aplicação;
- Adicionar pipeline de integração contínua.

---

# Autor

Projeto desenvolvido como parte de um case técnico para demonstração de:

- Desenvolvimento de APIs REST;
- Arquitetura em camadas;
- Spring Boot;
- JPA/Hibernate;
- PostgreSQL;
- Estratégias de precificação;
- Integração entre backend e frontend;
- Desenvolvimento de interfaces com Next.js.