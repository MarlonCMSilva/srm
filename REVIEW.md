# Code Review — Endpoint de Liquidação

## Resumo executivo

O endpoint apresenta riscos críticos de segurança, consistência financeira e integridade de dados.

Os principais problemas identificados são:

- Vulnerabilidade de SQL Injection;
- Ausência de validação de entrada;
- Possível erro de unidade nas taxas;
- Falta de transação atômica;
- Exceções silenciosamente ignoradas;
- Possibilidade de liquidação duplicada;
- Falta de validação do recebível;
- Uso inadequado de `Number` para valores monetários;
- Retorno de sucesso mesmo quando a operação falha;
- Falta de tratamento adequado da taxa de câmbio.

A recomendação é não aprovar este código para produção no estado atual.

---

## 1. SQL Injection

### Problema

As queries são montadas utilizando interpolação direta dos valores recebidos pelo cliente:

```ts
`SELECT * FROM receivables WHERE id = ${receivableId}`
```

e:

```ts
`INSERT INTO settlements (receivable_id, amount, currency)
 VALUES (${receivableId}, ${finalAmount.toFixed(2)}, '${currency}')`
```

O valor de `currency`, especialmente, é inserido diretamente na query SQL.

Um atacante poderia enviar um valor malicioso no body da requisição e alterar a query executada.

### Impacto

- Consulta ou alteração não autorizada de dados;
- Inserção de comandos SQL arbitrários;
- Comprometimento do banco de dados;
- Possível perda ou exposição de informações.

### Recomendação

Utilizar queries parametrizadas:

```ts
const receivable = await db.queryOne(
  `SELECT *
     FROM receivables
    WHERE id = $1`,
  [receivableId]
);
```

Para o insert:

```ts
await db.query(
  `INSERT INTO settlements
      (receivable_id, amount, currency)
   VALUES ($1, $2, $3)`,
  [receivableId, finalAmount, currency]
);
```

Nenhum valor recebido da requisição deve ser concatenado diretamente em uma query.

---

## 2. Ausência de validação da entrada

### Problema

O código extrai os valores do body sem validar:

```ts
const { receivableId, currency } = req.body;
```

Não há validação para:

- Existência de `receivableId`;
- Tipo numérico do ID;
- ID maior que zero;
- Moeda permitida;
- Presença do body;
- Formato da requisição;
- Permissão do usuário para liquidar o recebível.

### Impacto

Entradas inválidas podem causar:

- Erros inesperados;
- Consultas incorretas;
- Valores `NaN`;
- Comportamento inconsistente;
- Tentativas de liquidação com moedas não suportadas.

### Recomendação

Validar a entrada antes de acessar o banco:

```ts
const { receivableId, currency } = req.body;

if (
  !Number.isInteger(receivableId) ||
  receivableId <= 0
) {
  return res.status(400).json({
    error: "receivableId deve ser um inteiro positivo"
  });
}

const supportedCurrencies = ["BRL", "USD"];

if (!supportedCurrencies.includes(currency)) {
  return res.status(400).json({
    error: "Moeda não suportada"
  });
}
```

Em uma aplicação maior, recomenda-se usar uma biblioteca de validação, como Zod, Joi ou class-validator.

---

## 3. Recebível inexistente não é tratado

### Problema

O código utiliza imediatamente:

```ts
const spread =
  receivable.type === "DUPLICATA"
    ? 1.5
    : 2.5;
```

Se `db.queryOne` não encontrar um recebível, `receivable` poderá ser `null` ou `undefined`. Nesse caso, ocorrerá um erro ao acessar:

```ts
receivable.type
```

### Impacto

- Erro interno não tratado;
- Retorno possivelmente incorreto ao cliente;
- Ausência de resposta clara indicando que o recebível não existe.

### Recomendação

Validar o resultado da consulta:

```ts
if (!receivable) {
  return res.status(404).json({
    error: "Recebível não encontrado"
  });
}
```

---

## 4. Ausência de validação de status

### Problema

O endpoint não verifica o status atual do recebível antes da liquidação.

Consequentemente, pode tentar liquidar:

- Recebíveis já liquidados;
- Recebíveis cancelados;
- Recebíveis bloqueados;
- Recebíveis indisponíveis.

### Impacto

A mesma operação financeira pode ser realizada mais de uma vez.

### Recomendação

Validar o status no backend:

```ts
if (receivable.status !== "AVAILABLE") {
  return res.status(409).json({
    error: "Recebível não está disponível para liquidação"
  });
}
```

Essa validação também deve ser reforçada no banco de dados por meio de constraints ou locking apropriado.

---

## 5. Erro potencial na unidade das taxas

### Problema

O código define:

```ts
const BASE_RATE = 1.0;
```

e:

```ts
const spread =
  receivable.type === "DUPLICATA"
    ? 1.5
    : 2.5;
```

Depois calcula:

```ts
Math.pow(1 + BASE_RATE + spread, receivable.term)
```

Se os valores representam percentuais mensais, a taxa base de 1% deveria ser representada como:

```ts
0.01
```

e o spread de 1,5% como:

```ts
0.015
```

