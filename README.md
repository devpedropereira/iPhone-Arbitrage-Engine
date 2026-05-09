# 🚜 Marketplace Profit Sentinel (iPhone Arbitrage Engine)

> Um ecossistema completo (Full-Stack) de mineração de dados e arbitragem para o Facebook Marketplace, focado em identificar oportunidades de compra e revenda de iPhones em tempo real.

![Status](https://img.shields.io/badge/Status-Ativo-success)
![NodeJS](https://img.shields.io/badge/Node.js-Scraping_Agent-339933?logo=nodedotjs)
![Java](https://img.shields.io/badge/Java_21-Analysis_API-007396?logo=java)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-4.0-6DB33F?logo=springboot)
![MySQL](https://img.shields.io/badge/MySQL-Database-4479A1?logo=mysql)

---

## 📖 Visão Geral

O **Marketplace Profit Sentinel** é um sistema automatizado projetado para monitorar anúncios locais (Campinas/SP) de smartphones da Apple (do iPhone 11 ao 16, incluindo as versões Pro, Max, Plus e Mini). 

O sistema coleta anúncios continuamente, extrai o modelo e a capacidade de armazenamento do título, cruza essas informações com um "gabarito" (teto) de preços pré-estabelecido em um banco de dados relacional, e envia notificações instantâneas via Telegram apenas para os anúncios que apresentam margem de lucro para revenda (arbitragem).

## 🏗️ Arquitetura do Sistema (Monorepo)

O projeto é dividido em dois microsserviços principais:

1. **`scraping-agent` (O Trator - Node.js):** Responsável por varrer o Facebook Marketplace simulando uma sessão real, extraindo os títulos, preços e links dos anúncios e enviando lotes de dados em formato JSON para a API de análise.

2. **`analysis-api` (O Cérebro - Java / Spring Boot):** Recebe os dados brutos, aplica *Expressões Regulares (Regex)* complexas para identificar os modelos exatos e sua capacidade de armazenamento. Compara o preço anunciado com a tabela `gabarito_precos` no MySQL e, se o anúncio for classificado como "OPORTUNIDADE", dispara um alerta via API do Telegram.

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
