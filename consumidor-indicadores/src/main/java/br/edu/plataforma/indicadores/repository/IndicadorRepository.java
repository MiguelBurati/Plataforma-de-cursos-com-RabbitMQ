package br.edu.plataforma.indicadores.repository;

import br.edu.plataforma.indicadores.entity.Indicador;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface IndicadorRepository extends JpaRepository<Indicador, String> {

    @Modifying
    @Transactional
    @Query(value = "INSERT INTO indicador(curso_id, total) VALUES (:cursoId, 1) " +
            "ON CONFLICT (curso_id) DO UPDATE SET total = indicador.total + 1",
            nativeQuery = true)
    void incrementarTotal(@Param("cursoId") String cursoId);
}