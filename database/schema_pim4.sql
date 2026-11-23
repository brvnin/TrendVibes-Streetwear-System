-- ============================================================================
-- PROJETO INTEGRADO MULTIDISCIPLINAR (PIM IV) - TRENDVIBES STREETWEAR SYSTEM
-- Etapa 7: Script Completo do Banco de Dados (DDL, DML, Triggers e Procedures)
-- SGBD: MySQL 8.0 | Banco: trendvibesbd
-- ============================================================================

CREATE DATABASE IF NOT EXISTS `trendvibesbd` 
DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;

USE `trendvibesbd`;

-- ----------------------------------------------------------------------------
-- 1. ESTRUTURA FÍSICA DAS TABELAS (DDL)
-- ----------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS `cargo` (
  `id` int NOT NULL AUTO_INCREMENT,
  `nome` varchar(50) NOT NULL,
  `tipo` varchar(50) NOT NULL,
  `percentualComissao` decimal(5,2) DEFAULT '0.00',
  `descricao` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `usuario` (
  `id` int NOT NULL AUTO_INCREMENT,
  `nome` varchar(100) NOT NULL,
  `email` varchar(100) NOT NULL,
  `senha` varchar(255) NOT NULL,
  `status` varchar(10) DEFAULT 'ATIVO',
  `dataCriacao` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `colaborador` (
  `id` int NOT NULL AUTO_INCREMENT,
  `usuarioId` int NOT NULL,
  `cargoId` int NOT NULL,
  `dataAdmissao` date NOT NULL,
  `salarioBase` decimal(10,2) NOT NULL,
  `status` varchar(10) DEFAULT 'ATIVO',
  `turnoStatus` varchar(20) DEFAULT 'AUSENTE',
  `horaEntrada` datetime DEFAULT NULL,
  `horaSaida` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `usuarioId` (`usuarioId`),
  KEY `cargoId` (`cargoId`),
  CONSTRAINT `colaborador_ibfk_1` FOREIGN KEY (`usuarioId`) REFERENCES `usuario` (`id`),
  CONSTRAINT `colaborador_ibfk_2` FOREIGN KEY (`cargoId`) REFERENCES `cargo` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `metascolaboradores` (
  `id` int NOT NULL AUTO_INCREMENT,
  `colaboradorId` int NOT NULL,
  `valorMeta` decimal(10,2) NOT NULL,
  `mesReferencia` varchar(10) NOT NULL,
  `bonusPercentual` decimal(5,2) DEFAULT '5.00',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_colab_mes` (`colaboradorId`,`mesReferencia`),
  CONSTRAINT `metascolaboradores_ibfk_1` FOREIGN KEY (`colaboradorId`) REFERENCES `colaborador` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `ocorrencia` (
  `id` int NOT NULL AUTO_INCREMENT,
  `colaboradorId` int NOT NULL,
  `dataOcorrencia` date NOT NULL,
  `tipo` varchar(100) NOT NULL,
  `anexo` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_ocorrencia_colaborador` (`colaboradorId`),
  CONSTRAINT `fk_ocorrencia_colaborador` FOREIGN KEY (`colaboradorId`) REFERENCES `colaborador` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `tarefaestoque` (
  `id` int NOT NULL AUTO_INCREMENT,
  `titulo` varchar(100) NOT NULL,
  `descricao` varchar(255) DEFAULT NULL,
  `prioridade` enum('ALTA','MEDIA','BAIXA') DEFAULT 'MEDIA',
  `status` tinyint(1) DEFAULT '0',
  `dataTarefa` date DEFAULT (curdate()),
  `concluidoPorNome` varchar(100) DEFAULT NULL,
  `dataConclusao` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `venda` (
  `id` int NOT NULL AUTO_INCREMENT,
  `colaboradorId` int NOT NULL,
  `sku` varchar(50) NOT NULL,
  `valor` decimal(10,2) NOT NULL,
  `comissao` decimal(10,2) NOT NULL,
  `dataHora` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `colaboradorId` (`colaboradorId`),
  CONSTRAINT `venda_ibfk_1` FOREIGN KEY (`colaboradorId`) REFERENCES `colaborador` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Nova tabela de Auditoria para rastreabilidade das Triggers (PIM IV)
CREATE TABLE IF NOT EXISTS `auditoria_log` (
  `id` int NOT NULL AUTO_INCREMENT,
  `tabelaAfetada` varchar(50) NOT NULL,
  `operacao` varchar(20) NOT NULL,
  `registroId` int NOT NULL,
  `detalhes` varchar(255) NOT NULL,
  `dataEvento` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Garante a Foreign Key caso a tabela ocorrencia já existisse sem ela
SET @fk_exists := (
    SELECT COUNT(*) FROM information_schema.TABLE_CONSTRAINTS 
    WHERE CONSTRAINT_SCHEMA = 'trendvibesbd' 
      AND TABLE_NAME = 'ocorrencia' 
      AND CONSTRAINT_NAME = 'fk_ocorrencia_colaborador'
);
SET @sql_fk := IF(@fk_exists = 0, 
    'ALTER TABLE `ocorrencia` ADD CONSTRAINT `fk_ocorrencia_colaborador` FOREIGN KEY (`colaboradorId`) REFERENCES `colaborador` (`id`)', 
    'SELECT "FK já existe"');
PREPARE stmt FROM @sql_fk;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ----------------------------------------------------------------------------
-- 2. TRIGGERS (GATILHOS DE AUDITORIA E INTEGRIDADE AUTOMÁTICA)
-- ----------------------------------------------------------------------------

DROP TRIGGER IF EXISTS `trg_after_insert_venda`;
DROP TRIGGER IF EXISTS `trg_after_insert_ocorrencia`;

DELIMITER $$

-- Trigger 1: Audita automaticamente toda venda lançada via Web ou Mobile
CREATE TRIGGER `trg_after_insert_venda`
AFTER INSERT ON `venda`
FOR EACH ROW
BEGIN
    INSERT INTO `auditoria_log` (`tabelaAfetada`, `operacao`, `registroId`, `detalhes`)
    VALUES (
        'venda',
        'INSERT',
        NEW.id,
        CONCAT('Venda SKU [', NEW.sku, '] no valor de R$ ', NEW.valor, ' | Comissão: R$ ', NEW.comissao, ' | ColabID: ', NEW.colaboradorId)
    );
END$$

-- Trigger 2: Audita ocorrências de RH e registra alerta para fechamento de folha
CREATE TRIGGER `trg_after_insert_ocorrencia`
AFTER INSERT ON `ocorrencia`
FOR EACH ROW
BEGIN
    INSERT INTO `auditoria_log` (`tabelaAfetada`, `operacao`, `registroId`, `detalhes`)
    VALUES (
        'ocorrencia',
        'INSERT',
        NEW.id,
        CONCAT('Ocorrência [', NEW.tipo, '] registrada para ColabID: ', NEW.colaboradorId, ' na data ', NEW.dataOcorrencia)
    );
END$$

DELIMITER ;

-- ----------------------------------------------------------------------------
-- 3. STORED PROCEDURES (PROCEDIMENTOS ARMAZENADOS DE NEGÓCIO)
-- ----------------------------------------------------------------------------

DROP PROCEDURE IF EXISTS `sp_CalcularHoleriteColaborador`;
DROP PROCEDURE IF EXISTS `sp_RelatorioFechamentoComercial`;

DELIMITER $$

-- Procedure 1: Calcula vencimentos, comissões, INSS e descontos por faltas
CREATE PROCEDURE `sp_CalcularHoleriteColaborador`(
    IN p_colaboradorId INT,
    IN p_mes INT,
    IN p_ano INT
)
BEGIN
    DECLARE v_nome VARCHAR(100);
    DECLARE v_cargo VARCHAR(50);
    DECLARE v_salarioBase DECIMAL(10,2) DEFAULT 0.00;
    DECLARE v_totalVendas DECIMAL(10,2) DEFAULT 0.00;
    DECLARE v_comissao DECIMAL(10,2) DEFAULT 0.00;
    DECLARE v_diasFalta INT DEFAULT 0;
    DECLARE v_descontoFaltas DECIMAL(10,2) DEFAULT 0.00;
    DECLARE v_inss DECIMAL(10,2) DEFAULT 0.00;
    DECLARE v_totalVencimentos DECIMAL(10,2) DEFAULT 0.00;
    DECLARE v_totalDescontos DECIMAL(10,2) DEFAULT 0.00;
    DECLARE v_salarioLiquido DECIMAL(10,2) DEFAULT 0.00;

    -- Busca dados cadastrais e salário base
    SELECT u.nome, cg.nome, c.salarioBase
    INTO v_nome, v_cargo, v_salarioBase
    FROM colaborador c
    INNER JOIN usuario u ON c.usuarioId = u.id
    INNER JOIN cargo cg ON c.cargoId = cg.id
    WHERE c.id = p_colaboradorId;

    -- Soma vendas e comissões no período
    SELECT COALESCE(SUM(valor), 0), COALESCE(SUM(comissao), 0)
    INTO v_totalVendas, v_comissao
    FROM venda
    WHERE colaboradorId = p_colaboradorId
      AND MONTH(dataHora) = p_mes
      AND YEAR(dataHora) = p_ano;

    -- Conta faltas não justificadas no período
    SELECT COUNT(*)
    INTO v_diasFalta
    FROM ocorrencia
    WHERE colaboradorId = p_colaboradorId
      AND tipo = 'Falta Não Justificada'
      AND MONTH(dataOcorrencia) = p_mes
      AND YEAR(dataOcorrencia) = p_ano;

    -- Regras de cálculo trabalhista
    SET v_totalVencimentos = v_salarioBase + v_comissao;
    SET v_inss = ROUND(v_totalVencimentos * 0.0806, 2);
    SET v_descontoFaltas = ROUND((v_salarioBase / 30) * v_diasFalta, 2);
    SET v_totalDescontos = v_inss + v_descontoFaltas;
    SET v_salarioLiquido = v_totalVencimentos - v_totalDescontos;

    SELECT 
        p_colaboradorId AS ColabID,
        v_nome AS Colaborador,
        v_cargo AS Cargo,
        v_salarioBase AS SalarioBase,
        v_totalVendas AS TotalVendido,
        v_comissao AS Comissao5PorCento,
        v_diasFalta AS FaltasNaoJustificadas,
        v_descontoFaltas AS DescontoFaltas,
        v_inss AS DescontoINSS,
        v_totalVencimentos AS TotalVencimentos,
        v_totalDescontos AS TotalDescontos,
        v_salarioLiquido AS SalarioLiquido;
END$$

-- Procedure 2: Consolida performance da equipe vs. metas definidas
CREATE PROCEDURE `sp_RelatorioFechamentoComercial`(
    IN p_mesReferencia VARCHAR(10)
)
BEGIN
    SELECT 
        c.id AS ColabID,
        u.nome AS Vendedor,
        cg.nome AS Cargo,
        COALESCE(m.valorMeta, 25000.00) AS MetaMensal,
        COALESCE(SUM(v.valor), 0.00) AS FaturamentoRealizado,
        COALESCE(SUM(v.comissao), 0.00) AS ComissaoAcumulada,
        ROUND((COALESCE(SUM(v.valor), 0.00) / COALESCE(m.valorMeta, 25000.00)) * 100, 2) AS PercentualAtingido
    FROM colaborador c
    INNER JOIN usuario u ON c.usuarioId = u.id
    INNER JOIN cargo cg ON c.cargoId = cg.id
    LEFT JOIN metascolaboradores m ON m.colaboradorId = c.id AND m.mesReferencia = p_mesReferencia
    LEFT JOIN venda v ON v.colaboradorId = c.id
    WHERE u.status = 'ATIVO' AND cg.nome IN ('Vendedor', 'Gerente')
    GROUP BY c.id, u.nome, cg.nome, m.valorMeta
    ORDER BY FaturamentoRealizado DESC;
END$$

DELIMITER ;