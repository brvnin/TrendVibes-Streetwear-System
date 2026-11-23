using Npgsql;
using Microsoft.AspNetCore.Mvc;

var builder = WebApplication.CreateBuilder(args);

// Configurando injeção do NpgsqlConnection usando a ConnectionString configurada
builder.Services.AddTransient<NpgsqlConnection>(sp => 
{
    var config = sp.GetRequiredService<IConfiguration>();
    string connStr = config.GetConnectionString("TrendVibesDB")
                     ?? "Host=db.thkbjueczhohfjbchnoq.supabase.co;Port=5432;Database=postgres;Username=postgres;Password=SUA_SENHA_AQUI;SSL Mode=Require;Trust Server Certificate=true";
    return new NpgsqlConnection(connStr);
});

// 1. Configurando o CORS
builder.Services.AddCors(options =>
{
    options.AddPolicy("PermitirFrontend", policy =>
    {
        policy.AllowAnyOrigin()
              .AllowAnyMethod()
              .AllowAnyHeader();
    });
});

var app = builder.Build();
app.UseCors("PermitirFrontend");

// ==========================================
// ROTA 1: PUXAR USUÁRIOS
// ==========================================
app.MapGet("/api/usuarios", async (IConfiguration config) =>
{
    var listaUsuarios = new List<object>();
    string stringConexao = config.GetConnectionString("TrendVibesDB")!;

    using (var conexao = new NpgsqlConnection(stringConexao))
    {
        await conexao.OpenAsync();
        using (var comando = new NpgsqlCommand("SELECT id, nome, email, status FROM usuario", conexao))
        using (var reader = await comando.ExecuteReaderAsync())
        {
            while (await reader.ReadAsync())
            {
                listaUsuarios.Add(new
                {
                    Id = reader.GetInt32(reader.GetOrdinal("id")),
                    Nome = reader.GetString(reader.GetOrdinal("nome")),
                    Email = reader.GetString(reader.GetOrdinal("email")),
                    Status = reader.GetString(reader.GetOrdinal("status"))
                });
            }
        }
    }
    return Results.Ok(listaUsuarios);
});

// ==========================================
// ROTA 2: FAZER LOGIN 
// ==========================================
app.MapPost("/api/login", async ([FromBody] LoginRequest request, IConfiguration config) =>
{
    try
    {
        string stringConexao = config.GetConnectionString("TrendVibesDB")!;

        using (var conexao = new NpgsqlConnection(stringConexao))
        {
            await conexao.OpenAsync();

            string query = @"
                SELECT u.id AS usuarioId, col.id AS colaboradorId, u.nome, u.email, c.nome AS cargo, col.turnoStatus
                FROM usuario u
                INNER JOIN colaborador col ON u.id = col.usuarioId
                INNER JOIN cargo c ON col.cargoId = c.id
                WHERE u.email = @email AND u.senha = @senha AND u.status = 'ATIVO'";
                
            using (var comando = new NpgsqlCommand(query, conexao))
            {
                comando.Parameters.AddWithValue("@email", request.Email);
                comando.Parameters.AddWithValue("@senha", request.Senha);

                using (var reader = await comando.ExecuteReaderAsync())
                {
                    if (await reader.ReadAsync())
                    {
                        string nomeCargo = reader.GetString(reader.GetOrdinal("cargo"));

                        if (nomeCargo != request.Cargo)
                        {
                            return Results.BadRequest(new { sucesso = false, mensagem = "O cargo selecionado não corresponde a este usuário." });
                        }

                        string urlDestino = nomeCargo switch
                        {
                            "Vendedor" => "vendedor.html",
                            "Estoquista" => "estoquista.html",
                            "Gerente" => "gerente.html",
                            "RH" => "rh.html",
                            _ => "login.html" 
                        };
                        
                        int idxTurno = reader.GetOrdinal("turnoStatus");
                        string statusBanco = reader.IsDBNull(idxTurno) ? "AUSENTE" : reader.GetString(idxTurno);

                        return Results.Ok(new { 
                            sucesso = true, 
                            colaboradorId = reader.GetInt32(reader.GetOrdinal("colaboradorId")),
                            nome = reader.GetString(reader.GetOrdinal("nome")), 
                            cargo = nomeCargo,
                            url = urlDestino,
                            email = reader.GetString(reader.GetOrdinal("email")),
                            turnoStatus = statusBanco
                        });
                    }
                    else
                    {
                        return Results.BadRequest(new { sucesso = false, mensagem = "E-mail ou senha incorretos." });
                    }
                }
            }
        }
    }
    catch (Exception ex)
    {
        Console.WriteLine($"[ERRO LOGIN]: {ex.Message}");
        return Results.BadRequest(new { sucesso = false, mensagem = $"Erro no banco Supabase: {ex.Message}" });
    }
});

