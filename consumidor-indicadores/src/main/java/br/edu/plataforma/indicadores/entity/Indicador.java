package br.edu.plataforma.indicadores.entity;

import jakarta.persistence.*;

@Entity
public class Indicador {
    @Id
    private String cursoId;

    private Integer total;

    public Indicador() {}
    public Indicador(String cursoId, Integer total) {
        this.cursoId = cursoId;
        this.total = total;
    }

    public String getCursoId() { return cursoId; }
    public void setCursoId(String cursoId) { this.cursoId = cursoId; }
    public Integer getTotal() { return total; }
    public void setTotal(Integer total) { this.total = total; }
}