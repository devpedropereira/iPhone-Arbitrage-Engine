package com.robo.api.repository;

import com.robo.api.model.AnuncioEncontrado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AnuncioEncontradoRepository extends JpaRepository<AnuncioEncontrado, Long> {
    List<AnuncioEncontrado> findByStatusOportunidade(String status);

    boolean existsByLink(String link);
}
