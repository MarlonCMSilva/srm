# SRM Credit Engine — Especificação funcional e técnica

## 1. Objetivo

O SRM Credit Engine será uma aplicação para simular e liquidar recebíveis de diferentes tipos e moedas de pagamento.

A aplicação deverá:

- Calcular o valor presente de um recebível;
- Aplicar taxa base e spread conforme o tipo do ativo;
- Converter o valor para USD quando necessário;
- Permitir a liquidação do recebível;
- Registrar a liquidação para fins de auditoria;
- Impedir liquidações duplicadas;
- Disponibilizar um extrato de liquidações com filtros básicos.

A solução será implementada como um monólito modular, com backend em Java/Spring Boot, frontend em Next.js e banco de dados relacional PostgreSQL.

## 2. Premissas de negócio

### 2.1 Tipos de recebível

Serão suportados inicialmente os seguintes tipos:

| Tipo | Spread |
| --- | --- |
| Duplicata Mercantil | 1,50% ao mês |
| Cheque Pré-datado | 2,50% ao mês |

O spread será selecionado por meio do padrão Strategy, permitindo adicionar novos tipos de recebível sem alterar diretamente o fluxo principal de precificação.

### 2.2 Taxa base

A taxa base adotada para o case será:

```text
1,00% ao mês
```

A taxa será tratada internamente como `0,01`.

Nesta primeira versão, a taxa base será configurada na aplicação. Em um cenário real, deveria existir uma estrutura de configuração ou uma tabela de histórico para permitir a alteração da taxa ao longo do tempo sem modificar o código.

### 2.3 Fórmula de precificação

Será utilizada a fórmula de juros compostos definida no enunciado:

```text
Valor Presente =
Valor de Face / (1 + Taxa Base + Spread) ^ Prazo
```

Exemplo para uma duplicata:

```text
Taxa base: 1,00%
Spread: 1,50%
Taxa total: 2,50% ao mês
```

### 2.4 Prazo

O prazo será representado em meses inteiros.

Para os golden cases, o prazo será recebido diretamente como quantidade de meses, pois os casos de aferição fornecem essa informação explicitamente.

A aplicação também poderá armazenar a data de vencimento do recebível. Em uma evolução futura, o prazo poderá ser calculado automaticamente a partir da data da operação e da data de vencimento.

### 2.5 Moedas e câmbio

A primeira versão suportará:

- BRL;
- USD.

Quando a moeda de pagamento for BRL, o valor presente será exibido em BRL.

Quando a moeda de pagamento for USD:

- O valor presente será calculado em BRL;
- O valor em BRL será arredondado para duas casas;
- O valor arredondado será convertido pela taxa BRL/USD;
- O resultado em USD será arredondado para duas casas.

A conversão será feita pela fórmula:

```text
Valor em USD = Valor Presente em BRL / Taxa BRL/USD
```

As taxas de câmbio possuirão:

- Moeda de origem;
- Moeda de destino;
- Valor da taxa;
- Data e hora de vigência.

Para esta versão, a taxa de câmbio poderá ser cadastrada ou atualizada manualmente por endpoint. Na liquidação, a aplicação utilizará a taxa vigente mais recente aplicável ao momento da operação e armazenará essa taxa no registro da liquidação.

Uma liquidação já registrada não será recalculada caso a taxa de câmbio seja alterada posteriormente.

## 3. Precisão numérica e arredondamento

### 3.1 Aplicação

Valores monetários e taxas serão representados com `BigDecimal`.

Não serão utilizados `float` ou `double` para cálculos financeiros, devido aos possíveis erros de representação binária.

### 3.2 Banco de dados

Os valores monetários serão persistidos utilizando tipos decimais, por exemplo:

```text
NUMERIC(19, 6)
```

Para valores apresentados ao usuário, será utilizada escala de duas casas decimais.

As taxas poderão utilizar uma escala maior, permitindo armazenar valores como:

```text
5,4321
```

### 3.3 Política de arredondamento

Será utilizado arredondamento:

```text
HALF_EVEN
```

Também conhecido como arredondamento bancário.

A regra será:

- Não arredondar durante as etapas intermediárias do cálculo;
- Arredondar o valor presente em BRL somente no resultado final;
- Em operações cross-currency, utilizar o valor presente em BRL já arredondado;
- Arredondar o resultado final da conversão para duas casas decimais;
- Calcular o deságio em BRL com base no valor de face e no valor presente em BRL arredondado.

