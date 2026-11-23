package br.edu.plataforma.auditoria.controller;

import br.edu.plataforma.auditoria.entity.Auditoria;
import br.edu.plataforma.auditoria.repository.AuditoriaRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/auditoria")
public class AuditoriaController {

    private final AuditoriaRepository repository;

    public AuditoriaController(AuditoriaRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<Auditoria> listar(@RequestParam(required = false) String tipo) {
        if (tipo != null) {
            return repository.findAll().stream()
                    .filter(a -> a.getTipo().equals(tipo))
                    .toList();
        }
        return repository.findAll();
    }
}