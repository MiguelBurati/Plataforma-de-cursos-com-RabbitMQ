package br.edu.plataforma.indicadores.controller;

import br.edu.plataforma.indicadores.entity.Indicador;
import br.edu.plataforma.indicadores.repository.IndicadorRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/indicadores")
public class IndicadorController {

    private final IndicadorRepository repository;

    public IndicadorController(IndicadorRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<Indicador> listar() {
        return repository.findAll();
    }
}