// ==========================================
// ROTA 3: REGISTRAR VENDA
// ==========================================
app.MapPost("/api/vendas", async ([FromBody] VendaRequest request, IConfiguration config) =>
{
    string stringConexao = config.GetConnectionString("TrendVibesDB")!;

    using (var conexao = new NpgsqlConnection(stringConexao))
    {
        await conexao.OpenAsync();

        // Aplicando a Regra de Negócio: Comissão de 5% sobre o valor da venda
        decimal comissaoCalculada = request.Valor * 0.05m;

        string query = @"
            INSERT INTO venda (colaboradorId, sku, valor, comissao) 
            VALUES (@colaboradorId, @sku, @valor, @comissao)";

        using (var comando = new NpgsqlCommand(query, conexao))
        {
            comando.Parameters.AddWithValue("@colaboradorId", request.ColaboradorId);
            comando.Parameters.AddWithValue("@sku", request.Sku);
            comando.Parameters.AddWithValue("@valor", request.Valor);
            comando.Parameters.AddWithValue("@comissao", comissaoCalculada);

            await comando.ExecuteNonQueryAsync();

            return Results.Ok(new { 
                sucesso = true, 
                mensagem = "Venda registrada no banco de dados!", 
                comissao = comissaoCalculada 
            });
        }
    }
});

// ==========================================
// ROTA 4: PUXAR HISTÓRICO DE VENDAS DO COLABORADOR
// ==========================================
app.MapGet("/api/vendas/{colaboradorId}", async (int colaboradorId, IConfiguration config) =>
{
    var historico = new List<object>();
    string stringConexao = config.GetConnectionString("TrendVibesDB")!;

    using (var conexao = new NpgsqlConnection(stringConexao))
    {
        await conexao.OpenAsync();
        
        string query = @"
            SELECT sku, valor, comissao, TO_CHAR(dataHora, 'HH24:MI') as horaStr
            FROM venda 
            WHERE colaboradorId = @id
            ORDER BY dataHora DESC 
            LIMIT 10"; 

        using (var comando = new NpgsqlCommand(query, conexao))
        {
            comando.Parameters.AddWithValue("@id", colaboradorId);

            using (var reader = await comando.ExecuteReaderAsync())
            {
                while (await reader.ReadAsync())
                {
                    historico.Add(new
                    {
                        sku = reader.GetString(reader.GetOrdinal("sku")),
                        valor = reader.GetDecimal(reader.GetOrdinal("valor")),
                        comissao = reader.GetDecimal(reader.GetOrdinal("comissao")),
                        hora = reader.GetString(reader.GetOrdinal("horaStr"))
                    });
                }
            }
        }
    }
    return Results.Ok(historico);
});

// ==========================================
// ROTA 5: RESUMO DE METAS 
// ==========================================
app.MapGet("/api/vendas/resumo/{id}", async ([FromServices] NpgsqlConnection conexao, int id) => {
    try {
        await conexao.OpenAsync();
        
        string queryMeta = "SELECT valorMeta FROM metascolaboradores WHERE colaboradorId = @id ORDER BY id DESC LIMIT 1";
        
        decimal metaDefinida = 25000.00m; 
        
        using (var cmd = new NpgsqlCommand(queryMeta, conexao)) {
            cmd.Parameters.AddWithValue("@id", id);
            var resultado = await cmd.ExecuteScalarAsync();
            
            if (resultado != null && resultado != DBNull.Value) {
                metaDefinida = Convert.ToDecimal(resultado);
            }
        }

        string queryVendas = "SELECT COALESCE(SUM(valor), 0) FROM venda WHERE colaboradorId = @id";
        decimal totalVendido = 0;
        using (var cmd = new NpgsqlCommand(queryVendas, conexao)) {
            cmd.Parameters.AddWithValue("@id", id);
            var resVendas = await cmd.ExecuteScalarAsync();
            if (resVendas != null && resVendas != DBNull.Value) {
                totalVendido = Convert.ToDecimal(resVendas);
            }
        }

        return Results.Ok(new { 
            sucesso = true, 
            totalVendas = totalVendido, 
            metaMensal = metaDefinida,
            totalComissao = totalVendido * 0.05m 
        });
    } catch (Exception ex) {
        return Results.BadRequest(new { sucesso = false, mensagem = ex.Message });
    }
});

