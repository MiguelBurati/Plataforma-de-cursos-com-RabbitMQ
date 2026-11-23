package br.edu.plataforma.certificacao.entity;

import jakarta.persistence.*;

@Entity
public class Certificado {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String eventoId; // Garante que o mesmo evento não seja salvo duas vezes

    private String aluno;
    private String curso;
    private Integer cargaHoraria;

    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getEventoId() { return eventoId; }
    public void setEventoId(String eventoId) { this.eventoId = eventoId; }
    public String getAluno() { return aluno; }
    public void setAluno(String aluno) { this.aluno = aluno; }
    public String getCurso() { return curso; }
    public void setCurso(String curso) { this.curso = curso; }
    public Integer getCargaHoraria() { return cargaHoraria; }
    public void setCargaHoraria(Integer cargaHoraria) { this.cargaHoraria = cargaHoraria; }
}