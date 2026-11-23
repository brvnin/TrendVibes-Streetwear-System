# Estágio 1: Build e compilação da API .NET 8
FROM mcr.microsoft.com/dotnet/sdk:8.0 AS build
WORKDIR /src

# Copia o arquivo .csproj e restaura as dependências
COPY ["TrendVibes.Api.csproj", "./"]
RUN dotnet restore "TrendVibes.Api.csproj"

# Copia o restante do código-fonte e publica a versão de produção
COPY . .
RUN dotnet publish "TrendVibes.Api.csproj" -c Release -o /app/publish /p:UseAppHost=false

# Estágio 2: Imagem final leve de execução (Runtime)
FROM mcr.microsoft.com/dotnet/aspnet:8.0 AS final
WORKDIR /app
EXPOSE 8080
ENV ASPNETCORE_URLS=http://+:8080

COPY --from=build /app/publish .
ENTRYPOINT ["dotnet", "TrendVibes.Api.dll"]