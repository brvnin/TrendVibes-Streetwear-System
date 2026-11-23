let accState = { contrast: false, dyslexia: false, tts: false, fontSizeScale: 1.0 };
const drawer = document.getElementById('accessibility-panel');
const root = document.documentElement;

// Botões principais de abertura
document.getElementById('accessibility-btn').onclick = () => drawer.classList.remove('hidden');
document.getElementById('close-accessibility').onclick = () => drawer.classList.add('hidden');

// 1. Acessibilidade Visual
document.getElementById('toggle-contrast').onclick = () => {
    accState.contrast = !accState.contrast;
    document.body.classList.toggle('high-contrast-mode', accState.contrast);
};

document.getElementById('toggle-dyslexia').onclick = () => {
    accState.dyslexia = !accState.dyslexia;
    document.body.classList.toggle('dyslexia-mode', accState.dyslexia);
};

// 2. Leitor de voz (TTS)
const ttsBtn = document.getElementById('toggle-tts');
ttsBtn.onclick = () => {
    accState.tts = !accState.tts;
    ttsBtn.innerText = accState.tts ? "🔊 DESATIVAR NARRADOR" : "🔊 ATIVAR NARRADOR";
    if (accState.tts) speakText("Leitor de tela ativado. Passe o mouse nos elementos para ouvir.");
    else window.speechSynthesis.cancel();
};

// Função global de fala (usada pelos botões de fonte e pelo mouseover)
function speakText(text) {
    if (!accState.tts) return;
    window.speechSynthesis.cancel();
    const utterance = new SpeechSynthesisUtterance(text);
    utterance.lang = 'pt-BR';
    utterance.rate = 1.1;
    window.speechSynthesis.speak(utterance);
}

// 3. Controle de Fonte
document.getElementById('font-decrease').onclick = () => {
    accState.fontSizeScale = Math.max(0.8, accState.fontSizeScale - 0.1);
    root.style.fontSize = (accState.fontSizeScale * 100) + "%";
    speakText("Tamanho da fonte diminuído");
};

document.getElementById('font-reset').onclick = () => {
    accState.fontSizeScale = 1.0;
    root.style.fontSize = "100%";
    speakText("Tamanho da fonte normal");
};

document.getElementById('font-increase').onclick = () => {
    accState.fontSizeScale = Math.min(1.5, accState.fontSizeScale + 0.1);
    root.style.fontSize = (accState.fontSizeScale * 100) + "%";
    speakText("Tamanho da fonte aumentado");
};

// 4. Evento de Mouseover para narração automática
document.body.addEventListener('mouseover', (e) => {
    if (!accState.tts) return;
    const target = e.target.closest('h1, h2, h3, h4, button, a, label, span, p');
    if (target && target.innerText.trim()) {
        if (target.dataset.spoken === 'true') return;
        speakText(target.innerText.trim());
        target.dataset.spoken = 'true';
        target.addEventListener('mouseout', () => { target.dataset.spoken = 'false'; }, {once: true});
    }
});

// Redirecionamento simples (Foco no Front-end)
document.getElementById('login-form').onsubmit = (e) => {
    e.preventDefault();
    const cargo = document.getElementById('cargo').value;
    
    // Oculta vitrine e login
    document.getElementById('hero-view').classList.add('hidden');
    document.getElementById('catalogo').classList.add('hidden');
    document.getElementById('login-portal').classList.add('hidden');
    
    // Mostra o painel correto
    if(cargo === 'vendedor') {
        document.getElementById('vendedor-painel').classList.remove('hidden');
    }
    // ... outros cargos virão aqui
};