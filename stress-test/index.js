// Stress-test własnego serwera Minecraft botami mineflayer.
// Boty łączą się po kolei, logują w AuthMe, chodzą losowo i piszą na czacie.
// Na koniec wypisuje podsumowanie: ile weszło, kicki, ping i szacowany TPS serwera.
//
// Uruchomienie:  npm install  &&  npm start
// Parametry z linii komend nadpisują config.json, np.:
//   node index.js --bots 10 --duration 300 --port 25566

'use strict';

const fs = require('fs');
const path = require('path');
const net = require('net');
const mineflayer = require('mineflayer');

// ---------- konfiguracja ----------

const cfg = JSON.parse(fs.readFileSync(path.join(__dirname, 'config.json'), 'utf8'));
const argv = process.argv.slice(2);
for (let i = 0; i < argv.length; i += 2) {
  const key = argv[i].replace(/^--/, '');
  const val = argv[i + 1];
  const map = { bots: 'bots', duration: 'durationSec', port: 'port', host: 'host', delay: 'joinDelayMs', version: 'version' };
  if (!map[key] || val === undefined) {
    console.error(`Nieznany parametr: ${argv[i]}`);
    process.exit(1);
  }
  cfg[map[key]] = ['host', 'version'].includes(key) ? val : Number(val);
}

// Narzędzie jest do testowania WŁASNEGO serwera, więc łączy się tylko z adresami
// lokalnymi / z sieci prywatnej (localhost, 127.x, 10.x, 172.16-31.x, 192.168.x).
function isLocalHost(host) {
  if (host === 'localhost') return true;
  if (!net.isIP(host)) return false;
  if (host === '::1') return true;
  const p = host.split('.').map(Number);
  return p[0] === 127 || p[0] === 10 || (p[0] === 192 && p[1] === 168) || (p[0] === 172 && p[1] >= 16 && p[1] <= 31);
}
if (!isLocalHost(cfg.host)) {
  console.error(`Host "${cfg.host}" nie jest lokalny. Stress-test działa tylko na localhost / sieci LAN (Twój serwer).`);
  process.exit(1);
}

// ---------- log ----------

const logDir = path.join(__dirname, 'logs');
fs.mkdirSync(logDir, { recursive: true });
const logFile = path.join(logDir, `run-${new Date().toISOString().replace(/[:.]/g, '-')}.log`);
const logStream = fs.createWriteStream(logFile);

function log(who, msg) {
  const line = `[${new Date().toTimeString().slice(0, 8)}] [${who}] ${msg}`;
  console.log(line);
  logStream.write(line + '\n');
}

// ---------- statystyki ----------

const stats = new Map(); // nick -> { spawned, kicks, errors, pings[], logins }
const tpsSamples = [];

function stat(nick) {
  if (!stats.has(nick)) stats.set(nick, { spawned: false, everSpawned: false, kicks: [], errors: [], pings: [], authme: null });
  return stats.get(nick);
}

// ---------- bot ----------

const bots = [];
let stopping = false;

