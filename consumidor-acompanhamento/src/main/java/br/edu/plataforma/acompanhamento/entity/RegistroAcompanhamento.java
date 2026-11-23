package br.edu.plataforma.acompanhamento.entity;

import jakarta.persistence.*;

@Entity
public class RegistroAcompanhamento {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String eventoId;

    private String tipoEvento;
    private String aluno;
    private String referencia;
    private Double nota; // Pode ser nulo

    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getEventoId() { return eventoId; }
    public void setEventoId(String eventoId) { this.eventoId = eventoId; }
    public String getTipoEvento() { return tipoEvento; }
    public void setTipoEvento(String tipoEvento) { this.tipoEvento = tipoEvento; }
    public String getAluno() { return aluno; }
    public void setAluno(String aluno) { this.aluno = aluno; }
    public String getReferencia() { return referencia; }
    public void setReferencia(String referencia) { this.referencia = referencia; }
    public Double getNota() { return nota; }
    public void setNota(Double nota) { this.nota = nota; }
}