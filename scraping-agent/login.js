import { chromium } from 'playwright';
import { CONFIG }   from './config.js';

const USER_AGENT =
  'Mozilla/5.0 (Windows NT 10.0; Win64; x64) ' +
  'AppleWebKit/537.36 (KHTML, like Gecko) ' +
  'Chrome/124.0.0.0 Safari/537.36';

async function fazerLogin() {
  console.log('🌐 Abrindo o Facebook para login manual...');
  console.log('   Faça login normalmente no browser que vai abrir.');
  console.log('   Quando estiver na página inicial do Facebook, volte aqui e pressione ENTER.\n');

  const browser = await chromium.launch({ headless: false });
  const context = await browser.newContext({
    userAgent: USER_AGENT,
    viewport:  { width: 1366, height: 768 },
    locale:    'pt-BR',
  });

  const page = await context.newPage();
  await page.goto('https://www.facebook.com/login', { waitUntil: 'domcontentloaded' });

  await new Promise(resolve => {
    process.stdin.setRawMode(true);
    process.stdin.resume();
    process.stdin.once('data', () => {
      process.stdin.setRawMode(false);
      process.stdin.pause();
      resolve();
    });
  });

  await context.storageState({ path: CONFIG.SESSION_PATH });
  console.log(`\n✅ Sessão salva em "${CONFIG.SESSION_PATH}"`);
  console.log('   Agora rode "npm start" para iniciar o trator.\n');

  await browser.close();
}

fazerLogin().catch(err => {
  console.error('💥 Erro durante o login:', err);
  process.exit(1);
});