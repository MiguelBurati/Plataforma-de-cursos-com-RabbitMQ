package br.edu.plataforma.auditoria.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
public class Auditoria {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String eventoId;

    private String tipo;

    private Instant ocorridoEm;
    private Instant recebidoEm;

    @Column(columnDefinition = "TEXT")
    private String payloadJson; // O JSON inteiro do evento

    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getEventoId() { return eventoId; }
    public void setEventoId(String eventoId) { this.eventoId = eventoId; }
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public Instant getOcorridoEm() { return ocorridoEm; }
    public void setOcorridoEm(Instant ocorridoEm) { this.ocorridoEm = ocorridoEm; }
    public Instant getRecebidoEm() { return recebidoEm; }
    public void setRecebidoEm(Instant recebidoEm) { this.recebidoEm = recebidoEm; }
    public String getPayloadJson() { return payloadJson; }
    public void setPayloadJson(String payloadJson) { this.payloadJson = payloadJson; }
}