// ==========================================
// ROTA 6: RESUMO DE PROGRESSO DA EQUIPE DE VENDAS
// ==========================================
app.MapGet("/api/vendas/equipe/resumo", async ([FromServices] NpgsqlConnection conexao) => {
    var lista = new List<object>();
    await conexao.OpenAsync();
    
    string query = @"
        SELECT 
            c.id AS colaboradorId, 
            u.nome, 
            COALESCE((SELECT valorMeta FROM metascolaboradores m WHERE m.colaboradorId = c.id ORDER BY id DESC LIMIT 1), 25000.00) AS metaMensal,
            COALESCE((SELECT bonusPercentual FROM metascolaboradores m WHERE m.colaboradorId = c.id ORDER BY id DESC LIMIT 1), 5.00) AS bonusPercentual,
            COALESCE((SELECT SUM(valor) FROM venda v WHERE v.colaboradorId = c.id), 0) AS totalVendas
        FROM colaborador c
        INNER JOIN usuario u ON c.usuarioId = u.id
        INNER JOIN cargo cg ON c.cargoId = cg.id
        WHERE cg.nome = 'Vendedor' AND u.status = 'ATIVO'";
    
    using (var comando = new NpgsqlCommand(query, conexao))
    using (var reader = await comando.ExecuteReaderAsync())
    {
        while (await reader.ReadAsync())
        {
            lista.Add(new {
                colaboradorId = reader.GetInt32(reader.GetOrdinal("colaboradorId")),
                nome = reader.GetString(reader.GetOrdinal("nome")),
                metaMensal = reader.GetDecimal(reader.GetOrdinal("metaMensal")),
                bonusPercentual = reader.GetDecimal(reader.GetOrdinal("bonusPercentual")),
                totalVendas = reader.GetDecimal(reader.GetOrdinal("totalVendas"))
            });
        }
    }
    return Results.Ok(lista);
});

// 1. ROTA GET: Busca as tarefas para exibir na tela 
app.MapGet("/api/tarefas", async ([FromServices] NpgsqlConnection conexao) => {
    await conexao.OpenAsync();
    string query = "SELECT id, titulo, descricao, prioridade, status, concluidoPorNome, TO_CHAR(dataConclusao, 'HH24:MI') as horaConclusao FROM tarefaestoque WHERE dataTarefa = CURRENT_DATE";
    
    using (var comando = new NpgsqlCommand(query, conexao))
    using (var reader = await comando.ExecuteReaderAsync())
    {
        var lista = new List<object>();
        while (await reader.ReadAsync())
        {
            int idxDescricao = reader.GetOrdinal("descricao");
            int idxNome = reader.GetOrdinal("concluidoPorNome");
            int idxHora = reader.GetOrdinal("horaConclusao");
            int idxStatus = reader.GetOrdinal("status");

            lista.Add(new {
                id = reader.GetInt32(reader.GetOrdinal("id")),
                titulo = reader.GetString(reader.GetOrdinal("titulo")),
                descricao = reader.IsDBNull(idxDescricao) ? "" : reader.GetString(idxDescricao),
                prioridade = reader.GetString(reader.GetOrdinal("prioridade")),
                status = !reader.IsDBNull(idxStatus) && Convert.ToInt32(reader[idxStatus]) == 1,
                concluidoPorNome = reader.IsDBNull(idxNome) ? "" : reader.GetString(idxNome),
                horaConclusao = reader.IsDBNull(idxHora) ? "" : reader.GetString(idxHora)
            });
        }
        return Results.Ok(lista);
    }
});

