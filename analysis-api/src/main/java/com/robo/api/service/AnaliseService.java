package com.robo.api.service;

import com.robo.api.dto.AnuncioDTO;
import com.robo.api.model.AnuncioEncontrado;
import com.robo.api.model.HistoricoPreco;
import com.robo.api.repository.AnuncioEncontradoRepository;
import com.robo.api.repository.GabaritoPrecoRepository;
import com.robo.api.repository.HistoricoPrecoRepository;
import org.springframework.stereotype.Service;
import com.robo.api.model.GabaritoPreco;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class AnaliseService {

    private final GabaritoPrecoRepository  gabaritoRepo;
    private final AnuncioEncontradoRepository anuncioRepo;
    private final HistoricoPrecoRepository historicoRepo;
    private final TelegramService          telegramService;

    private static final double THRESHOLD_GOLPE = 0.50;

    private static final Pattern IPHONE_PATTERN = Pattern.compile(
            "(?i)iphone\\s*(1[1-5])" +
                    "(?:\\s*(pro\\s*max|pro|plus|max))?" +
                    ".*?(\\d+)\\s*(?:gb|tb|gigas|giga)",
            Pattern.CASE_INSENSITIVE
    );

    public AnaliseService(GabaritoPrecoRepository gabaritoRepo,
                          AnuncioEncontradoRepository anuncioRepo,
                          HistoricoPrecoRepository historicoRepo,
                          TelegramService telegramService) {
        this.gabaritoRepo   = gabaritoRepo;
        this.anuncioRepo    = anuncioRepo;
        this.historicoRepo  = historicoRepo;
        this.telegramService = telegramService;
    }


    public Optional<Map<String, Object>> extrairDadosDoTitulo(String titulo) {
        Matcher matcher = IPHONE_PATTERN.matcher(titulo);
        if (!matcher.find()) return Optional.empty();

        String geracao = matcher.group(1);
        String versao  = matcher.group(2) != null
                ? matcher.group(2).trim().toUpperCase().replace("  ", " ")
                : "";
        int armazenamento = Integer.parseInt(matcher.group(3));

        boolean isTb = titulo.toLowerCase().contains("tb") &&
                titulo.toLowerCase().indexOf("tb") > titulo.toLowerCase().indexOf(matcher.group(3));
        if (isTb) armazenamento *= 1024;

        Map<String, Object> dados = new HashMap<>();
        dados.put("modelo", "iPhone " + geracao);
        dados.put("versao", versao);
        dados.put("armazenamentoGb", armazenamento);
        return Optional.of(dados);
    }


    public List<AnuncioEncontrado> processarAnuncios(List<AnuncioDTO> anunciosRaspados) {
        return anunciosRaspados.stream()
                .filter(dto -> !anuncioRepo.existsByLink(dto.getLink()))
                .map(dto -> {
                    AnuncioEncontrado anuncio = new AnuncioEncontrado();
                    anuncio.setTitulo(dto.getTitulo());
                    anuncio.setPrecoAnunciado(dto.getPreco());
                    anuncio.setLink(dto.getLink());

                    Optional<Map<String, Object>> dadosOpt = extrairDadosDoTitulo(dto.getTitulo());

                    dadosOpt.ifPresentOrElse(dados -> {
                        String modelo        = (String)  dados.get("modelo");
                        String versao        = (String)  dados.get("versao");
                        int    armazenamento = (Integer) dados.get("armazenamentoGb");

                        String modeloCompleto = modelo +
                                (versao.isBlank() ? "" : " " + versao) +
                                " " + armazenamento + "GB";

                        anuncio.setModeloIdentificado(modeloCompleto);

                        List<GabaritoPreco> gabaritos = gabaritoRepo.findByModeloAndVersaoAndArmazenamentoGb(modelo, versao, armazenamento);

                        if (!gabaritos.isEmpty()) {
                            GabaritoPreco gabarito = gabaritos.get(0); // pega o primeiro da lista
                            BigDecimal precoTeto  = gabarito.getPrecoTeto();
                            BigDecimal precoAtual = dto.getPreco();

                            BigDecimal limiteGolpe = precoTeto.multiply(
                                    BigDecimal.valueOf(1 - THRESHOLD_GOLPE)
                            );

                            if (precoAtual.compareTo(limiteGolpe) < 0) {
                                anuncio.setStatusOportunidade("IGNORADO");
                                anuncio.setScore(0);
                                anuncio.setClassificacaoScore("GOLPE");
                            } else if (precoAtual.compareTo(precoTeto) < 0) {
                                anuncio.setStatusOportunidade("OPORTUNIDADE");

                                int score = calcularScore(precoAtual, precoTeto);
                                anuncio.setScore(score);
                                anuncio.setClassificacaoScore(classificarScore(score));

                                telegramService.notificarOportunidade(
                                        modeloCompleto,
                                        precoAtual,
                                        dto.getLink(),
                                        score,
                                        anuncio.getClassificacaoScore()
                                );
                            } else {
                                anuncio.setStatusOportunidade("IGNORADO");
                                anuncio.setScore(null);
                                anuncio.setClassificacaoScore(null);
                            }
                        } else {
                            // Modelo não está no gabarito
                            anuncio.setStatusOportunidade("IGNORADO");
                            anuncio.setScore(null);
                            anuncio.setClassificacaoScore(null);
                        }

                    }, () -> {
                        anuncio.setModeloIdentificado("NÃO IDENTIFICADO");
                        anuncio.setStatusOportunidade("IGNORADO");
                        anuncio.setScore(null);
                        anuncio.setClassificacaoScore(null);
                    });

                    return anuncio;
                })
                .collect(Collectors.collectingAndThen(
                        Collectors.toList(),
                        lista -> {
                            List<AnuncioEncontrado> salvos = anuncioRepo.saveAll(lista);

                            salvos.forEach(a -> {
                                if (a.getPrecoAnunciado() != null) {
                                    HistoricoPreco h = new HistoricoPreco();
                                    h.setAnuncio(a);
                                    h.setPreco(a.getPrecoAnunciado());
                                    historicoRepo.save(h);
                                }
                            });

                            return salvos;
                        }
                ));
    }

    private int calcularScore(BigDecimal precoAtual, BigDecimal precoTeto) {
        BigDecimal desconto = precoTeto.subtract(precoAtual);
        BigDecimal percentual = desconto.divide(precoTeto, 4, RoundingMode.HALF_UP);

        double faixaValida = THRESHOLD_GOLPE; // 50%
        double score = (percentual.doubleValue() / faixaValida) * 100;

        return (int) Math.min(100, Math.max(0, Math.round(score)));
    }

    private String classificarScore(int score) {
        if (score >= 75) return "EXCELENTE";
        if (score >= 50) return "BOA";
        if (score >= 25) return "MODERADA";
        return "FRACA";
    }

    public List<AnuncioEncontrado> listarOportunidades() {
        return anuncioRepo.findByStatusOportunidade("OPORTUNIDADE");
    }
}