package br.com.bestand;

public enum Permissao {
    GERENCIAR_PRODUTOS("Gerenciar produtos"),
    REGISTRAR_VENDAS("Registrar vendas"),
    REGISTRAR_DEVOLUCOES("Registrar devoluções"),
    REGISTRAR_CARREGAMENTOS("Registrar carregamentos"),
    CONSULTAR_RECEITAS("Consultar receitas"),
    GERENCIAR_USUARIOS("Gerenciar perfis de usuário");

    private final String descricao;

    Permissao(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
