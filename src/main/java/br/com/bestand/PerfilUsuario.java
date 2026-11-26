package br.com.bestand;

import java.util.Set;

public final class PerfilUsuario {
    private final String nome;
    private final Set<Permissao> permissoes;

    public PerfilUsuario(String nome, Set<Permissao> permissoes) {
        this.nome = nome;
        this.permissoes = Set.copyOf(permissoes);
    }

    public String getNome() {
        return nome;
    }

    public Set<Permissao> getPermissoes() {
        return permissoes;
    }

    public boolean temPermissao(Permissao permissao) {
        return permissoes.contains(permissao);
    }
}
