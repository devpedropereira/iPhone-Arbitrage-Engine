# Marketplace Profit Sentinel (iPhone Arbitrage Engine)

> Protótipo pessoal de estudo: um coletor em Node.js e uma API em Java/Spring Boot que classificam anúncios de iPhone por modelo, armazenamento e preço.

## Status

- **Protótipo de estudo.** Este repositório não documenta implantação, execução contínua ou resultados de uso.
- O código foi publicado como estrutura inicial (maio/2026). Os testes automatizados se limitam ao teste padrão de carregamento de contexto do Spring Boot.
- A coleta automatizada de páginas do Facebook Marketplace pode violar os Termos de Uso da Meta. O código é mantido apenas como referência de arquitetura e não deve ser executado contra a plataforma.

## Visão geral

O protótipo foi pensado para anúncios locais (Campinas/SP) de iPhone 11 a 16. A ideia: extrair modelo e capacidade de armazenamento do título, comparar o preço com uma tabela de referência ("gabarito") em MySQL e enviar uma notificação pelo Telegram quando o preço ficar abaixo do teto definido.

## 🏗️ Arquitetura do Sistema (Monorepo)

O projeto é dividido em dois componentes:

1. **`scraping-agent` (O Trator - Node.js):** Responsável por varrer o Facebook Marketplace simulando uma sessão real, extraindo os títulos, preços e links dos anúncios e enviando lotes de dados em formato JSON para a API de análise.

2. **`analysis-api` (O Cérebro - Java / Spring Boot):** Recebe os dados brutos, aplica *Expressões Regulares (Regex)* para identificar os modelos exatos e sua capacidade de armazenamento. Compara o preço anunciado com a tabela `gabarito_precos` no MySQL e, se o anúncio for classificado como "OPORTUNIDADE", envia um alerta via API do Telegram.

## 📂 Estrutura de Pastas

```text
Marketplace-Profit-Sentinel/
│
├── scraping-agent/           # Frontend Scraper em Node.js
│   ├── index.js              # Loop de raspagem e envio para a API
│   ├── config.js             # Termos de busca e configurações
│   └── package.json          
│
├── analysis-api/             # Backend em Java 21 / Spring Boot 4
│   ├── src/main/java/.../    # Controllers, Services, Models e Repositories
│   ├── pom.xml               # Dependências do Maven
│   └── src/main/resources/   # application.properties (não versionado)
│
├── .gitignore                # Regras de exclusão de arquivos sensíveis
└── README.md                 # Documentação do projeto
