package br.edu.plataforma.comunicacao.repository;

import br.edu.plataforma.comunicacao.entity.Aviso;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AvisoRepository extends JpaRepository<Aviso, Long> {
    boolean existsByEventoId(String eventoId);
}