### 3.4 Golden cases

O motor deverá passar pelos seguintes casos:

| Caso | Resultado esperado |
| --- | --- |
| Duplicata de R$ 100.000,00, prazo de 3 meses, pagamento em BRL | R$ 92.859,94 |
| Cheque de R$ 25.000,00, prazo de 2 meses, pagamento em BRL | R$ 23.337,77 |
| Duplicata de R$ 100.000,00, prazo de 3 meses, pagamento em USD, câmbio 5,4321 | US$ 17.094,67 |

O deságio do primeiro caso deverá ser:

```text
R$ 100.000,00 - R$ 92.859,94 = R$ 7.140,06
```

## 4. Modelo de dados e auditabilidade

A solução deverá possuir, no mínimo, as seguintes estruturas:

### 4.1 Recebível

Deverá armazenar:

- Identificador;
- Cedente;
- Tipo;
- Valor de face;
- Moeda original;
- Data de vencimento;
- Prazo;
- Status;
- Data de criação.

Os status iniciais serão:

- `AVAILABLE`;
- `SETTLED`.

### 4.2 Taxa de câmbio

Deverá armazenar:

- Identificador;
- Moeda de origem;
- Moeda de destino;
- Taxa;
- Data e hora de vigência;
- Data de criação.

### 4.3 Liquidação

A liquidação deverá ser tratada como um registro imutável e deverá armazenar os valores utilizados no momento da operação, incluindo:

- Identificador;
- Identificador do recebível;
- Cedente;
- Valor de face;
- Valor presente em BRL;
- Valor liquidado;
- Moeda de pagamento;
- Taxa de câmbio utilizada;
- Taxa base utilizada;
- Spread utilizado;
- Prazo utilizado;
- Chave de idempotência;
- Data e hora da liquidação.

Depois de registrada, a liquidação não poderá ser alterada pela aplicação.

O recebível poderá possuir apenas uma liquidação. Essa regra deverá ser reforçada por restrição de unicidade no banco de dados.

## 5. Transação e idempotência

A liquidação deverá ser executada em uma única transação de banco de dados.

As seguintes operações deverão ocorrer atomicamente:

- Validar o recebível;
- Verificar se ele ainda está disponível;
- Calcular o valor da liquidação;
- Criar o registro da liquidação;
- Atualizar o status do recebível para `SETTLED`.

Caso qualquer etapa falhe, nenhuma alteração deverá ser confirmada.

A API deverá aceitar uma chave de idempotência para a operação de liquidação. Quando a mesma requisição for repetida:

- A aplicação não deverá criar uma segunda liquidação;
- O resultado original deverá ser retornado;
- A idempotência deverá ser reforçada por uma restrição única no banco.

## 6. API prevista

### Recebíveis

```text
POST /api/receivables
GET /api/receivables
GET /api/receivables/{id}
```

### Taxas de câmbio

```text
POST /api/exchange-rates
GET /api/exchange-rates
```

### Simulação

```text
POST /api/pricing/simulations
```

A simulação não deverá persistir uma liquidação.

### Liquidação

```text
POST /api/settlements
GET /api/settlements/{id}
```

A requisição de liquidação deverá conter a chave de idempotência.

### Extrato

```text
GET /api/settlements
```

Filtros previstos:

- Período inicial;
- Período final;
- Cedente;
- Moeda.

A API deverá utilizar códigos HTTP semânticos, como:

- `200 OK` para consultas e operações concluídas;
- `201 Created` para criação de recursos;
- `400 Bad Request` para dados inválidos;
- `404 Not Found` para recursos inexistentes;
- `409 Conflict` para recebível já liquidado ou conflito de idempotência;
- `500 Internal Server Error` para falhas inesperadas.

A documentação da API será disponibilizada por OpenAPI/Swagger.

## 7. Frontend

O frontend será desenvolvido em Next.js e terá duas funcionalidades principais.

### 7.1 Painel de simulação

O operador poderá informar:

- Cedente;
- Valor de face;
- Tipo de recebível;
- Prazo ou data de vencimento;
- Moeda de pagamento;
- Taxa de câmbio, quando necessário.

A tela exibirá:

- Taxa base;
- Spread;
- Valor presente em BRL;
- Valor final na moeda de pagamento;
- Deságio;
- Taxa cambial utilizada.

