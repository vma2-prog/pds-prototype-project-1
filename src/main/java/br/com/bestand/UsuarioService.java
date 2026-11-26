package br.com.bestand;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

public final class UsuarioService {
    private static final int ITERACOES_HASH = 120_000;
    private static final int TAMANHO_HASH_BITS = 256;
    private static final int TAMANHO_SALT = 16;
    private static final Pattern NOME_VALIDO = Pattern.compile("[A-Za-z0-9_-]{1,32}");
    private static final SecureRandom RANDOM = new SecureRandom();

    private final UsuarioRepository repository;

    public UsuarioService() {
        this(new Database(Path.of(System.getProperty("user.home"), ".bestand", "perfis.db")));
    }

    public UsuarioService(Path databaseFile) {
        this(new Database(databaseFile));
    }

    public UsuarioService(Database database) {
        repository = new UsuarioRepository(database);
        inicializarPerfisPadrao();
    }

    public Optional<PerfilUsuario> autenticar(String nome, char[] senha) {
        if (nome == null || senha == null) {
            return Optional.empty();
        }
        Optional<UsuarioRepository.Credencial> credencial = repository.buscarCredencial(nome.trim());
        if (credencial.isEmpty()) {
            return Optional.empty();
        }

        UsuarioRepository.Credencial dados = credencial.get();
        byte[] hashInformado = gerarHash(senha, Base64.getDecoder().decode(dados.salt()));
        byte[] hashArmazenado = Base64.getDecoder().decode(dados.senhaHash());
        return MessageDigest.isEqual(hashArmazenado, hashInformado)
                ? Optional.of(dados.perfil())
                : Optional.empty();
    }

    public List<PerfilUsuario> listarPerfis() {
        return repository.listar();
    }

    public PerfilUsuario cadastrar(String nome, char[] senha, Set<Permissao> permissoes) {
        String nomeNormalizado = validarNome(nome);
        validarSenha(senha);
        Set<Permissao> permissoesValidas = validarPermissoes(permissoes);
        if (repository.existe(nomeNormalizado)) {
            throw new IllegalArgumentException("Já existe um perfil com esse nome.");
        }

        byte[] salt = gerarSalt();
        repository.inserir(nomeNormalizado, codificar(gerarHash(senha, salt)), codificar(salt), permissoesValidas);
        return repository.buscarCredencial(nomeNormalizado).orElseThrow().perfil();
    }

    public PerfilUsuario atualizar(String nome, char[] novaSenha, Set<Permissao> permissoes) {
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("Selecione um perfil para alterar.");
        }
        if (nome.equalsIgnoreCase("admin")) {
            throw new IllegalArgumentException("O perfil admin não pode ser alterado.");
        }
        Set<Permissao> permissoesValidas = validarPermissoes(permissoes);
        String hash = null;
        String saltCodificado = null;
        if (novaSenha != null && novaSenha.length > 0) {
            validarSenha(novaSenha);
            byte[] salt = gerarSalt();
            hash = codificar(gerarHash(novaSenha, salt));
            saltCodificado = codificar(salt);
        }

        repository.atualizar(nome, hash, saltCodificado, permissoesValidas);
        return repository.buscarCredencial(nome).orElseThrow().perfil();
    }

    public void remover(String nome) {
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("Selecione um perfil para remover.");
        }
        if (nome.equalsIgnoreCase("admin")) {
            throw new IllegalArgumentException("O perfil admin não pode ser removido.");
        }
        repository.remover(nome);
    }

    private void inicializarPerfisPadrao() {
        List<PerfilUsuario> perfisExistentes = repository.listar();
        if (perfisExistentes.isEmpty()) {
            criarPerfilPadrao("admin", "admin".toCharArray(), EnumSet.allOf(Permissao.class));
            EnumSet<Permissao> permissoesOperador = EnumSet.allOf(Permissao.class);
            permissoesOperador.remove(Permissao.GERENCIAR_USUARIOS);
            criarPerfilPadrao("operador", "1234".toCharArray(), permissoesOperador);
        } else if (!repository.existe("admin")) {
            criarPerfilPadrao("admin", "admin".toCharArray(), EnumSet.allOf(Permissao.class));
        }
    }

    private void criarPerfilPadrao(String nome, char[] senha, Set<Permissao> permissoes) {
        byte[] salt = gerarSalt();
        repository.inserir(nome, codificar(gerarHash(senha, salt)), codificar(salt), permissoes);
        Arrays.fill(senha, '\0');
    }

    private String validarNome(String nome) {
        String normalizado = nome == null ? "" : nome.trim();
        if (!NOME_VALIDO.matcher(normalizado).matches()) {
            throw new IllegalArgumentException(
                    "O nome deve ter de 1 a 32 caracteres: letras sem acento, números, hífen ou sublinhado.");
        }
        return normalizado;
    }

    private void validarSenha(char[] senha) {
        if (senha == null || senha.length == 0) {
            throw new IllegalArgumentException("A senha é obrigatória.");
        }
    }

    private Set<Permissao> validarPermissoes(Set<Permissao> permissoes) {
        if (permissoes == null || permissoes.isEmpty()) {
            throw new IllegalArgumentException("Selecione pelo menos uma permissão.");
        }
        return EnumSet.copyOf(permissoes);
    }

    private byte[] gerarSalt() {
        byte[] salt = new byte[TAMANHO_SALT];
        RANDOM.nextBytes(salt);
        return salt;
    }

    private byte[] gerarHash(char[] senha, byte[] salt) {
        PBEKeySpec spec = new PBEKeySpec(senha, salt, ITERACOES_HASH, TAMANHO_HASH_BITS);
        try {
            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Não foi possível proteger a senha.", e);
        } finally {
            spec.clearPassword();
        }
    }

    private String codificar(byte[] valor) {
        return Base64.getEncoder().encodeToString(valor);
    }
}
