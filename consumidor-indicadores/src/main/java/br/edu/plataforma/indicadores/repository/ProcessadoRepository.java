package br.edu.plataforma.indicadores.repository;

import br.edu.plataforma.indicadores.entity.Processado;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProcessadoRepository extends JpaRepository<Processado, Long> {}