// 2. ROTA PUT: Atualiza o status no banco salvando QUEM fez
app.MapPut("/api/tarefas/{id}", async ([FromServices] NpgsqlConnection conexao, int id, TarefaStatusRequest req) => {
    await conexao.OpenAsync();
    
    string query = req.Status 
        ? "UPDATE tarefaestoque SET status = 1, concluidoPorNome = @nome, dataConclusao = NOW() WHERE id = @id"
        : "UPDATE tarefaestoque SET status = 0, concluidoPorNome = NULL, dataConclusao = NULL WHERE id = @id";
    
    using (var comando = new NpgsqlCommand(query, conexao))
    {
        comando.Parameters.AddWithValue("@id", id);
        comando.Parameters.AddWithValue("@nome", (object?)req.NomeConcluinte ?? DBNull.Value);
        await comando.ExecuteNonQueryAsync();
    }
    return Results.Ok();
});

app.MapPost("/api/metas", async ([FromServices] NpgsqlConnection conexao, MetasRequest request) => {
    await conexao.OpenAsync();
    string query = @"INSERT INTO metascolaboradores (colaboradorId, valorMeta, bonusPercentual, mesReferencia) 
                     VALUES (@cid, @meta, @bonus, @mes)
                     ON CONFLICT (colaboradorId, mesReferencia) 
                     DO UPDATE SET valorMeta = EXCLUDED.valorMeta, bonusPercentual = EXCLUDED.bonusPercentual";
    
    using (var comando = new NpgsqlCommand(query, conexao))
    {
        comando.Parameters.AddWithValue("@cid", request.ColaboradorId);
        comando.Parameters.AddWithValue("@meta", request.ValorMeta);
        comando.Parameters.AddWithValue("@bonus", request.bonusPercentual);
        comando.Parameters.AddWithValue("@mes", request.MesReferencia);
        await comando.ExecuteNonQueryAsync();
    }
    return Results.Ok(new { sucesso = true });
});

// ==========================================
// ROTA 7: ALTERAR STATUS DO TURNO (Todos os usuários)
// ==========================================
app.MapPut("/api/turno", async ([FromServices] NpgsqlConnection conexao, TurnoRequest req) => {
    await conexao.OpenAsync();
    
    string query = req.Status == "ATIVO" 
        ? "UPDATE colaborador SET turnoStatus = 'ATIVO', horaEntrada = NOW() WHERE id = @id"
        : "UPDATE colaborador SET turnoStatus = 'AUSENTE', horaSaida = NOW() WHERE id = @id";
    
    using (var comando = new NpgsqlCommand(query, conexao))
    {
        comando.Parameters.AddWithValue("@id", req.ColaboradorId);
        await comando.ExecuteNonQueryAsync();
    }
    return Results.Ok(new { sucesso = true });
});

// ==========================================
// ROTA 8: STATUS INTERNO DA EQUIPE (Painel do Gerente)
// ==========================================
app.MapGet("/api/equipe/status", async ([FromServices] NpgsqlConnection conexao) => {
    await conexao.OpenAsync();
    string query = @"
        SELECT c.id, u.nome, cg.nome as cargo, c.turnoStatus, 
               TO_CHAR(c.horaEntrada, 'HH24:MI') as horaEntrada,
               TO_CHAR(c.horaSaida, 'HH24:MI') as horaSaida
        FROM colaborador c
        INNER JOIN usuario u ON c.usuarioId = u.id
        INNER JOIN cargo cg ON c.cargoId = cg.id
        WHERE u.status = 'ATIVO'";
    
    var lista = new List<object>();
    using (var comando = new NpgsqlCommand(query, conexao))
    using (var reader = await comando.ExecuteReaderAsync())
    {
        while (await reader.ReadAsync())
        {
            int idxTurno = reader.GetOrdinal("turnoStatus");
            int idxEntrada = reader.GetOrdinal("horaEntrada");
            int idxSaida = reader.GetOrdinal("horaSaida");

            lista.Add(new {
                id = reader.GetInt32(reader.GetOrdinal("id")),
                nome = reader.GetString(reader.GetOrdinal("nome")),
                cargo = reader.GetString(reader.GetOrdinal("cargo")),
                turnoStatus = reader.IsDBNull(idxTurno) ? "AUSENTE" : reader.GetString(idxTurno),
                horaEntrada = reader.IsDBNull(idxEntrada) ? "--:--" : reader.GetString(idxEntrada),
                horaSaida = reader.IsDBNull(idxSaida) ? "--:--" : reader.GetString(idxSaida)
            });
        }
    }
    return Results.Ok(lista);
});

