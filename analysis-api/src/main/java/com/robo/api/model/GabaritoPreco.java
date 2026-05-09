package com.robo.api.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "gabarito_precos")
public class GabaritoPreco {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @Column(nullable = false)
        private String modelo;

        @Column
        private String versao;

        @Column(name = "armazenamento_gb")
        private Integer armazenamentoGb;

        @Column(name = "preco_teto", nullable = false)
        private BigDecimal precoTeto;

        // Getters e Setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public String getModelo() { return modelo; }
        public void setModelo(String modelo) { this.modelo = modelo; }

        public String getVersao() { return versao; }
        public void setVersao(String versao) { this.versao = versao; }

        public Integer getArmazenamentoGb() { return armazenamentoGb; }
        public void setArmazenamentoGb(Integer armazenamentoGb) { this.armazenamentoGb = armazenamentoGb; }

        public BigDecimal getPrecoTeto() { return precoTeto; }
        public void setPrecoTeto(BigDecimal precoTeto) { this.precoTeto = precoTeto; }
}
