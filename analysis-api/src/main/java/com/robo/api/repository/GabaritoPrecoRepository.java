package com.robo.api.repository;

import com.robo.api.model.GabaritoPreco;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GabaritoPrecoRepository extends JpaRepository<GabaritoPreco, Long> {
    List<GabaritoPreco> findByModeloAndVersaoAndArmazenamentoGb(String modelo, String versao, Integer armazenamentoGb);
}
