package br.edu.plataforma.auditoria.repository;

import br.edu.plataforma.auditoria.entity.Auditoria;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditoriaRepository extends JpaRepository<Auditoria, Long> {
    boolean existsByEventoId(String eventoId);
}