O cálculo atual interpreta:

```text
BASE_RATE = 1.0   → 100%
spread = 1.5      → 150%
```

### Impacto

O valor presente ficará completamente incorreto.

Com:

```ts
1 + 1.0 + 1.5
```

o fator mensal será:

```text
3,5
```

em vez de:

```text
1,025
```

para taxa base de 1% e spread de 1,5%.

### Recomendação

Representar taxas percentuais como frações decimais:

```ts
const BASE_RATE = 0.01;

const spread =
  receivable.type === "DUPLICATA"
    ? 0.015
    : 0.025;
```

Ou utilizar uma função explícita:

```ts
const percentToDecimal = (value: number) =>
  value / 100;
```

Mais importante: a unidade precisa ser documentada e padronizada em toda a aplicação.

---

## 6. Uso de `Number` para valores financeiros

### Problema

O código utiliza o tipo JavaScript `number` para calcular valores monetários:

```ts
const presentValue =
  receivable.face_value /
  Math.pow(
    1 + BASE_RATE + spread,
    receivable.term
  );
```

O tipo `number` utiliza ponto flutuante binário e pode gerar pequenas imprecisões em cálculos financeiros.

Além disso, o arredondamento só é aplicado na resposta e no SQL:

```ts
finalAmount.toFixed(2)
```

O valor interno continua sendo um `number`.

### Impacto

- Diferenças de centavos;
- Divergência entre simulação e liquidação;
- Inconsistências em valores acumulados;
- Possíveis erros contábeis.

### Recomendação

Executar cálculos monetários com uma biblioteca decimal, como:

- `decimal.js`;
- `big.js`;
- ` Dinero.js`.

Exemplo com `decimal.js`:

```ts
import Decimal from "decimal.js";

const baseRate = new Decimal("0.01");
const spread = new Decimal("0.015");
const faceValue = new Decimal(
  receivable.face_value.toString()
);

const totalRate = baseRate.plus(spread);

const divisor = new Decimal(1)
  .plus(totalRate)
  .pow(receivable.term);

const presentValue = faceValue.div(divisor);
```

O valor final deve ser arredondado uma única vez, conforme a regra de negócio:

```ts
const amount = presentValue.toDecimalPlaces(
  2,
  Decimal.ROUND_HALF_EVEN
);
```

---

## 7. Falta de transação

### Problema

A liquidação executa duas operações independentes:

```ts
await db.query(
  `INSERT INTO settlements ...`
);
```

e:

```ts
await db.query(
  `UPDATE receivables
      SET status = 'SETTLED'
    WHERE id = ${receivableId}`
);
```

Se o insert funcionar, mas o update falhar, o banco ficará inconsistente.

### Exemplo

```text
Settlement criado
Receivable continua AVAILABLE
```

Uma nova requisição poderá liquidar novamente o mesmo recebível.

### Impacto

- Liquidações duplicadas;
- Estado inconsistente;
- Divergência entre as tabelas;
- Necessidade de correção manual no banco.

### Recomendação

Executar as operações dentro de uma transação:

```ts
const client = await db.connect();

try {
  await client.query("BEGIN");

  // Buscar e bloquear o recebível
  const receivableResult = await client.query(
    `SELECT *
       FROM receivables
      WHERE id = $1
      FOR UPDATE`,
    [receivableId]
  );

  const receivable = receivableResult.rows[0];

  if (!receivable) {
    throw new NotFoundError(
      "Recebível não encontrado"
    );
  }

  if (receivable.status !== "AVAILABLE") {
    throw new ConflictError(
      "Recebível não está disponível"
    );
  }

  await client.query(
    `INSERT INTO settlements
        (receivable_id, amount, currency)
     VALUES ($1, $2, $3)`,
    [receivableId, amount, currency]
  );

  await client.query(
    `UPDATE receivables
        SET status = 'SETTLED'
      WHERE id = $1`,
    [receivableId]
  );

  await client.query("COMMIT");
} catch (error) {
  await client.query("ROLLBACK");
  throw error;
} finally {
  client.release();
}
```

O `INSERT` e o `UPDATE` devem ser confirmados ou revertidos juntos.

---

## 8. Exceção ignorada

### Problema

O bloco `catch` não trata o erro:

```ts
} catch (e) {
  // se falhar aqui, o insert já rodou, então segue o jogo
}
```

O comentário indica que o sistema deliberadamente continua depois de uma falha.

### Impacto

- O cliente recebe sucesso mesmo quando a liquidação falhou;
- O erro desaparece dos logs;
- O problema não pode ser diagnosticado adequadamente;
- A API pode responder com dados que não existem no banco.

### Recomendação

O erro deve ser registrado e propagado:

```ts
} catch (error) {
  logger.error(
    {
      error,
      receivableId,
      currency
    },
    "Erro ao realizar liquidação"
  );

  return res.status(500).json({
    error: "Não foi possível concluir a liquidação"
  });
}
```

Se a operação estiver dentro de transação:

```ts
await client.query("ROLLBACK");
```

deve ser executado antes da resposta de erro.

Nunca retornar `200 OK` após uma falha de persistência.

