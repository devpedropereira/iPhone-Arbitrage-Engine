import axios from 'axios';
import { CONFIG } from '../config.js';

export async function enviarLote(dtos) {
  if (dtos.length === 0) return [];

  try {
    const { data } = await axios.post(CONFIG.API_URL, dtos, {
      headers: { 'Content-Type': 'application/json' },
      timeout: 15_000,
    });
    return data;
  } catch (err) {
    if (err.response) {
      console.error(`   ❌ API retornou ${err.response.status}:`, err.response.data);
    } else {
      console.error(`   ❌ Erro ao conectar na API: ${err.message}`);
    }
    return [];
  }
}

export function exibirOportunidades(anunciosProcessados) {
  const oportunidades = anunciosProcessados.filter(
    a => a.statusOportunidade === 'OPORTUNIDADE'
  );

  if (oportunidades.length === 0) {
    console.log('   📭 Nenhuma oportunidade neste ciclo.');
    return;
  }

  console.log(`\n${'─'.repeat(60)}`);
  console.log(`🔥 ${oportunidades.length} OPORTUNIDADE(S) ENCONTRADA(S):`);
  console.log('─'.repeat(60));

  for (const op of oportunidades) {
    console.log(`\n  📱 ${op.modeloIdentificado}`);
    console.log(`  💰 R$${op.precoAnunciado}`);
    console.log(`  ⭐ Score: ${op.score ?? 'N/A'} | ${op.classificacaoScore ?? ''}`);
    console.log(`  🔗 ${op.link}`);
  }

  console.log(`\n${'─'.repeat(60)}\n`);
}

export async function notificarErro(mensagem) {
  const token  = process.env.TELEGRAM_TOKEN;
  const chatId = process.env.TELEGRAM_CHAT_ID;

  if (!token || !chatId) {
    console.warn('⚠ TELEGRAM_TOKEN ou TELEGRAM_CHAT_ID não configurados no .env — notificação ignorada');
    return;
  }

  try {
    await axios.post(`https://api.telegram.org/bot${token}/sendMessage`, {
      chat_id:    chatId,
      text:       `🤖 <b>Trator — Alerta</b>\n\n${mensagem}`,
      parse_mode: 'HTML',
    });
  } catch (err) {
    console.error('⚠ Falha ao notificar Telegram:', err.message);
  }
}