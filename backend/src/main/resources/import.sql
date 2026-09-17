-- ============================================================
-- SRM Credit Engine - Dados iniciais para desenvolvimento
-- Banco esperado: PostgreSQL
-- ============================================================

-- ============================================================
-- Premissas utilizadas neste script
-- ============================================================
--
-- 1. Enums persistidos como texto:
--    - BRL / USD
--    - AVAILABLE / SETTLED
--    - DUPLICATE_MERCANTILE / PRE_DATED_CHECK
--
-- 2. Nomes das colunas seguem snake_case:
--    - faceValue        -> face_value
--    - termInMonths     -> term_in_months
--    - presentValueBrl  -> present_value_brl
--
-- 3. A tabela de câmbio foi considerada como:
--    tb_exchange_rate
--
-- 4. A taxa BRL/USD representa quantos reais equivalem a um dólar.
--    Conversão:
--
--    valor_em_usd = valor_em_brl / taxa_brl_usd
--
-- 5. Os IDs foram informados manualmente para facilitar os testes.
-- ============================================================


-- ============================================================
-- Limpeza opcional
-- ============================================================
--
-- Descomente estas linhas caso queira recriar a massa de dados
-- sempre que a aplicação iniciar.
--
-- Atenção: isso apagará dados existentes.
-- ============================================================

-- DELETE FROM tb_settlement;
-- DELETE FROM tb_receivable;
-- DELETE FROM tb_exchange_rate;


-- ============================================================
-- TAXAS DE CÂMBIO
-- ============================================================
INSERT INTO tb_exchange_rate (id, source_currency, target_currency, rate, effective_at, created_at) VALUES (1, 'BRL', 'USD', 5.4321, TIMESTAMP '2026-01-01 09:00:00', TIMESTAMP '2026-01-01 09:00:00');
INSERT INTO tb_exchange_rate (id, source_currency, target_currency, rate, effective_at, created_at) VALUES (2, 'BRL', 'USD', 5.2500, TIMESTAMP '2026-01-01 09:00:00', TIMESTAMP '2026-01-01 09:00:00');
INSERT INTO tb_exchange_rate (id, source_currency, target_currency, rate, effective_at, created_at) VALUES (3, 'BRL', 'USD', 5.5000, TIMESTAMP '2026-01-01 09:00:00', TIMESTAMP '2026-01-01 09:00:00');


-- ============================================================
-- RECEBÍVEIS DISPONÍVEIS
-- Utilizados para testar:
-- - Simulação
-- - Liquidação em BRL
-- - Liquidação em USD
-- - Idempotência
-- ============================================================

-- C1 - Duplicata Mercantil
-- Valor de face: R$ 100.000,00
-- Prazo: 3 meses
-- Taxa base: 1,00% a.m.
-- Spread: 1,50% a.m.
-- Valor presente esperado: R$ 92.859,94

INSERT INTO tb_receivable (id, assignor, type, face_value, term_in_months, status, due_date) VALUES (1, 'Empresa Alpha Ltda', 'DUPLICATE_MERCANTILE', 100000.00, 3, 'AVAILABLE', DATE '2026-04-01');


-- C2 - Cheque Pré-datado
-- Valor de face: R$ 25.000,00
-- Prazo: 2 meses
-- Taxa base: 1,00% a.m.
-- Spread: 2,50% a.m.
-- Valor presente esperado: R$ 23.337,77

INSERT INTO tb_receivable (id, assignor, type, face_value, term_in_months, status, due_date) VALUES (2, 'Empresa Beta S.A.', 'PRE_DATED_CHECK', 25000.00, 2, 'AVAILABLE', DATE '2026-03-01');


-- C3 - Duplicata Mercantil com pagamento em USD
-- Valor de face: R$ 100.000,00
-- Prazo: 3 meses
-- Câmbio: 5,4321 BRL/USD
-- Valor presente esperado em BRL: R$ 92.859,94
-- Valor esperado em USD: US$ 17.094,67

INSERT INTO tb_receivable (id, assignor, type, face_value, term_in_months, status, due_date)VALUES (3, 'Empresa Gamma Comércio Ltda', 'DUPLICATE_MERCANTILE', 100000.00, 3, 'AVAILABLE', DATE '2026-04-01');


-- Recebível adicional disponível para testes de validação
INSERT INTO tb_receivable (id, assignor, type, face_value, term_in_months, status, due_date) VALUES (5, 'Empresa Horizonte S.A.', 'DUPLICATE_MERCANTILE', 80000.00, 2, 'SETTLED', DATE '2025-12-15');
INSERT INTO tb_receivable (id, assignor, type, face_value, term_in_months, status, due_date) VALUES (6, 'Empresa Nova Era Ltda', 'PRE_DATED_CHECK', 30000.00, 2, 'SETTLED', DATE '2025-11-20');
INSERT INTO tb_receivable (id, assignor, type, face_value, term_in_months, status, due_date) VALUES (7, 'Empresa Sol Nascente S.A.', 'DUPLICATE_MERCANTILE', 120000.00, 4, 'SETTLED', DATE '2025-10-10');


-- ============================================================
-- RECEBÍVEIS JÁ LIQUIDADOS
-- Utilizados para testar:
-- - Extrato
-- - Consulta de liquidação
-- - Filtros por período
-- - Filtros por cedente
-- - Filtros por moeda
-- ============================================================

INSERT INTO tb_receivable (id, assignor, type, face_value, term_in_months, status, due_date)VALUES (4, 'Empresa Delta Serviços Ltda', 'PRE_DATED_CHECK', 50000.00, 1, 'AVAILABLE', DATE '2026-02-01');

INSERT INTO tb_settlement (id,assignor,face_value,type,present_value_brl,settled_amount,payment_currency,base_rate_used,spread_used,status,exchange_rate_used,term_in_months,created_at,receivable_id)VALUES(1,'Empresa Horizonte S.A.',80000.00,'DUPLICATE_MERCANTILE',74287.95,74287.95,'BRL',0.0100,0.0150,'SETTLED',NULL,2,TIMESTAMP '2025-10-15 14:30:00',5);
INSERT INTO tb_settlement (id,assignor,face_value,type,present_value_brl,settled_amount,payment_currency,base_rate_used,spread_used,status,exchange_rate_used,term_in_months,created_at,receivable_id) VALUES(2, 'Empresa Nova Era Ltda',30000.00,'PRE_DATED_CHECK',28005.50,5334.38,'USD',0.0100,0.0250,'SETTLED',5.2500,2,TIMESTAMP '2025-09-20 10:15:00',6);


ALTER TABLE tb_receivable ALTER COLUMN id RESTART WITH 8;

ALTER TABLE tb_exchange_rate ALTER COLUMN id RESTART WITH 4;

ALTER TABLE tb_settlement ALTER COLUMN id RESTART WITH 4;