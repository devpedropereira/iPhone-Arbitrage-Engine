package com.robo.api.controller;

import com.robo.api.dto.AnuncioDTO;
import com.robo.api.model.AnuncioEncontrado;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.robo.api.service.AnaliseService;

import java.util.List;

@RestController
@RequestMapping("/api/anuncios")
public class AnuncioController {

    private final AnaliseService analiseService;

    public AnuncioController(AnaliseService analiseService) {
        this.analiseService = analiseService;
    }

    @PostMapping
    public ResponseEntity<List<AnuncioEncontrado>> processarAnuncios(@RequestBody List<AnuncioDTO> anuncios) {
        if (anuncios == null || anuncios.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        List<AnuncioEncontrado> resultado = analiseService.processarAnuncios(anuncios);
        return ResponseEntity.ok(resultado);
    }

    @GetMapping("/oportunidades")
    public ResponseEntity<List<AnuncioEncontrado>> listarOportunidades() {
        return ResponseEntity.ok(analiseService.listarOportunidades());
    }
}