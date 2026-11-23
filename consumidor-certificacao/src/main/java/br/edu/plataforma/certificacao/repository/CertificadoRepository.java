package br.edu.plataforma.certificacao.repository;

import br.edu.plataforma.certificacao.entity.Certificado;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CertificadoRepository extends JpaRepository<Certificado, Long> {
    boolean existsByEventoId(String eventoId);
}