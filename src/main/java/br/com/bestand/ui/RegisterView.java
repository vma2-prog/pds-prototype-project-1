package br.com.bestand.ui;

import br.com.bestand.database.DatabaseManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.sql.Connection;
import java.sql.PreparedStatement;

public class RegisterView extends javafx.scene.layout.StackPane {

    private final Runnable onRegisterSuccess;
    private final Runnable onNavigateToLogin;

    public RegisterView(Runnable onRegisterSuccess, Runnable onNavigateToLogin) {
        this.onRegisterSuccess = onRegisterSuccess;
        this.onNavigateToLogin = onNavigateToLogin;

        this.getStyleClass().add("root");
        this.setAlignment(Pos.CENTER);

        VBox card = new VBox(16);
        card.getStyleClass().add("card");
        card.setMaxWidth(420);
        card.setPadding(new Insets(32));
        card.setAlignment(Pos.CENTER_LEFT);

        Label logo = new Label("Bestand");
        logo.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: #ffffff;");

        Label subtitle = new Label("Crie sua conta para começar a usar o Bestand");
        subtitle.setStyle("-fx-font-size: 13px; -fx-text-fill: #a29dbd;");

        VBox header = new VBox(6, logo, subtitle);

        TextField txtUsuario = new TextField();
        txtUsuario.setPromptText("Nome de usuário (sem espaços/acentos)");

        PasswordField txtSenha = new PasswordField();
        txtSenha.setPromptText("Senha");

        PasswordField txtConfirmar = new PasswordField();
        txtConfirmar.setPromptText("Confirme a senha");

        Label lblError = new Label();
        lblError.setStyle("-fx-text-fill: #E69A72; -fx-font-size: 12px;");
        lblError.setVisible(false);

        Button btnCadastrar = new Button("Criar Conta");
        btnCadastrar.getStyleClass().add("btn-primary");
        btnCadastrar.setMaxWidth(Double.MAX_VALUE);
        btnCadastrar.setOnAction(e -> {
            String user = txtUsuario.getText().trim();
            String pass = txtSenha.getText().trim();
            String conf = txtConfirmar.getText().trim();

            if (user.isEmpty() || pass.isEmpty()) {
                lblError.setText("Preencha todos os campos obrigatórios.");
                lblError.setVisible(true);
                return;
            }
            if (user.contains(" ") || user.matches(".*[^a-zA-Z0-9].*")) {
                lblError.setText("Nome de usuário inválido (sem espaços ou acentos).");
                lblError.setVisible(true);
                return;
            }
            if (!pass.equals(conf)) {
                lblError.setText("As senhas informadas não coincidem.");
                lblError.setVisible(true);
                return;
            }

            if (cadastrar(user, pass)) {
                onRegisterSuccess.run();
            } else {
                lblError.setText("Nome de usuário já cadastrado.");
                lblError.setVisible(true);
            }
        });

        HBox footer = new HBox(5);
        footer.setAlignment(Pos.CENTER);
        Label lblLogin = new Label("Já possui uma conta?");
        lblLogin.setStyle("-fx-text-fill: #a29dbd;");
        Hyperlink linkLogin = new Hyperlink("Fazer Login");
        linkLogin.setStyle("-fx-text-fill: #7a6fbe; -fx-font-weight: bold;");
        linkLogin.setOnAction(e -> onNavigateToLogin.run());
        footer.getChildren().addAll(lblLogin, linkLogin);

        card.getChildren().addAll(header, new Label("Usuário:"), txtUsuario, new Label("Senha:"), txtSenha, new Label("Confirmar Senha:"), txtConfirmar, lblError, btnCadastrar, footer);
        this.getChildren().add(card);
    }

    private boolean cadastrar(String usuario, String senha) {
        String sql = "INSERT INTO usuarios(nome, senha, permissoes) VALUES(?, ?, 'PRODUTOS,VENDAS,ESTOQUE')";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, usuario);
            pstmt.setString(2, senha);
            pstmt.executeUpdate();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
