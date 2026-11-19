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
import java.util.Map;

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
    private SVGPath svgIconeTema;

    // Vetores SVG para Lua (Tema Claro) e Sol (Tema Escuro)
    private static final String ICON_LUA = "M21 12.79A9 9 0 1 1 11.21 3 7 7 0 0 0 21 12.79z";
    private static final String ICON_SOL = "M12 7a5 5 0 1 0 0 10 5 5 0 0 0 0-10zm0-5v2m0 16v2M4.22 4.22l1.42 1.42m12.72 12.72l1.42 1.42M2 12h2m16 0h2M4.22 19.78l1.42-1.42M18.36 5.64l1.42-1.42";

    private static final Map<String, String> PERFIS_PROTOTIPO = Map.of(
            "admin", "admin",
            "operador", "1234"
    );

    @FXML
    public void initialize() {
        txtUsuario.textProperty().addListener((obs, oldVal, newVal) -> ocultarErro());
        txtSenha.textProperty().addListener((obs, oldVal, newVal) -> ocultarErro());

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

        if (!PERFIS_PROTOTIPO.containsKey(usuario) || !PERFIS_PROTOTIPO.get(usuario).equals(senha)) {
            exibirErro("Usuário ou senha inválidos. Verifique os dados e tente novamente.");
            txtSenha.clear();
            return;
        }

        abrirTelaPrincipal(event, usuario);
    }

    private void abrirTelaPrincipal(ActionEvent event, String usuarioLogado) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("main-view.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root, 1100, 700);
            ThemeManager.applyTheme(scene); // Mantém o tema selecionado no login

            stage.setTitle("Bestand - Sistema de Gestão de Estoque (" + usuarioLogado + ")");
            stage.setScene(scene);
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
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