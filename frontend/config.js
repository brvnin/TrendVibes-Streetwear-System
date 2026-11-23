// ==============================================================================
// CONFIGURAÇÃO CENTRALIZADA DA API - TRENDVIBES STREETWEAR SYSTEM
// ==============================================================================

(function() {
    // 1. Permite customizar a URL da API via localStorage (ex: se o usuário quiser alterar)
    const customApiUrl = localStorage.getItem('TRENDVIBES_API_URL');

    // 2. Detecta se está rodando localmente (localhost ou 127.0.0.1)
    const isLocal = window.location.hostname === 'localhost' || window.location.hostname === '127.0.0.1' || window.location.protocol === 'file:';

    // 3. Define a URL da API:
    // Se tiver URL customizada, usa ela. Se for local, usa localhost:5208. Se for produção (Render), usa a URL do Web Service no Render.
    window.API_BASE_URL = customApiUrl 
        ? customApiUrl.replace(/\/$/, '') 
        : (isLocal ? 'http://localhost:5208' : 'https://trendvibes-api.onrender.com');

    console.log('[TrendVibes] Conectando à API em:', window.API_BASE_URL);
})();
