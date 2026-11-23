package br.edu.plataforma.acompanhamento.controller;

import br.edu.plataforma.acompanhamento.entity.RegistroAcompanhamento;
import br.edu.plataforma.acompanhamento.repository.RegistroAcompanhamentoRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/registros")
public class RegistroAcompanhamentoController {

    private final RegistroAcompanhamentoRepository repository;

    public RegistroAcompanhamentoController(RegistroAcompanhamentoRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<RegistroAcompanhamento> listar() {
        return repository.findAll();
    }
}