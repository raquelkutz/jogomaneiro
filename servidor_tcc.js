const http = require('http');
const fs = require('fs');
const path = require('path');
const { spawn, exec } = require('child_process');

const PORT = 3000;
const TCC_DIR = path.join(__dirname, 'novoprojeto_tcc');
const GAME_DIR = path.join(__dirname, 'jogomaneiro-main');

let javaGameProcess = null;
const sseClients = [];
const eventHistory = [];

const MIME_TYPES = {
  '.html': 'text/html; charset=utf-8',
  '.css': 'text/css; charset=utf-8',
  '.js': 'application/javascript; charset=utf-8',
  '.json': 'application/json; charset=utf-8',
  '.png': 'image/png',
  '.jpg': 'image/jpeg',
  '.jpeg': 'image/jpeg',
  '.webp': 'image/webp',
  '.jfif': 'image/jpeg',
  '.svg': 'image/svg+xml',
  '.ttf': 'font/ttf',
  '.woff': 'font/woff',
  '.woff2': 'font/woff2',
  '.wav': 'audio/wav',
  '.mp3': 'audio/mpeg',
  '.ico': 'image/x-icon'
};

function broadcastEvent(eventData) {
  eventHistory.push(eventData);
  if (eventHistory.length > 50) eventHistory.shift();

  const dataStr = `data: ${JSON.stringify(eventData)}\n\n`;
  sseClients.forEach(res => {
    try {
      res.write(dataStr);
    } catch (e) {
      // client disconnected
    }
  });
}

function launchJavaGame() {
  if (javaGameProcess && !javaGameProcess.killed) {
    return { success: true, message: 'O jogo já está em execução!', isRunning: true, pid: javaGameProcess.pid };
  }

  try {
    const vbsPath = path.join(__dirname, 'lancar_jogo.vbs');

    // wscript.exe executa o .vbs que usa WScript.Shell com janela visível (1)
    const child = spawn('wscript.exe', [vbsPath], {
      cwd: __dirname,
      stdio: 'ignore',
      detached: true,
      windowsHide: false
    });

    child.unref();
    javaGameProcess = child;

    child.on('close', code => {
      console.log(`[JogoAudrey] Processo encerrado com código ${code}`);
      javaGameProcess = null;
      broadcastEvent({ type: 'GAME_CLOSED', code, timestamp: new Date().toISOString() });
    });

    broadcastEvent({ type: 'GAME_STARTED', pid: child.pid, timestamp: new Date().toISOString() });
    console.log('[JogoAudrey] Jogo iniciado via wscript — janela visível em instantes!');
    return { success: true, message: 'Jogo Audrey iniciado! A janela abrirá em instantes.', isRunning: true, pid: child.pid };
  } catch (err) {
    console.error('Erro ao iniciar Jogo Audrey:', err);
    return { success: false, error: err.message };
  }
}

const server = http.createServer((req, res) => {
  const parsedUrl = new URL(req.url, `http://${req.headers.host || 'localhost'}`);
  const pathname = decodeURIComponent(parsedUrl.pathname);

  // Headers CORS para suportar requisições locais
  res.setHeader('Access-Control-Allow-Origin', '*');
  res.setHeader('Access-Control-Allow-Methods', 'GET, POST, OPTIONS');
  res.setHeader('Access-Control-Allow-Headers', 'Content-Type');

  if (req.method === 'OPTIONS') {
    res.writeHead(204);
    res.end();
    return;
  }

  // --- ROTAS DA API ---

  // 1. Iniciar o Jogo Java Audrey
  if (pathname === '/api/iniciar-jogo' && req.method === 'POST') {
    const result = launchJavaGame();
    res.writeHead(result.success ? 200 : 500, { 'Content-Type': 'application/json' });
    res.end(JSON.stringify(result));
    return;
  }

  // 2. Status do Jogo
  if (pathname === '/api/status-jogo' && req.method === 'GET') {
    const isRunning = javaGameProcess !== null && !javaGameProcess.killed;
    res.writeHead(200, { 'Content-Type': 'application/json' });
    res.end(JSON.stringify({ isRunning, pid: isRunning ? javaGameProcess.pid : null }));
    return;
  }

  // 3. Receber eventos enviados pelo Jogo Java
  if (pathname === '/api/evento-jogo' && req.method === 'POST') {
    let body = '';
    req.on('data', chunk => { body += chunk; });
    req.on('end', () => {
      try {
        const eventData = JSON.parse(body || '{}');
        console.log('[API Evento do Jogo]', eventData);
        broadcastEvent(eventData);
        res.writeHead(200, { 'Content-Type': 'application/json' });
        res.end(JSON.stringify({ success: true }));
      } catch (err) {
        res.writeHead(400, { 'Content-Type': 'application/json' });
        res.end(JSON.stringify({ success: false, error: 'JSON inválido' }));
      }
    });
    return;
  }

  // 4. Stream SSE de eventos em tempo real para a página web
  if (pathname === '/api/eventos' && req.method === 'GET') {
    res.writeHead(200, {
      'Content-Type': 'text/event-stream',
      'Cache-Control': 'no-cache',
      'Connection': 'keep-alive'
    });
    res.write(`data: ${JSON.stringify({ type: 'CONNECTED', isGameRunning: javaGameProcess !== null })}\n\n`);

    sseClients.push(res);
    req.on('close', () => {
      const idx = sseClients.indexOf(res);
      if (idx !== -1) sseClients.splice(idx, 1);
    });
    return;
  }

  // --- SERVIR ARQUIVOS ESTÁTICOS DO TCC (novoprojeto_tcc) ---
  const targetPath = pathname === '/' ? 'home.html' : pathname;
  let filePath = path.join(TCC_DIR, targetPath);

  // Fallback 1: pasta pública do Android assets
  if (!fs.existsSync(filePath)) {
    const publicCandidate = path.join(TCC_DIR, 'android', 'app', 'src', 'main', 'assets', 'public', targetPath);
    if (fs.existsSync(publicCandidate) && !fs.statSync(publicCandidate).isDirectory()) {
      filePath = publicCandidate;
    }
  }

  // Fallback 2: diretório raiz do projeto
  if (!fs.existsSync(filePath)) {
    const rootCandidate = path.join(__dirname, targetPath);
    if (fs.existsSync(rootCandidate) && !fs.statSync(rootCandidate).isDirectory()) {
      filePath = rootCandidate;
    }
  }

  fs.stat(filePath, (err, stats) => {
    if (err || !stats.isFile()) {
      res.writeHead(404, { 'Content-Type': 'text/plain; charset=utf-8' });
      res.end('Arquivo não encontrado: ' + pathname);
      return;
    }

    const ext = path.extname(filePath).toLowerCase();
    const contentType = MIME_TYPES[ext] || 'application/octet-stream';

    res.writeHead(200, { 'Content-Type': contentType });
    fs.createReadStream(filePath).pipe(res);
  });
});

server.listen(PORT, () => {
  console.log(`=======================================================`);
  console.log(`✨ Servidor Hobby Quest + Jogo Audrey Online! ✨`);
  console.log(`👉 Abra no navegador: http://localhost:${PORT}/`);
  console.log(`=======================================================`);
});
