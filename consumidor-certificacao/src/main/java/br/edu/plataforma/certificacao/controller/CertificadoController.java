package br.edu.plataforma.certificacao.controller;

import br.edu.plataforma.certificacao.entity.Certificado;
import br.edu.plataforma.certificacao.repository.CertificadoRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/certificados")
public class CertificadoController {

    private final CertificadoRepository repository;

    public CertificadoController(CertificadoRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<Certificado> listar() {
        return repository.findAll();
    }
}