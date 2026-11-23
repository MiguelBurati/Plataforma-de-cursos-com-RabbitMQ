package br.edu.plataforma.acompanhamento.repository;

import br.edu.plataforma.acompanhamento.entity.RegistroAcompanhamento;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RegistroAcompanhamentoRepository extends JpaRepository<RegistroAcompanhamento, Long> {
    boolean existsByEventoId(String eventoId);
}