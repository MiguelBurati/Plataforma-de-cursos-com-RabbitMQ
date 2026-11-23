package br.edu.plataforma.indicadores.service;

import br.edu.plataforma.indicadores.entity.Processado;
import br.edu.plataforma.indicadores.repository.IndicadorRepository;
import br.edu.plataforma.indicadores.repository.ProcessadoRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class IndicadorService {

    private final ProcessadoRepository processadoRepo;
    private final IndicadorRepository indicadorRepo;

    public IndicadorService(ProcessadoRepository processadoRepo, IndicadorRepository indicadorRepo) {
        this.processadoRepo = processadoRepo;
        this.indicadorRepo = indicadorRepo;
    }

    @Transactional
    public void registrarConclusao(String eventoId, String cursoId) {
        try {
            // Tenta salvar o ID do evento. Se a outra instância já salvou, vai lançar erro (Unique Constraint)
            processadoRepo.saveAndFlush(new Processado(eventoId));
        } catch (DataIntegrityViolationException e) {
            // Se cair aqui, significa que o evento já foi processado. Abortamos.
            System.out.println("Evento " + eventoId + " já processado por outra instância. Ignorando.");
            return;
        }

        // Se passou, incrementa o contador de forma atômica
        indicadorRepo.incrementarTotal(cursoId);
        System.out.println("Indicador atualizado para o curso: " + cursoId);
    }
}