package com.robo.api.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "historico_precos")
public class HistoricoPreco {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "anuncio_id", nullable = false)
    private AnuncioEncontrado anuncio;

    @Column(name = "preco", nullable = false)
    private BigDecimal preco;

    @Column(name = "registrado_em", nullable = false)
    private LocalDateTime registradoEm;

    @PrePersist
    public void prePersist() {
        this.registradoEm = LocalDateTime.now();
    }

    public Long getId() { return id; }

    public AnuncioEncontrado getAnuncio() { return anuncio; }
    public void setAnuncio(AnuncioEncontrado anuncio) { this.anuncio = anuncio; }

    public BigDecimal getPreco() { return preco; }
    public void setPreco(BigDecimal preco) { this.preco = preco; }

    public LocalDateTime getRegistradoEm() { return registradoEm; }
}