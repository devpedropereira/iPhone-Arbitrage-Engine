import { executarScraper }              from './src/scraper.js';
import { parsearAnuncios }             from './src/parser.js';
import { enviarLote, exibirOportunidades, notificarErro } from './src/api-client.js';

async function main() {
  console.log('🚜 Trator iniciado\n');
  const inicio = Date.now();

  const resultado = await executarScraper();

  if (!resultado.sucesso) {
    const mensagens = {
      SESSION_EXPIRED:   '❌ Sessão do Facebook expirada!\n\nExecute "npm run login" no terminal para renovar o acesso.',
      SESSION_NOT_FOUND: '❌ Arquivo session.json não encontrado!\n\nExecute "npm run login" para fazer o login inicial.',
      NAVIGATION_FAILED: '❌ Falha de conexão ao navegar no Facebook.\n\nVerifique sua internet e tente novamente.',
    };

    const msg = mensagens[resultado.motivo] || '❌ Erro desconhecido no scraper.';
    console.error(msg);

    await notificarErro(msg);
    process.exit(1);
  }

  const brutos = resultado.anuncios;

  if (brutos.length === 0) {
    console.log('⚠ Nenhum anúncio coletado. Encerrando.');
    return;
  }

  console.log('\n🔎 Parseando anúncios...');
  const dtos = parsearAnuncios(brutos);

  if (dtos.length === 0) {
    console.log('⚠ Nenhum DTO válido após o parser. Encerrando.');
    return;
  }

  console.log(`\n📡 Enviando ${dtos.length} anúncios para a API...`);
  const processados = await enviarLote(dtos);

  exibirOportunidades(processados);

  const segundos = ((Date.now() - inicio) / 1000).toFixed(1);
  console.log(`✅ Ciclo completo em ${segundos}s`);
}

main().catch(err => {
  console.error('💥 Erro fatal:', err);
  process.exit(1);
});