A simulação será realizada pelo backend, que será a fonte oficial das regras de negócio.

### 7.2 Histórico de liquidações

Será disponibilizada uma listagem com:

- Data da liquidação;
- Cedente;
- Tipo;
- Valor de face;
- Valor liquidado;
- Moeda;
- Taxa de câmbio;
- Status.

Para o escopo júnior, a listagem poderá ser simples. A paginação server-side e filtros mais avançados serão considerados melhorias futuras.

## 8. Segurança

A aplicação deverá:

- Utilizar queries parametrizadas ou repositórios seguros;
- Validar todos os dados recebidos pela API;
- Não confiar apenas nas validações do frontend;
- Evitar exposição de informações sensíveis nos erros;
- Não permitir alteração de uma liquidação registrada;
- Controlar valores monetários e taxas com precisão decimal;
- Registrar logs sem expor dados desnecessários;
- Utilizar variáveis de ambiente para configurações sensíveis;
- Impedir SQL Injection;
- Garantir que a liquidação seja autorizada apenas para recebíveis válidos e disponíveis.

Autenticação e autorização não fazem parte do escopo mínimo, pois não foram detalhadas no enunciado. Entretanto, a solução deverá ser estruturada de forma que possam ser adicionadas posteriormente.

## 9. Critérios de aceite

### Funcionais

- O operador consegue simular uma duplicata;
- O operador consegue simular um cheque pré-datado;
- O operador consegue realizar liquidação em BRL;
- O operador consegue realizar liquidação em USD;
- O sistema aplica corretamente o spread por tipo;
- O sistema reproduz os três golden cases ao centavo;
- Uma liquidação é registrada com os valores e a taxa de câmbio efetivamente utilizados;
- Uma mesma requisição repetida não gera liquidação duplicada;
- Um recebível já liquidado não pode ser liquidado novamente;
- O extrato permite consultar liquidações por período, cedente e moeda.

### Técnicos

- Utilização de BigDecimal no backend;
- Arredondamento HALF_EVEN;
- Transação envolvendo a liquidação e atualização do recebível;
- Testes automatizados do motor de precificação;
- Separação entre controller, aplicação, negócio e persistência;
- Documentação da API;
- README com instruções funcionais de execução.

### Usabilidade

- Campos obrigatórios devem ser indicados;
- Valores inválidos devem apresentar mensagens claras;
- O botão de liquidação deve ser desabilitado durante o processamento;
- O operador deve confirmar os dados antes da liquidação;
- Valores e datas devem ser exibidos em formato apropriado;
- Erros da API devem ser apresentados de forma compreensível.

### Desempenho

Para o escopo inicial:

- O cálculo de simulação deverá responder em até 500 ms em ambiente local, desconsiderando latência de rede;
- A consulta do extrato deverá utilizar filtros no banco;
- A API deverá evitar carregar registros desnecessários em memória;
- A evolução para paginação server-side será considerada quando o volume de dados exigir.

## 10. Perguntas para o negócio

Em um projeto real, eu validaria as seguintes questões:

- O prazo deve ser informado em meses ou calculado a partir da data de vencimento?
- Como devem ser tratados meses incompletos?
- A taxa base é fixa ou possui histórico de vigência?
- Qual taxa de câmbio deve ser utilizada na liquidação?
- A taxa de câmbio precisa de aprovação operacional?
- O recebível pode ser estornado após a liquidação?
- Existe necessidade de liquidação parcial?
- O sistema deverá suportar outras moedas além de BRL e USD?
- O valor de face sempre estará em BRL?
- Como devem ser tratados feriados e finais de semana?
- A mesma chave de idempotência pode ser reutilizada com dados diferentes?
- Existem perfis diferentes de operador e permissões de acesso?
- Qual é o volume esperado de recebíveis e liquidações?
- Qual é o prazo máximo aceitável para uma simulação e para uma liquidação?

## 11. Fora do escopo inicial

Não serão implementados inicialmente:

- Microserviços;
- Integração real com provedor externo de câmbio;
- Autenticação e autorização completas;
- Liquidação parcial;
- Estorno;
- Arquitetura orientada a eventos;
- Alta disponibilidade;
- Paginação avançada no frontend;
- Escalabilidade para milhões de transações por minuto;
- Observabilidade avançada.

Esses itens poderão ser adicionados posteriormente, conforme novas necessidades de negócio e volume operacional.