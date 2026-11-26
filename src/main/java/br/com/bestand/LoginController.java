package br.com.bestand;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.shape.SVGPath;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;

public class LoginController {

    @FXML
    private TextField txtUsuario;

    @FXML
    private PasswordField txtSenha;

    @FXML
    private Label lblErroLogin;

    @FXML
    private Button btnTema;

    @FXML
    private Button btnEntrar;

    @FXML
    private SVGPath svgIconeTema;

    // Vetores SVG para Lua (Tema Claro) e Sol (Tema Escuro)
    private static final String ICON_LUA = "M21 12.79A9 9 0 1 1 11.21 3 7 7 0 0 0 21 12.79z";
    private static final String ICON_SOL = "M12 7a5 5 0 1 0 0 10 5 5 0 0 0 0-10zm0-5v2m0 16v2M4.22 4.22l1.42 1.42m12.72 12.72l1.42 1.42M2 12h2m16 0h2M4.22 19.78l1.42-1.42M18.36 5.64l1.42-1.42";

    private UsuarioService usuarioService;

    @FXML
    public void initialize() {
        txtUsuario.textProperty().addListener((obs, oldVal, newVal) -> ocultarErro());
        txtSenha.textProperty().addListener((obs, oldVal, newVal) -> ocultarErro());

        try {
            usuarioService = new UsuarioService();
        } catch (IllegalStateException e) {
            btnEntrar.setDisable(true);
            exibirErro("Não foi possível inicializar o banco de perfis: " + e.getMessage());
        }

        // Garante que o tema atual e o ícone correto sejam aplicados assim que a Scene estiver pronta
        Platform.runLater(() -> {
            Scene scene = btnTema.getScene();
            if (scene != null) {
                ThemeManager.applyTheme(scene);
                atualizarIconeTema();
            }
        });
    }

    @FXML
    private void onAlternarTema(ActionEvent event) {
        Scene scene = btnTema.getScene();
        if (scene != null) {
            ThemeManager.toggleTheme(scene);
            atualizarIconeTema();
        }
    }

    private void atualizarIconeTema() {
        svgIconeTema.setContent(ThemeManager.isDarkTheme() ? ICON_SOL : ICON_LUA);
    }

    @FXML
    private void onEntrar(ActionEvent event) {
        String usuario = txtUsuario.getText() != null ? txtUsuario.getText().trim() : "";
        String senha = txtSenha.getText() != null ? txtSenha.getText() : "";

        if (usuario.isEmpty() || senha.isEmpty()) {
            exibirErro("Preencha o perfil de usuário e a senha para continuar.");
            return;
        }

        if (usuarioService == null) {
            exibirErro("O serviço de perfis não está disponível. Corrija o banco de dados e reinicie o aplicativo.");
            return;
        }

        char[] senhaDigitada = senha.toCharArray();
        Optional<PerfilUsuario> perfil;
        try {
            perfil = usuarioService.autenticar(usuario, senhaDigitada);
        } catch (IllegalStateException e) {
            exibirErro("Não foi possível autenticar o perfil: " + e.getMessage());
            return;
        } finally {
            Arrays.fill(senhaDigitada, '\0');
            txtSenha.clear();
        }

        if (perfil.isEmpty()) {
            exibirErro("Usuário ou senha inválidos. Verifique os dados e tente novamente.");
            return;
        }

        abrirTelaPrincipal(event, perfil.get());
    }

    private void abrirTelaPrincipal(ActionEvent event, PerfilUsuario perfil) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("main-view.fxml"));
            Parent root = loader.load();
            MainController mainController = loader.getController();
            mainController.configurarSessao(perfil);

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root, 1100, 700);
            ThemeManager.applyTheme(scene);

            stage.setTitle("Bestand - Sistema de Gestão de Estoque (" + perfil.getNome() + ")");
            stage.setScene(scene);
            stage.show();

        } catch (IOException e) {
            exibirErro("Erro interno ao carregar a tela principal.");
        }
    }

    private void exibirErro(String mensagem) {
        lblErroLogin.setText(mensagem);
        lblErroLogin.setVisible(true);
        lblErroLogin.setManaged(true);
    }

    private void ocultarErro() {
        if (lblErroLogin.isVisible()) {
            lblErroLogin.setVisible(false);
            lblErroLogin.setManaged(false);
        }
    }
}