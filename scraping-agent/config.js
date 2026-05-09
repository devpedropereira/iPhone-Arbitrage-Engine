import 'dotenv/config';

export const CONFIG = {
  API_URL:              process.env.API_URL      || 'http://localhost:8080/api/anuncios',
  HEADLESS:             process.env.HEADLESS      === 'true',
  SESSION_PATH:         process.env.SESSION_PATH  || './session.json',

  TERMOS_DE_BUSCA: [
    'iphone 11',
    'iphone 12',
    'iphone 13',
    'iphone 14',
    'iphone 15',
  ],

  DELAY_ENTRE_BUSCAS_MS:  4000,
  DELAY_ENTRE_SCROLLS_MS: 1800,
  QUANTIDADE_DE_SCROLLS:  5,
};