function createBot(index) {
  const nick = `${cfg.nickPrefix}${index}`;
  const s = stat(nick);
  log(nick, `łączę z ${cfg.host}:${cfg.port} (${cfg.version})`);

  const bot = mineflayer.createBot({
    host: cfg.host,
    port: cfg.port,
    username: nick,
    version: cfg.version,
    auth: 'offline',
    hideErrors: true,
  });
  bots.push(bot);

  const timers = [];
  let lastTime = null;

  // AuthMe: reaguj na prośbę o rejestrację / logowanie.
  bot.on('messagestr', (text) => {
    const t = text.toLowerCase();
    if (t.includes('/register')) {
      bot.chat(`/register ${cfg.authmePassword} ${cfg.authmePassword}`);
      s.authme = 'register';
      log(nick, 'AuthMe: wysłano /register');
    } else if (t.includes('/login')) {
      bot.chat(`/login ${cfg.authmePassword}`);
      s.authme = 'login';
      log(nick, 'AuthMe: wysłano /login');
    }
  });

  bot.once('spawn', () => {
    s.spawned = true;
    s.everSpawned = true;
    const p = bot.entity.position;
    log(nick, `spawn na ${p.x.toFixed(1)} ${p.y.toFixed(1)} ${p.z.toFixed(1)}`);

    // Szacowany TPS z pakietów czasu (serwer wysyła je co ~20 ticków).
    // Liczy go tylko pierwszy bot, żeby nie dublować próbek.
    if (index === 1) {
      // Paper podaje rosnący "age" świata; gdy serwer go nie wysyła (age stoi),
      // liczymy z pory dnia (działa, gdy doDaylightCycle jest włączony).
      bot.on('time', () => {
        const age = Number(bot.time.age);
        const day = Number(bot.time.timeOfDay);
        const now = Date.now();
        if (lastTime && now - lastTime.t > 500) {
          let ticks = age - lastTime.age;
          if (!(ticks > 0)) ticks = (day - lastTime.day + 24000) % 24000;
          const tps = (ticks / (now - lastTime.t)) * 1000;
          if (tps > 0 && tps < 40) tpsSamples.push(Math.min(20, tps));
        }
        lastTime = { age, day, t: now };
      });
    }

    if (cfg.walk) {
      timers.push(setInterval(() => {
        const dirs = ['forward', 'back', 'left', 'right'];
        dirs.forEach((d) => bot.setControlState(d, false));
        const d = dirs[Math.floor(Math.random() * dirs.length)];
        bot.setControlState(d, true);
        bot.setControlState('jump', Math.random() < 0.3);
        bot.look(Math.random() * Math.PI * 2, 0, true).catch(() => {});
      }, 2000));
    }

    if (cfg.chat) {
      timers.push(setInterval(() => {
        const m = cfg.chatMessages[Math.floor(Math.random() * cfg.chatMessages.length)];
        bot.chat(`${m} #${index}`);
      }, cfg.chatEverySec * 1000 + index * 300));
    }

    timers.push(setInterval(() => {
      const ping = bot.player && typeof bot.player.ping === 'number' ? bot.player.ping : bot._client.latency;
      if (typeof ping === 'number') s.pings.push(ping);
    }, 5000));
  });

  bot.on('kicked', (reason) => {
    let r = reason;
    try { r = typeof reason === 'string' ? reason : JSON.stringify(reason); } catch (_) {}
    s.kicks.push(r);
    log(nick, `KICK: ${r}`);
  });

  bot.on('error', (err) => {
    s.errors.push(err.code || err.message);
    log(nick, `BŁĄD: ${err.code || err.message}`);
  });

  bot.on('end', (why) => {
    s.spawned = false;
    timers.forEach(clearInterval);
    log(nick, `rozłączony (${why})`);
  });
}

// ---------- raporty i koniec ----------

function avg(a) { return a.length ? a.reduce((x, y) => x + y, 0) / a.length : NaN; }

function report() {
  const online = [...stats.values()].filter((s) => s.spawned).length;
  const recent = tpsSamples.slice(-5);
  log('RAPORT', `online ${online}/${cfg.bots}, TPS ~${isNaN(avg(recent)) ? '?' : avg(recent).toFixed(1)}`);
}

function summary() {
  log('WYNIK', '================ PODSUMOWANIE ================');
  let ok = 0;
  for (const [nick, s] of stats) {
    if (s.everSpawned) ok++;
    const ping = s.pings.length ? `${Math.round(avg(s.pings))} ms` : '-';
    log('WYNIK', `${nick}: wszedł=${s.everSpawned ? 'tak' : 'NIE'} authme=${s.authme || '-'} ping=${ping} kicki=${s.kicks.length} błędy=${s.errors.length}`);
  }
  log('WYNIK', `weszło ${ok}/${cfg.bots} botów`);
  if (tpsSamples.length) {
    log('WYNIK', `TPS: min ${Math.min(...tpsSamples).toFixed(1)}, średnio ${avg(tpsSamples).toFixed(1)} (${tpsSamples.length} próbek)`);
  } else {
    log('WYNIK', 'TPS: brak próbek');
  }
  log('WYNIK', `log zapisany: ${logFile}`);
}

function stop() {
  if (stopping) return;
  stopping = true;
  log('TEST', 'koniec testu, rozłączam boty');
  bots.forEach((b) => { try { b.quit('koniec testu'); } catch (_) {} });
  setTimeout(() => { summary(); logStream.end(() => process.exit(0)); }, 2000);
}

process.on('SIGINT', stop);

log('TEST', `start: ${cfg.bots} botów → ${cfg.host}:${cfg.port}, odstęp ${cfg.joinDelayMs} ms, czas ${cfg.durationSec} s`);
for (let i = 1; i <= cfg.bots; i++) {
  setTimeout(() => { if (!stopping) createBot(i); }, (i - 1) * cfg.joinDelayMs);
}
setInterval(report, cfg.reportEverySec * 1000);
setTimeout(stop, cfg.durationSec * 1000 + (cfg.bots - 1) * cfg.joinDelayMs);
