package br.edu.plataforma.comunicacao.controller;

import br.edu.plataforma.comunicacao.entity.Aviso;
import br.edu.plataforma.comunicacao.repository.AvisoRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/avisos")
public class AvisoController {

    private final AvisoRepository repository;

    public AvisoController(AvisoRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<Aviso> listar() {
        return repository.findAll();
    }
}