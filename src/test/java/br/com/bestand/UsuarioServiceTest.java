package br.com.bestand;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class UsuarioServiceTest {
    private Path diretorioTemporario;
    private Path arquivoBanco;
    private UsuarioService service;

    @Before
    public void configurar() throws IOException {
        diretorioTemporario = Files.createTempDirectory("bestand-usuarios-test");
        arquivoBanco = diretorioTemporario.resolve("perfis.db");
        service = new UsuarioService(arquivoBanco);
    }

    @After
    public void limpar() throws IOException {
        Files.deleteIfExists(arquivoBanco);
        Files.deleteIfExists(diretorioTemporario);
    }

    @Test
    public void perfisPadraoMantemCredenciaisDoPrototipo() {
        Optional<PerfilUsuario> admin = service.autenticar("admin", "admin".toCharArray());
        Optional<PerfilUsuario> operador = service.autenticar("operador", "1234".toCharArray());

        assertTrue(admin.isPresent());
        assertTrue(admin.get().temPermissao(Permissao.GERENCIAR_USUARIOS));
        assertTrue(operador.isPresent());
        assertFalse(operador.get().temPermissao(Permissao.GERENCIAR_USUARIOS));
    }

    @Test
    public void cadastroAlteracaoELoginPersistemNoBanco() {
        service.cadastrar("estoquista", "senha1".toCharArray(),
                EnumSet.of(Permissao.GERENCIAR_PRODUTOS));
        service.atualizar("estoquista", "senha2".toCharArray(),
                EnumSet.of(Permissao.GERENCIAR_PRODUTOS, Permissao.CONSULTAR_RECEITAS));

        UsuarioService serviceReaberto = new UsuarioService(arquivoBanco);
        Optional<PerfilUsuario> perfil = serviceReaberto.autenticar("ESTOQUISTA", "senha2".toCharArray());

        assertTrue(perfil.isPresent());
        assertTrue(perfil.get().temPermissao(Permissao.CONSULTAR_RECEITAS));
        assertFalse(serviceReaberto.autenticar("estoquista", "senha1".toCharArray()).isPresent());
    }

    @Test
    public void protegeAdminERejeitaNomesInvalidosEDuplicados() {
        assertFalha(() -> service.remover("admin"));
        assertFalha(() -> service.atualizar("admin", new char[0],
                EnumSet.allOf(Permissao.class)));
        assertFalha(() -> service.cadastrar("nome inválido", "senha".toCharArray(),
                Set.of(Permissao.GERENCIAR_PRODUTOS)));
        service.cadastrar("operador2", "senha".toCharArray(), Set.of(Permissao.GERENCIAR_PRODUTOS));
        assertFalha(() -> service.cadastrar("OPERADOR2", "outrasenha".toCharArray(),
                Set.of(Permissao.GERENCIAR_PRODUTOS)));
    }

    @Test
    public void removerPerfilImpedeAutenticacao() {
        service.cadastrar("temporario", "senha".toCharArray(), Set.of(Permissao.CONSULTAR_RECEITAS));
        service.remover("temporario");

        assertFalse(service.autenticar("temporario", "senha".toCharArray()).isPresent());
        assertEquals(2, service.listarPerfis().size());
    }

    @Test
    public void perfilPadraoRemovidoNaoERecriadoAoReabrirBanco() {
        service.remover("operador");
        UsuarioService serviceReaberto = new UsuarioService(arquivoBanco);

        assertFalse(serviceReaberto.autenticar("operador", "1234".toCharArray()).isPresent());
        assertEquals(1, serviceReaberto.listarPerfis().size());
    }

    private void assertFalha(Runnable operacao) {
        try {
            operacao.run();
            fail("A operação deveria ter sido rejeitada.");
        } catch (IllegalArgumentException esperado) {
            assertTrue(esperado.getMessage() != null && !esperado.getMessage().isBlank());
        }
    }
}
