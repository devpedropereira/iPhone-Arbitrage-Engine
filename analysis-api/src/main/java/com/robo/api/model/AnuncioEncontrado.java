package com.robo.api.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "anuncios_encontrados")
public class AnuncioEncontrado {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @Column
        private String titulo;

        @Column(name = "preco_anunciado")
        private BigDecimal precoAnunciado;

        @Column(name = "link", unique = true)
        private String link;

        @Column(name = "modelo_identificado")
        private String modeloIdentificado;

        @Column(name = "status_oportunidade")
        private String statusOportunidade;

        @Column(name = "score")
        private Integer score;

        @Column(name = "classificacao_score")
        private String classificacaoScore;

        @OneToMany(mappedBy = "anuncio", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
        private List<HistoricoPreco> historicoPrecos = new ArrayList<>();

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public String getTitulo() { return titulo; }
        public void setTitulo(String titulo) { this.titulo = titulo; }

        public BigDecimal getPrecoAnunciado() { return precoAnunciado; }
        public void setPrecoAnunciado(BigDecimal precoAnunciado) { this.precoAnunciado = precoAnunciado; }

        public String getLink() { return link; }
        public void setLink(String link) { this.link = link; }

        public String getModeloIdentificado() { return modeloIdentificado; }
        public void setModeloIdentificado(String modeloIdentificado) { this.modeloIdentificado = modeloIdentificado; }

        public String getStatusOportunidade() { return statusOportunidade; }
        public void setStatusOportunidade(String statusOportunidade) { this.statusOportunidade = statusOportunidade; }

        public Integer getScore() { return score; }
        public void setScore(Integer score) { this.score = score; }

        public String getClassificacaoScore() { return classificacaoScore; }
        public void setClassificacaoScore(String classificacaoScore) { this.classificacaoScore = classificacaoScore; }

        public List<HistoricoPreco> getHistoricoPrecos() { return historicoPrecos; }
}