// ==========================================
// ROTA 9: CRIAR NOVA TAREFA (Painel do Gerente)
// ==========================================
app.MapPost("/api/tarefas", async ([FromServices] NpgsqlConnection conexao, TarefaRequest req) => {
    await conexao.OpenAsync();
    
    string query = @"INSERT INTO tarefaestoque (titulo, descricao, prioridade, dataTarefa, status) 
                     VALUES (@titulo, @descricao, @prioridade, @dataTarefa::date, 0)";
                     
    using (var comando = new NpgsqlCommand(query, conexao))
    {
        comando.Parameters.AddWithValue("@titulo", req.Titulo);
        comando.Parameters.AddWithValue("@descricao", req.Descricao);
        comando.Parameters.AddWithValue("@prioridade", req.Prioridade);
        comando.Parameters.AddWithValue("@dataTarefa", req.DataTarefa.ToString("yyyy-MM-dd"));
        
        await comando.ExecuteNonQueryAsync();
    }
    return Results.Ok(new { sucesso = true });
});

// ==========================================
// ROTA 10: LISTAR COLABORADORES PARA O RH (Dropdown)
// ==========================================
app.MapGet("/api/colaboradores/simples", async ([FromServices] NpgsqlConnection conexao) => {
    await conexao.OpenAsync();
    string query = @"
        SELECT c.id, u.nome, cg.nome as cargo 
        FROM colaborador c
        INNER JOIN usuario u ON c.usuarioId = u.id
        INNER JOIN cargo cg ON c.cargoId = cg.id
        WHERE u.status = 'ATIVO'";
    
    var lista = new List<object>();
    using (var comando = new NpgsqlCommand(query, conexao))
    using (var reader = await comando.ExecuteReaderAsync())
    {
        while (await reader.ReadAsync())
        {
            lista.Add(new {
                id = reader.GetInt32(reader.GetOrdinal("id")),
                nome = reader.GetString(reader.GetOrdinal("nome")),
                cargo = reader.GetString(reader.GetOrdinal("cargo"))
            });
        }
    }
    return Results.Ok(lista);
});

// ==========================================
// ROTA 11: GERAR DADOS DO HOLERITE 
// ==========================================
app.MapGet("/api/holerite/{id}", async ([FromServices] NpgsqlConnection conexao, int id) => {
    await conexao.OpenAsync();
    
    string nome = "";
    string cargo = "";
    decimal salarioBase = 1800.00m; 
    string dataAdmissao = "15/02/2024"; 
    string cbo = "5211-10"; 
    
    // 1. Puxa os dados do colaborador
    string queryFunc = @"
        SELECT u.nome, cg.nome as cargo 
        FROM colaborador c
        INNER JOIN usuario u ON c.usuarioId = u.id
        INNER JOIN cargo cg ON c.cargoId = cg.id
        WHERE c.id = @id";
        
    using (var cmd = new NpgsqlCommand(queryFunc, conexao)) {
        cmd.Parameters.AddWithValue("@id", id);
        using (var reader = await cmd.ExecuteReaderAsync()) {
            if (await reader.ReadAsync()) {
                nome = reader.GetString(reader.GetOrdinal("nome"));
                cargo = reader.GetString(reader.GetOrdinal("cargo"));
                
                if(cargo == "Gerente") { salarioBase = 3500.00m; cbo = "1414-10"; }
                else if(cargo == "Estoquista") { salarioBase = 1600.00m; cbo = "4141-05"; }
                else if(cargo == "RH") { salarioBase = 2500.00m; cbo = "4110-05"; }
            }
        }
    }

    // 2. Puxa as Comissões
    decimal totalVendas = 0;
    decimal comissao = 0;
    string queryVendas = "SELECT COALESCE(SUM(valor), 0), COALESCE(SUM(comissao), 0) FROM venda WHERE colaboradorId = @id";
    using (var cmd = new NpgsqlCommand(queryVendas, conexao)) {
        cmd.Parameters.AddWithValue("@id", id);
        using (var reader = await cmd.ExecuteReaderAsync()) {
            if (await reader.ReadAsync() && !reader.IsDBNull(0)) {
                totalVendas = reader.GetDecimal(0);
                comissao = reader.GetDecimal(1);
            }
        }
    }

    // 3. CONTA AS FALTAS NÃO JUSTIFICADAS DO MÊS ATUAL
    int diasFalta = 0;
    string queryFaltas = @"SELECT COUNT(*) FROM ocorrencia 
                           WHERE colaboradorId = @id AND tipo = 'Falta Não Justificada' 
                           AND EXTRACT(MONTH FROM dataOcorrencia) = EXTRACT(MONTH FROM CURRENT_DATE) 
                           AND EXTRACT(YEAR FROM dataOcorrencia) = EXTRACT(YEAR FROM CURRENT_DATE)";
    using (var cmd = new NpgsqlCommand(queryFaltas, conexao)) {
        cmd.Parameters.AddWithValue("@id", id);
        var resFaltas = await cmd.ExecuteScalarAsync();
        if (resFaltas != null && resFaltas != DBNull.Value) {
            diasFalta = Convert.ToInt32(resFaltas);
        }
    }

    // 4. Cálculos do Holerite (Matemática da Folha)
    decimal totalVencimentos = salarioBase + comissao;
    decimal inss = Math.Round(totalVencimentos * 0.0806m, 2); 
    
    decimal faltasValor = Math.Round((salarioBase / 30m) * diasFalta, 2); 
    
    decimal totalDescontos = inss + faltasValor;
    decimal liquido = totalVencimentos - totalDescontos;

    return Results.Ok(new {
        sucesso = true,
        nome = nome,
        cargo = cargo,
        admissao = dataAdmissao,
        cbo = cbo,
        salarioBase = salarioBase,
        totalVendas = totalVendas,
        comissao = comissao,
        inss = inss,
        diasFalta = diasFalta,
        faltas = faltasValor,
        totalVencimentos = totalVencimentos,
        totalDescontos = totalDescontos,
        liquido = liquido
    });
});

