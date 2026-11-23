package br.edu.plataforma.common;

import java.time.Instant;
import java.util.Map;

public class Evento {
    private String eventId;
    private String tipo;
    private Instant ocorridoEm;
    private Map<String, Object> dados;

    public Evento() {}

    public Evento(String eventId, String tipo, Instant ocorridoEm, Map<String, Object> dados) {
        this.eventId = eventId;
        this.tipo = tipo;
        this.ocorridoEm = ocorridoEm;
        this.dados = dados;
    }

    // Getters e Setters (gere com Alt+Insert no IntelliJ)
    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public Instant getOcorridoEm() { return ocorridoEm; }
    public void setOcorridoEm(Instant ocorridoEm) { this.ocorridoEm = ocorridoEm; }
    public Map<String, Object> getDados() { return dados; }
    public void setDados(Map<String, Object> dados) { this.dados = dados; }
}