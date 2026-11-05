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
import java.sql.ResultSet;

public class LoginView extends javafx.scene.layout.StackPane {

    private final Runnable onLoginSuccess;
    private final Runnable onNavigateToRegister;

    public LoginView(Runnable onLoginSuccess, Runnable onNavigateToRegister) {
        this.onLoginSuccess = onLoginSuccess;
        this.onNavigateToRegister = onNavigateToRegister;

        this.getStyleClass().add("root");
        this.setAlignment(Pos.CENTER);

        VBox card = new VBox(18);
        card.getStyleClass().add("card");
        card.setMaxWidth(400);
        card.setPadding(new Insets(32));
        card.setAlignment(Pos.CENTER_LEFT);

        Label logo = new Label("Bestand");
        logo.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: #ffffff;");

        Label subtitle = new Label("Acesse sua conta para gerenciar seu estoque");
        subtitle.setStyle("-fx-font-size: 13px; -fx-text-fill: #a29dbd;");

        VBox header = new VBox(6, logo, subtitle);

        TextField txtUsuario = new TextField();
        txtUsuario.setPromptText("Nome de usuário");
        txtUsuario.setText("admin");

        PasswordField txtSenha = new PasswordField();
        txtSenha.setPromptText("Sua senha");
        txtSenha.setText("admin123");

        Label lblError = new Label();
        lblError.setStyle("-fx-text-fill: #E69A72; -fx-font-size: 12px;");
        lblError.setVisible(false);

        Button btnEntrar = new Button("Entrar no Sistema");
        btnEntrar.getStyleClass().add("btn-primary");
        btnEntrar.setMaxWidth(Double.MAX_VALUE);
        btnEntrar.setOnAction(e -> {
            if (autenticar(txtUsuario.getText().trim(), txtSenha.getText().trim())) {
                onLoginSuccess.run();
            } else {
                lblError.setText("Usuário ou senha incorretos.");
                lblError.setVisible(true);
            }
        });

        HBox footer = new HBox(5);
        footer.setAlignment(Pos.CENTER);
        Label lblCadastro = new Label("Não tem uma conta?");
        lblCadastro.setStyle("-fx-text-fill: #a29dbd;");
        Hyperlink linkCadastro = new Hyperlink("Cadastre-se");
        linkCadastro.setStyle("-fx-text-fill: #7a6fbe; -fx-font-weight: bold;");
        linkCadastro.setOnAction(e -> onNavigateToRegister.run());
        footer.getChildren().addAll(lblCadastro, linkCadastro);

        card.getChildren().addAll(header, new Label("Usuário:"), txtUsuario, new Label("Senha:"), txtSenha, lblError, btnEntrar, footer);
        this.getChildren().add(card);
    }

    private boolean autenticar(String usuario, String senha) {
        String sql = "SELECT * FROM usuarios WHERE nome = ? AND senha = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, usuario);
            pstmt.setString(2, senha);
            ResultSet rs = pstmt.executeQuery();
            return rs.next();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