// ==========================================
// ROTA 12: REGISTRAR OCORRÊNCIA / FALTA (RH)
// ==========================================
app.MapPost("/api/ocorrencias", async ([FromServices] NpgsqlConnection conexao, OcorrenciaRequest req) => {
    await conexao.OpenAsync();
    string query = @"INSERT INTO ocorrencia (colaboradorId, dataOcorrencia, tipo, anexo) 
                     VALUES (@cid, @data::date, @tipo, @anexo)";
    using (var cmd = new NpgsqlCommand(query, conexao)) {
        cmd.Parameters.AddWithValue("@cid", req.ColaboradorId);
        cmd.Parameters.AddWithValue("@data", req.DataOcorrencia);
        cmd.Parameters.AddWithValue("@tipo", req.Tipo);
        cmd.Parameters.AddWithValue("@anexo", (object?)req.Anexo ?? DBNull.Value);
        await cmd.ExecuteNonQueryAsync();
    }
    return Results.Ok(new { sucesso = true });
});

// ==========================================
// ROTA 13: LISTAR TODOS OS COLABORADORES
// ==========================================
app.MapGet("/api/pessoal", async ([FromServices] NpgsqlConnection conexao) => {
    await conexao.OpenAsync();
    string query = @"
        SELECT c.id as colabId, u.id as userId, u.nome, u.email, cg.nome as cargo, u.status
        FROM colaborador c
        INNER JOIN usuario u ON c.usuarioId = u.id
        INNER JOIN cargo cg ON c.cargoId = cg.id
        ORDER BY u.status ASC, u.nome ASC";

    var lista = new List<object>();
    using (var cmd = new NpgsqlCommand(query, conexao))
    using (var reader = await cmd.ExecuteReaderAsync())
    {
        while (await reader.ReadAsync())
        {
            lista.Add(new {
                colabId = reader.GetInt32(0),
                userId = reader.GetInt32(1),
                nome = reader.IsDBNull(2) ? "SEM NOME" : reader.GetString(2),
                email = reader.IsDBNull(3) ? "SEM E-MAIL" : reader.GetString(3),
                cargo = reader.IsDBNull(4) ? "SEM CARGO" : reader.GetString(4),
                status = reader.IsDBNull(5) ? "INATIVO" : reader.GetString(5)
            });
        }
    }
    return Results.Ok(lista);
});

