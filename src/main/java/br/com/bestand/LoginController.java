package br.com.bestand;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
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

    // Perfis fictícios para demonstração do protótipo (RF01 / RF02)
    private static final Map<String, String> PERFIS_PROTOTIPO = Map.of(
            "admin", "admin",
            "operador", "1234"
    );

    @FXML
    public void initialize() {
        // Esconde o banner de erro automaticamente assim que o usuário volta a digitar
        txtUsuario.textProperty().addListener((obs, oldVal, newVal) -> ocultarErro());
        txtSenha.textProperty().addListener((obs, oldVal, newVal) -> ocultarErro());
    }

    @FXML
    private void onEntrar(ActionEvent event) {
        String usuario = txtUsuario.getText() != null ? txtUsuario.getText().trim() : "";
        String senha = txtSenha.getText() != null ? txtSenha.getText() : "";

        // 1. Validação de campos obrigatórios
        if (usuario.isEmpty() || senha.isEmpty()) {
            exibirErro("Preencha o perfil de usuário e a senha para continuar.");
            return;
        }

        // 2. Validação contra os perfis simulados do protótipo
        if (!PERFIS_PROTOTIPO.containsKey(usuario) || !PERFIS_PROTOTIPO.get(usuario).equals(senha)) {
            exibirErro("Usuário ou senha inválidos. Verifique os dados e tente novamente.");
            txtSenha.clear();
            return;
        }

        // 3. Autenticação bem-sucedida: transita para a tela principal
        abrirTelaPrincipal(event, usuario);
    }

    private void abrirTelaPrincipal(ActionEvent event, String usuarioLogado) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("main-view.fxml"));
            Parent root = loader.load();

            // Repassa o nome do usuário logado para o MainController, se desejar exibir na topbar
            Object controller = loader.getController();
            if (controller instanceof MainController) {
                MainController mainController = (MainController) controller;
                // Pronto para integrar quando atualizarmos o MainController
                System.out.println("Sessão iniciada como: " + usuarioLogado);
            }

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root, 1100, 700);
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