---

## 9. Status HTTP inadequado

### Problema

O endpoint sempre retorna:

```ts
res.status(200).json(...)
```

Mesmo sendo uma criação de recurso, a operação deveria retornar um status que indique criação.

### Recomendação

Para uma nova liquidação:

```ts
res.status(201).json({
  ok: true,
  settlementId,
  amount: amount.toFixed(2)
});
```

Sugestão de respostas:

| Situação | Status |
|---|---:|
| Liquidação criada | `201 Created` |
| Request inválido | `400 Bad Request` |
| Recebível não encontrado | `404 Not Found` |
| Recebível já liquidado | `409 Conflict` |
| Taxa de câmbio indisponível | `503 Service Unavailable` |
| Erro inesperado | `500 Internal Server Error` |

---

## 10. Possibilidade de liquidação duplicada

### Problema

Duas requisições simultâneas podem executar o fluxo ao mesmo tempo:

```text
Requisição A verifica recebível
Requisição B verifica recebível
Requisição A insere settlement
Requisição B insere settlement
```

Como não há lock, constraint ou verificação transacional, o mesmo recebível pode ser liquidado duas vezes.

### Recomendação

Aplicar três proteções:

1. Transação;
2. Bloqueio pessimista com `FOR UPDATE`;
3. Constraint `UNIQUE` em `settlements.receivable_id`.

Exemplo:

```sql
ALTER TABLE settlements
ADD CONSTRAINT uq_settlements_receivable
UNIQUE (receivable_id);
```

A validação na aplicação melhora a mensagem de erro, mas a constraint do banco é a proteção definitiva contra duplicidade.

---

## 11. Taxa de câmbio sem validação

### Problema

O código faz:

```ts
finalAmount = presentValue / rate;
```

sem validar se `rate`:

- Existe;
- É numérica;
- É maior que zero;
- Está vigente;
- Está na direção correta.

### Impacto

Podem ocorrer:

- Divisão por zero;
- Resultado `Infinity`;
- Resultado `NaN`;
- Conversão usando uma cotação inválida;
- Uso de taxa antiga ou indisponível.

### Recomendação

Validar a taxa:

```ts
if (
  currency === "USD" &&
  (!rate ||
    !Number.isFinite(rate) ||
    rate <= 0)
) {
  return res.status(503).json({
    error: "Taxa de câmbio indisponível"
  });
}
```

Também é importante definir explicitamente o significado da taxa:

```text
1 USD = 5,43 BRL
```

Nesse caso:

```text
valorEmUSD = valorEmBRL / taxaBRLPorUSD
```

---

## 12. Tipo de recebível não suportado

### Problema

O código considera qualquer tipo diferente de `"DUPLICATA"` como cheque:

```ts
const spread =
  receivable.type === "DUPLICATA"
    ? 1.5
    : 2.5;
```

Assim, um valor inválido, como:

```text
"UNKNOWN"
```

receberia automaticamente o spread do cheque.

### Recomendação

Validar explicitamente os tipos suportados:

```ts
const spreads: Record<string, number> = {
  DUPLICATA: 0.015,
  CHEQUE_PRE_DATADO: 0.025
};

const spread = spreads[receivable.type];

if (spread === undefined) {
  return res.status(422).json({
    error: "Tipo de recebível não suportado"
  });
}
```

Melhor ainda seria usar um enum ou uma estratégia específica por tipo.

---

## 13. Falta de autenticação e autorização

### Problema

O código não mostra qualquer verificação de identidade ou permissão.

Não é possível saber se o usuário atual pode:

- Consultar o recebível;
- Liquidar o recebível;
- Operar em nome da empresa;
- Realizar operações em determinada moeda.

### Recomendação

O endpoint deve ser protegido por autenticação e autorização, por exemplo:

```ts
app.post(
  "/settlements",
  authenticate,
  authorize("SETTLE_RECEIVABLE"),
  settlementController.settle
);
```

Também deve ser registrada a identidade do operador para auditoria.

---

## 14. Falta de auditoria

Uma liquidação financeira deveria registrar, além dos valores:

- Usuário ou sistema responsável;
- Data e hora;
- Taxa base;
- Spread;
- Taxa de câmbio;
- IP ou origem da requisição;
- Identificador da operação;
- Dados utilizados no cálculo.

A tabela `settlements` deveria armazenar os valores efetivamente utilizados no momento da liquidação, para permitir auditoria posterior.

---

## 15. Cálculo e persistência precisam usar a mesma regra

A simulação e a liquidação devem compartilhar o mesmo serviço de cálculo.

Não é recomendável calcular no frontend e aceitar o valor enviado pelo cliente. O valor final deve ser recalculado no backend durante a liquidação.

O cliente deve enviar apenas os dados necessários, por exemplo:

```json
{
  "receivableId": 1,
  "currency": "USD"
}
```

O backend deve buscar os dados atuais do recebível e recalcular:

```text
valor de face
prazo
tipo
spread
taxa de câmbio
valor presente
valor final
```

O valor enviado pelo cliente nunca deve ser considerado confiável.

---