// ==========================================
// ROTA 14: SALVAR NOVO OU EDITAR FUNCIONÁRIO 
// ==========================================
app.MapPost("/api/pessoal", async ([FromServices] NpgsqlConnection conexao, FuncionarioRequest req) => {
    await conexao.OpenAsync();
    using var transaction = await conexao.BeginTransactionAsync();
    try {
        int cargoId = 1;
        string nomeBusca = req.Cargo == "RH" ? "Recursos Humanos" : req.Cargo;
        
        using (var cmdCargo = new NpgsqlCommand("SELECT id FROM cargo WHERE nome = @nome LIMIT 1", conexao, transaction)) {
            cmdCargo.Parameters.AddWithValue("@nome", nomeBusca);
            var res = await cmdCargo.ExecuteScalarAsync();
            if(res != null) {
                cargoId = Convert.ToInt32(res);
            } else {
                using (var cmdFallback = new NpgsqlCommand("SELECT id FROM cargo LIMIT 1", conexao, transaction)) {
                    var fallbackRes = await cmdFallback.ExecuteScalarAsync();
                    if (fallbackRes != null) cargoId = Convert.ToInt32(fallbackRes);
                }
            }
        }

        if (req.UserId == 0) {
            string qUser = "INSERT INTO usuario (nome, email, senha, status) VALUES (@n, @e, @s, 'ATIVO') RETURNING id";
            int newUserId = 0;
            using (var cmdU = new NpgsqlCommand(qUser, conexao, transaction)) {
                cmdU.Parameters.AddWithValue("@n", req.Nome);
                cmdU.Parameters.AddWithValue("@e", req.Email);
                cmdU.Parameters.AddWithValue("@s", req.Senha);
                var insertedId = await cmdU.ExecuteScalarAsync();
                newUserId = Convert.ToInt32(insertedId);
            }
            
            string qColab = "INSERT INTO colaborador (usuarioId, cargoId, dataAdmissao, salarioBase) VALUES (@u, @c, @da::date, @sb)";
            using (var cmdC = new NpgsqlCommand(qColab, conexao, transaction)) {
                cmdC.Parameters.AddWithValue("@u", newUserId);
                cmdC.Parameters.AddWithValue("@c", cargoId);
                cmdC.Parameters.AddWithValue("@da", string.IsNullOrEmpty(req.DataAdmissao) ? DateTime.Now.ToString("yyyy-MM-dd") : req.DataAdmissao);
                cmdC.Parameters.AddWithValue("@sb", req.SalarioBase);
                await cmdC.ExecuteNonQueryAsync();
            }
        } else {
            string qUser = "UPDATE usuario SET nome = @n, email = @e" +
                           (string.IsNullOrEmpty(req.Senha) ? "" : ", senha = @s") + 
                           " WHERE id = @uid";
            using (var cmdU = new NpgsqlCommand(qUser, conexao, transaction)) {
                cmdU.Parameters.AddWithValue("@n", req.Nome);
                cmdU.Parameters.AddWithValue("@e", req.Email);
                cmdU.Parameters.AddWithValue("@uid", req.UserId);
                if(!string.IsNullOrEmpty(req.Senha)) cmdU.Parameters.AddWithValue("@s", req.Senha);
                await cmdU.ExecuteNonQueryAsync();
            }
            string qColab = "UPDATE colaborador SET cargoId = @c, salarioBase = @sb WHERE id = @cid";
            using (var cmdC = new NpgsqlCommand(qColab, conexao, transaction)) {
                cmdC.Parameters.AddWithValue("@c", cargoId);
                cmdC.Parameters.AddWithValue("@sb", req.SalarioBase); 
                cmdC.Parameters.AddWithValue("@cid", req.ColabId);
                await cmdC.ExecuteNonQueryAsync();
            }
        }
        await transaction.CommitAsync();
        return Results.Ok(new { sucesso = true });
    } catch (Exception ex) {
        await transaction.RollbackAsync();
        Console.WriteLine("ERRO NO CRUD DE USUÁRIO: " + ex.Message);
        return Results.BadRequest(new { sucesso = false, mensagem = ex.Message });
    }
});

