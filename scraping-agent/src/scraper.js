import { chromium } from 'playwright';
import { CONFIG } from '../config.js';

const USER_AGENT =
  'Mozilla/5.0 (Windows NT 10.0; Win64; x64) ' +
  'AppleWebKit/537.36 (KHTML, like Gecko) ' +
  'Chrome/124.0.0.0 Safari/537.36';

const VIEWPORT = { width: 1366, height: 768 };

const EXTRA_HEADERS = {
  'Accept-Language':    'pt-BR,pt;q=0.9,en-US;q=0.8,en;q=0.7',
  'Accept':             'text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,*/*;q=0.8',
  'sec-ch-ua':          '"Chromium";v="124", "Google Chrome";v="124", "Not-A.Brand";v="99"',
  'sec-ch-ua-mobile':   '?0',
  'sec-ch-ua-platform': '"Windows"',
};

async function sessaoEstaValida(page) {
  const url = page.url();

  const urlsDeLogin = [
    'facebook.com/login',
    'facebook.com/r.php',
    'facebook.com/?next=',
    'facebook.com/checkpoint',
  ];

  if (urlsDeLogin.some(u => url.includes(u))) {
    return false;
  }

  const botaoLogin = await page.$('a[href*="/login"]').catch(() => null);
  if (botaoLogin) {
    const visivel = await botaoLogin.isVisible().catch(() => false);
    if (visivel) return false;
  }

  const marketplace = await page.$('[aria-label="Facebook"]').catch(() => null);
  return !!marketplace;
}


export async function executarScraper() {
  const browser = await chromium.launch({
    headless: CONFIG.HEADLESS,
    args: [
      '--no-sandbox',
      '--disable-blink-features=AutomationControlled',
    ],
  });

  const contextOpcoes = {
    userAgent:        USER_AGENT,
    viewport:         VIEWPORT,
    locale:           'pt-BR',
    timezoneId:       'America/Sao_Paulo',
    extraHTTPHeaders: EXTRA_HEADERS,
  };

  try {
    const fs = await import('fs');
    if (fs.existsSync(CONFIG.SESSION_PATH)) {
      contextOpcoes.storageState = CONFIG.SESSION_PATH;
      console.log('🔑 Sessão carregada:', CONFIG.SESSION_PATH);
    } else {
      console.warn('⚠ session.json não encontrado — execute "npm run login" primeiro');
      await browser.close();
      return { sucesso: false, motivo: 'SESSION_NOT_FOUND', anuncios: [] };
    }
  } catch (_) {}

  const context = await browser.newContext(contextOpcoes);

  await context.addInitScript(() => {
    Object.defineProperty(navigator, 'webdriver', { get: () => undefined });
  });

  const page = await context.newPage();

  console.log('🔐 Verificando sessão...');
  try {
    await page.goto('https://www.facebook.com/marketplace/', {
      waitUntil: 'domcontentloaded',
      timeout: 20_000,
    });
  } catch (err) {
    await browser.close();
    return { sucesso: false, motivo: 'NAVIGATION_FAILED', anuncios: [] };
  }

  const valida = await sessaoEstaValida(page);

  if (!valida) {
    console.error('❌ Sessão expirada! Execute "npm run login" para renovar.');
    await browser.close();

    return { sucesso: false, motivo: 'SESSION_EXPIRED', anuncios: [] };
  }

  console.log('✅ Sessão válida — iniciando coleta\n');

  const todosAnuncios = [];

  for (const termo of CONFIG.TERMOS_DE_BUSCA) {
    console.log(`🔍 Buscando: "${termo}"`);
    const encontrados = await buscarTermo(page, termo);
    todosAnuncios.push(...encontrados);
    console.log(`   ✔ ${encontrados.length} anúncios coletados`);
    await pausar(CONFIG.DELAY_ENTRE_BUSCAS_MS);
  }

  await browser.close();

  const unicos = deduplicar(todosAnuncios);
  console.log(`\n📦 Total único após deduplicação: ${unicos.length} anúncios`);

  return { sucesso: true, motivo: null, anuncios: unicos };
}


async function buscarTermo(page, termo) {
  const url =
  `https://www.facebook.com/marketplace/campinas/search` +
  `?query=${encodeURIComponent(termo)}` +
  `&exact=false` +
  `&latitude=-22.9099` +
  `&longitude=-47.0626` +
  `&radius=30` +         
  `&deliveryMethod=local_pick_up`;

  try {
    await page.goto(url, { waitUntil: 'domcontentloaded', timeout: 30_000 });
  } catch (err) {
    console.warn(`   ⚠ Timeout ao navegar para "${termo}": ${err.message}`);
    return [];
  }

  await page
    .waitForSelector('a[href*="/marketplace/item/"]', { timeout: 12_000 })
    .catch(() => console.warn('   ⚠ Nenhum card no timeout inicial — continuando'));

  await rolarPagina(page, CONFIG.QUANTIDADE_DE_SCROLLS);

  return extrairCards(page);
}


async function extrairCards(page) {
  return page.evaluate(() => {
    const BASE_URL = 'https://www.facebook.com';
    const links = Array.from(document.querySelectorAll('a[href*="/marketplace/item/"]'));

    return links.map(link => {
      const titulo =
        link.querySelector('span[style*="-webkit-box"]')?.innerText?.trim() ||
        link.querySelector('[class*="x1lliihq"]')?.innerText?.trim()        ||
        link.querySelector('span:not([aria-hidden])')?.innerText?.trim()    ||
        link.getAttribute('aria-label')?.trim()                             ||
        '';

      const spans = Array.from(link.querySelectorAll('span'));
      const preco =
        spans.find(s => s.innerText?.includes('R$'))?.innerText?.trim() ||
        spans.find(s => s.getAttribute('dir') === 'auto')?.innerText?.trim() ||
        '';

      const href = link.getAttribute('href') || '';
      const linkLimpo = BASE_URL + href.split('?')[0];

      return { titulo, preco, link: linkLimpo };
    }).filter(a => a.titulo.length > 3 && a.preco.length > 0);
  });
}


async function rolarPagina(page, vezes) {
  for (let i = 0; i < vezes; i++) {
    await page.evaluate(() =>
      window.scrollBy({ top: window.innerHeight * 1.2, behavior: 'smooth' })
    );
    await pausar(CONFIG.DELAY_ENTRE_SCROLLS_MS);
  }
  await page.evaluate(() => window.scrollTo(0, 0));
}


function pausar(ms) {
  const jitter = Math.floor(Math.random() * 600) - 300;
  return new Promise(resolve => setTimeout(resolve, ms + jitter));
}

function deduplicar(anuncios) {
  const vistos = new Set();
  return anuncios.filter(a => {
    if (vistos.has(a.link)) return false;
    vistos.add(a.link);
    return true;
  });
}