package br.com.bestand;

import br.com.bestand.database.DatabaseManager;
import br.com.bestand.ui.LoginView;
import br.com.bestand.ui.MainLayout;
import br.com.bestand.ui.RegisterView;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class App extends Application {

    private Stage primaryStage;
    private Scene scene;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;

        DatabaseManager.initializeDatabase();
        showLogin();

        primaryStage.setTitle("Bestand - Sistema de Gerenciamento de Estoque");
        primaryStage.setMinWidth(1100);
        primaryStage.setMinHeight(700);
        primaryStage.show();
    }

    public void showLogin() {
        LoginView loginView = new LoginView(
            this::showMainApp,
            this::showRegister
        );
        setSceneContent(loginView);
    }

    public void showRegister() {
        RegisterView registerView = new RegisterView(
            this::showLogin,
            this::showLogin
        );
        setSceneContent(registerView);
    }

    public void showMainApp() {
        MainLayout mainLayout = new MainLayout();
        setSceneContent(mainLayout);
    }

    private void setSceneContent(javafx.scene.Parent rootNode) {
        if (scene == null) {
            scene = new Scene(rootNode, 1280, 800);
            String cssPath = getClass().getResource("/styles.css") != null
                    ? getClass().getResource("/styles.css").toExternalForm()
                    : "file:styles.css";
            scene.getStylesheets().add(cssPath);
            primaryStage.setScene(scene);
        } else {
            scene.setRoot(rootNode);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
