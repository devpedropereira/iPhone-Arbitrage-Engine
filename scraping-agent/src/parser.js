const IPHONE_PATTERN = /iphone\s*(1[1-5])(?:\s*(pro\s*max|pro|plus|max))?.*?(\d+)\s*(?:gb|tb|gigas|giga)/i;

export function parsearAnuncios(anunciosBrutos) {
  const dtos = [];
  for (const bruto of anunciosBrutos) {
    const resultado = parsearUm(bruto);
    if (resultado.valido) dtos.push(resultado.dto);
  }
  return dtos;
}

function parsearUm({ titulo, preco, link }) {
  if (!titulo || titulo.trim().length < 5 || !IPHONE_PATTERN.test(titulo)) return { valido: false };
  const precoNumerico = extrairPreco(preco);
  if (precoNumerico === null || precoNumerico < 200) return { valido: false };
  return { valido: true, dto: { titulo: titulo.trim(), preco: precoNumerico, link: link.trim() } };
}

function extrairPreco(texto) {
  if (!texto) return null;
  let limpo = texto.replace(/R\$|\s/g, '').trim();
  if (limpo.includes(',')) limpo = limpo.replace(/\./g, '').replace(',', '.');
  else if ((limpo.match(/\./g) || []).length === 1 && limpo.indexOf('.') === limpo.length - 4) limpo = limpo.replace('.', '');
  const valor = parseFloat(limpo);
  return isNaN(valor) ? null : valor;
}