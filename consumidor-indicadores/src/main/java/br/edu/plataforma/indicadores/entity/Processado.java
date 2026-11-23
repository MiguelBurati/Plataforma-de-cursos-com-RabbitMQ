package br.edu.plataforma.indicadores.entity;

import jakarta.persistence.*;

@Entity
public class Processado {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String eventoId;

    public Processado() {}
    public Processado(String eventoId) { this.eventoId = eventoId; }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getEventoId() { return eventoId; }
    public void setEventoId(String eventoId) { this.eventoId = eventoId; }
}