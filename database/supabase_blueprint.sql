-- ============================================================================
-- TRENDVIBES STREETWEAR SYSTEM - BLUEPRINT SUPABASE (POSTGRESQL)
-- Compatível 100% com o Supabase SQL Editor
-- ============================================================================

-- 1. TABELAS DO SISTEMA
-- ----------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS cargo (
    id SERIAL PRIMARY KEY,
    nome VARCHAR(50) NOT NULL UNIQUE,
    tipo VARCHAR(50) NOT NULL,
    percentualComissao NUMERIC(5,2) DEFAULT 0.00,
    descricao VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS usuario (
    id SERIAL PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    senha VARCHAR(255) NOT NULL,
    status VARCHAR(10) DEFAULT 'ATIVO',
    dataCriacao TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS colaborador (
    id SERIAL PRIMARY KEY,
    usuarioId INT NOT NULL REFERENCES usuario(id) ON DELETE CASCADE,
    cargoId INT NOT NULL REFERENCES cargo(id),
    dataAdmissao DATE NOT NULL DEFAULT CURRENT_DATE,
    salarioBase NUMERIC(10,2) NOT NULL,
    status VARCHAR(10) DEFAULT 'ATIVO',
    turnoStatus VARCHAR(20) DEFAULT 'AUSENTE',
    horaEntrada TIMESTAMPTZ,
    horaSaida TIMESTAMPTZ
);

CREATE TABLE IF NOT EXISTS metascolaboradores (
    id SERIAL PRIMARY KEY,
    colaboradorId INT NOT NULL REFERENCES colaborador(id) ON DELETE CASCADE,
    valorMeta NUMERIC(10,2) NOT NULL,
    mesReferencia VARCHAR(10) NOT NULL,
    bonusPercentual NUMERIC(5,2) DEFAULT 5.00,
    CONSTRAINT uq_colab_mes UNIQUE (colaboradorId, mesReferencia)
);

CREATE TABLE IF NOT EXISTS ocorrencia (
    id SERIAL PRIMARY KEY,
    colaboradorId INT NOT NULL REFERENCES colaborador(id) ON DELETE CASCADE,
    dataOcorrencia DATE NOT NULL DEFAULT CURRENT_DATE,
    tipo VARCHAR(100) NOT NULL,
    anexo VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS tarefaestoque (
    id SERIAL PRIMARY KEY,
    titulo VARCHAR(100) NOT NULL,
    descricao VARCHAR(255),
    prioridade VARCHAR(10) DEFAULT 'MEDIA' CHECK (prioridade IN ('ALTA', 'MEDIA', 'BAIXA')),
    status SMALLINT DEFAULT 0,
    dataTarefa DATE DEFAULT CURRENT_DATE,
    concluidoPorNome VARCHAR(100),
    dataConclusao TIMESTAMPTZ
);

CREATE TABLE IF NOT EXISTS venda (
    id SERIAL PRIMARY KEY,
    colaboradorId INT NOT NULL REFERENCES colaborador(id) ON DELETE CASCADE,
    sku VARCHAR(50) NOT NULL,
    valor NUMERIC(10,2) NOT NULL,
    comissao NUMERIC(10,2) NOT NULL,
    dataHora TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS auditoria_log (
    id SERIAL PRIMARY KEY,
    tabelaAfetada VARCHAR(50) NOT NULL,
    operacao VARCHAR(20) NOT NULL,
    registroId INT NOT NULL,
    detalhes VARCHAR(255) NOT NULL,
    dataEvento TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

-- ----------------------------------------------------------------------------
-- 2. TRIGGERS DE AUDITORIA (POSTGRESQL / PLPGSQL)
-- ----------------------------------------------------------------------------

CREATE OR REPLACE FUNCTION fn_trg_after_insert_venda()
RETURNS TRIGGER AS $$
BEGIN
    INSERT INTO auditoria_log (tabelaAfetada, operacao, registroId, detalhes)
    VALUES (
        'venda',
        'INSERT',
        NEW.id,
        'Venda SKU [' || NEW.sku || '] no valor de R$ ' || NEW.valor || ' | Comissão: R$ ' || NEW.comissao || ' | ColabID: ' || NEW.colaboradorId
    );
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_after_insert_venda ON venda;
CREATE TRIGGER trg_after_insert_venda
AFTER INSERT ON venda
FOR EACH ROW
EXECUTE FUNCTION fn_trg_after_insert_venda();

CREATE OR REPLACE FUNCTION fn_trg_after_insert_ocorrencia()
RETURNS TRIGGER AS $$
BEGIN
    INSERT INTO auditoria_log (tabelaAfetada, operacao, registroId, detalhes)
    VALUES (
        'ocorrencia',
        'INSERT',
        NEW.id,
        'Ocorrência [' || NEW.tipo || '] registrada para ColabID: ' || NEW.colaboradorId || ' na data ' || NEW.dataOcorrencia
    );
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_after_insert_ocorrencia ON ocorrencia;
CREATE TRIGGER trg_after_insert_ocorrencia
AFTER INSERT ON ocorrencia
FOR EACH ROW
EXECUTE FUNCTION fn_trg_after_insert_ocorrencia();

-- ----------------------------------------------------------------------------
-- 3. FUNCTIONS / PROCEDURES DE NEGÓCIO (HOLERITE E FECHAMENTO)
-- ----------------------------------------------------------------------------

CREATE OR REPLACE FUNCTION sp_CalcularHoleriteColaborador(
    p_colaboradorId INT,
    p_mes INT,
    p_ano INT
)
RETURNS TABLE (
    ColabID INT,
    Colaborador VARCHAR,
    Cargo VARCHAR,
    SalarioBase NUMERIC,
    TotalVendido NUMERIC,
    Comissao5PorCento NUMERIC,
    FaltasNaoJustificadas BIGINT,
    DescontoFaltas NUMERIC,
    DescontoINSS NUMERIC,
    TotalVencimentos NUMERIC,
    TotalDescontos NUMERIC,
    SalarioLiquido NUMERIC
) AS $$
DECLARE
    v_nome VARCHAR(100);
    v_cargo VARCHAR(50);
    v_salarioBase NUMERIC(10,2) := 0.00;
    v_totalVendas NUMERIC(10,2) := 0.00;
    v_comissao NUMERIC(10,2) := 0.00;
    v_diasFalta BIGINT := 0;
    v_descontoFaltas NUMERIC(10,2) := 0.00;
    v_inss NUMERIC(10,2) := 0.00;
    v_totalVencimentos NUMERIC(10,2) := 0.00;
    v_totalDescontos NUMERIC(10,2) := 0.00;
    v_salarioLiquido NUMERIC(10,2) := 0.00;
BEGIN
    SELECT u.nome, cg.nome, c.salarioBase
    INTO v_nome, v_cargo, v_salarioBase
    FROM colaborador c
    INNER JOIN usuario u ON c.usuarioId = u.id
    INNER JOIN cargo cg ON c.cargoId = cg.id
    WHERE c.id = p_colaboradorId;

    SELECT COALESCE(SUM(valor), 0), COALESCE(SUM(comissao), 0)
    INTO v_totalVendas, v_comissao
    FROM venda
    WHERE colaboradorId = p_colaboradorId
      AND EXTRACT(MONTH FROM dataHora) = p_mes
      AND EXTRACT(YEAR FROM dataHora) = p_ano;

    SELECT COUNT(*)
    INTO v_diasFalta
    FROM ocorrencia
    WHERE colaboradorId = p_colaboradorId
      AND tipo = 'Falta Não Justificada'
      AND EXTRACT(MONTH FROM dataOcorrencia) = p_mes
      AND EXTRACT(YEAR FROM dataOcorrencia) = p_ano;

    v_totalVencimentos := v_salarioBase + v_comissao;
    v_inss := ROUND(v_totalVencimentos * 0.0806, 2);
    v_descontoFaltas := ROUND((v_salarioBase / 30) * v_diasFalta, 2);
    v_totalDescontos := v_inss + v_descontoFaltas;
    v_salarioLiquido := v_totalVencimentos - v_totalDescontos;

    RETURN QUERY SELECT
        p_colaboradorId,
        v_nome,
        v_cargo,
        v_salarioBase,
        v_totalVendas,
        v_comissao,
        v_diasFalta,
        v_descontoFaltas,
        v_inss,
        v_totalVencimentos,
        v_totalDescontos,
        v_salarioLiquido;
END;
$$ LANGUAGE plpgsql;

-- ----------------------------------------------------------------------------
-- 4. SEED DATA (CARGOS, USUÁRIOS E DADOS DE TESTE INICIAIS)
-- ----------------------------------------------------------------------------

-- Inserção de Cargos
INSERT INTO cargo (nome, tipo, percentualComissao, descricao) VALUES 
('Vendedor', 'Operacional', 5.00, 'Atendimento ao cliente e lançamento de vendas'),
('Estoquista', 'Operacional', 0.00, 'Controle de entrada, saída e conferência de SKUs'),
('Gerente', 'Gestao', 2.00, 'Gestão de equipe, metas e relatórios executivos'),
('RH', 'Administrativo', 0.00, 'Gestão de colaboradores, holerites e ocorrências')
ON CONFLICT (nome) DO NOTHING;

-- Inserção de Usuários Padrão para Login
INSERT INTO usuario (nome, email, senha, status) VALUES 
('Lucas Vendedor', 'vendedor@trendvibes.com', '123456', 'ATIVO'),
('Marcos Estoquista', 'estoquista@trendvibes.com', '123456', 'ATIVO'),
('Camila Gerente', 'gerente@trendvibes.com', '123456', 'ATIVO'),
('Patricia RH', 'rh@trendvibes.com', '123456', 'ATIVO')
ON CONFLICT (email) DO NOTHING;

-- Vinculação Colaborador -> Cargo
INSERT INTO colaborador (usuarioId, cargoId, dataAdmissao, salarioBase, status, turnoStatus)
SELECT u.id, c.id, CURRENT_DATE, 2500.00, 'ATIVO', 'ATIVO'
FROM usuario u
JOIN cargo c ON c.nome = 'Vendedor'
WHERE u.email = 'vendedor@trendvibes.com'
  AND NOT EXISTS (SELECT 1 FROM colaborador WHERE usuarioId = u.id);

INSERT INTO colaborador (usuarioId, cargoId, dataAdmissao, salarioBase, status, turnoStatus)
SELECT u.id, c.id, CURRENT_DATE, 2400.00, 'ATIVO', 'ATIVO'
FROM usuario u
JOIN cargo c ON c.nome = 'Estoquista'
WHERE u.email = 'estoquista@trendvibes.com'
  AND NOT EXISTS (SELECT 1 FROM colaborador WHERE usuarioId = u.id);

INSERT INTO colaborador (usuarioId, cargoId, dataAdmissao, salarioBase, status, turnoStatus)
SELECT u.id, c.id, CURRENT_DATE, 5200.00, 'ATIVO', 'ATIVO'
FROM usuario u
JOIN cargo c ON c.nome = 'Gerente'
WHERE u.email = 'gerente@trendvibes.com'
  AND NOT EXISTS (SELECT 1 FROM colaborador WHERE usuarioId = u.id);

INSERT INTO colaborador (usuarioId, cargoId, dataAdmissao, salarioBase, status, turnoStatus)
SELECT u.id, c.id, CURRENT_DATE, 4100.00, 'ATIVO', 'ATIVO'
FROM usuario u
JOIN cargo c ON c.nome = 'RH'
WHERE u.email = 'rh@trendvibes.com'
  AND NOT EXISTS (SELECT 1 FROM colaborador WHERE usuarioId = u.id);

-- Tarefas Iniciais de Estoque
INSERT INTO tarefaestoque (titulo, descricao, prioridade, status) VALUES
('Conferir Drop Sangue-Frio', 'Conferência de chegada dos novos moletons e jaquetas.', 'ALTA', 0),
('Etiquetar Calças Cargo', 'Imprimir e aplicar etiquetas com código de barras nas calças.', 'MEDIA', 0),
('Inventário Semanal Acessórios', 'Contagem de bonés, meias e shoulder bags.', 'BAIXA', 0);