// ==========================================
// ROTA 15: ALTERAR STATUS (INATIVAR / REATIVAR)
// ==========================================
app.MapPut("/api/pessoal/status", async ([FromServices] NpgsqlConnection conexao, StatusRequest req) => {
    await conexao.OpenAsync();
    string query = "UPDATE usuario SET status = @status WHERE id = @uid";
    using (var cmd = new NpgsqlCommand(query, conexao)) {
        cmd.Parameters.AddWithValue("@status", req.NovoStatus);
        cmd.Parameters.AddWithValue("@uid", req.UserId);
        await cmd.ExecuteNonQueryAsync();
    }
    return Results.Ok(new { sucesso = true });
});

// ==========================================
// ROTA 16: CÁLCULO DE TICKET MÉDIO 
// ==========================================
app.MapGet("/api/ticket/{id}", async ([FromServices] NpgsqlConnection conexao, int id) => {
    await conexao.OpenAsync();
    
    string cargo = "";
    using (var cmdCargo = new NpgsqlCommand("SELECT cg.nome FROM colaborador c INNER JOIN cargo cg ON c.cargoId = cg.id WHERE c.id = @id", conexao)) {
        cmdCargo.Parameters.AddWithValue("@id", id);
        var result = await cmdCargo.ExecuteScalarAsync();
        
        if (result != null) cargo = result.ToString() ?? ""; 
    }

    string queryVendas = cargo == "Gerente" 
        ? "SELECT COUNT(*), COALESCE(SUM(valor), 0) FROM venda" 
        : "SELECT COUNT(*), COALESCE(SUM(valor), 0) FROM venda WHERE colaboradorId = @id";

    using (var cmd = new NpgsqlCommand(queryVendas, conexao)) {
        if (cargo != "Gerente") cmd.Parameters.AddWithValue("@id", id);
        
        using (var reader = await cmd.ExecuteReaderAsync()) {
            if (await reader.ReadAsync()) {
                int qtdVendas = reader.IsDBNull(0) ? 0 : Convert.ToInt32(reader[0]);
                decimal somaVendas = reader.IsDBNull(1) ? 0 : Convert.ToDecimal(reader[1]);
                
                decimal ticketMedio = qtdVendas > 0 ? somaVendas / qtdVendas : 0;
                
                return Results.Ok(new { sucesso = true, ticket = ticketMedio });
            }
        }
    }
    return Results.Ok(new { sucesso = true, ticket = 0 });
});

app.MapGet("/api/limpar-gatilhos", () => {
    return Results.Ok(new { mensagem = "Banco de dados Supabase (PostgreSQL) com triggers nativas ativas." });
});

app.Run();

public class LoginRequest
{
    public string Email { get; set; } = string.Empty;
    public string Senha { get; set; } = string.Empty;
    public string Cargo { get; set; } = string.Empty;
}

public class VendaRequest
{
    public int ColaboradorId { get; set; }
    public string Sku { get; set; } = string.Empty;
    public decimal Valor { get; set; }
}

public class MetasRequest
{
    public int ColaboradorId { get; set; }
    public decimal ValorMeta { get; set; }
    public decimal bonusPercentual { get; set; } 
    public string MesReferencia { get; set; } = string.Empty;
}

public class TurnoRequest {
    public int ColaboradorId { get; set; }
    public string Status { get; set; } = string.Empty;
}

public class TarefaRequest
{
    public string Titulo { get; set; } = string.Empty;
    public string Descricao { get; set; } = string.Empty;
    public string Prioridade { get; set; } = string.Empty;
    public DateTime DataTarefa { get; set; }
}

public class TarefaStatusRequest
{
    public bool Status { get; set; }
    public string NomeConcluinte { get; set; } = string.Empty;
}

public class OcorrenciaRequest {
    public int ColaboradorId { get; set; }
    public string DataOcorrencia { get; set; } = string.Empty;
    public string Tipo { get; set; } = string.Empty;
    public string Anexo { get; set; } = string.Empty;
}

public class FuncionarioRequest {
    public int UserId { get; set; }
    public int ColabId { get; set; }
    public string Nome { get; set; } = string.Empty;
    public string Email { get; set; } = string.Empty;
    public string Senha { get; set; } = string.Empty;
    public string Cargo { get; set; } = string.Empty;
    
    public string DataAdmissao { get; set; } = string.Empty; 
    public decimal SalarioBase { get; set; } 
}

public class StatusRequest {
    public int UserId { get; set; }
    public string NovoStatus { get; set; } = string.Empty;
}
