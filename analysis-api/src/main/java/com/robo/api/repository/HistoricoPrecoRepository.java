package com.robo.api.repository;

import com.robo.api.model.HistoricoPreco;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HistoricoPrecoRepository extends JpaRepository<HistoricoPreco, Long> {

    List<HistoricoPreco> findByAnuncioIdOrderByRegistradoEmDesc(Long anuncioId);
}