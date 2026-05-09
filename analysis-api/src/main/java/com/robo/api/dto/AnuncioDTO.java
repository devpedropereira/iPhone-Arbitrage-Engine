package com.robo.api.dto;

import java.math.BigDecimal;

public class AnuncioDTO {
    private String titulo;
    private BigDecimal preco;
    private String link;

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public BigDecimal getPreco() { return preco; }
    public void setPreco(BigDecimal preco) { this.preco = preco; }

    public String getLink() { return link; }
    public void setLink(String link) { this.link = link; }
}