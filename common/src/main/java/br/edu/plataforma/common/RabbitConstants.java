package br.edu.plataforma.common;

public final class RabbitConstants {
    public static final String EXCHANGE = "plataforma.cursos";

    public static final String RK_CURSO_CONCLUIDO = "curso.concluido";
    public static final String RK_CURSO_ABANDONADO = "curso.abandonado";
    public static final String RK_AVALIACAO_ENVIADA = "avaliacao.enviada";
    public static final String RK_AVALIACAO_REVISADA = "avaliacao.revisada";

    public static final String Q_CERTIFICACAO = "q.certificacao";
    public static final String Q_COMUNICACAO = "q.comunicacao";
    public static final String Q_ACOMPANHAMENTO = "q.acompanhamento";
    public static final String Q_AUDITORIA = "q.auditoria";
    public static final String Q_INDICADORES = "q.indicadores";
}