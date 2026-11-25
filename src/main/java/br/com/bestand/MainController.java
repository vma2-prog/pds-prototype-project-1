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
import javafx.scene.control.MenuButton;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.SVGPath;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;

public class MainController {

    @FXML private Button btnNavDashboard;
    @FXML private Button btnNavProdutos;
    @FXML private Button btnNavVendas;
    @FXML private Button btnNavCarregamentos;
    @FXML private Button btnNavReceitas;
    @FXML private Button btnNavConfiguracoes;

    @FXML private TextField txtBuscaGlobal;
    @FXML private Button btnTema;
    @FXML private SVGPath svgIconeTema;

    @FXML private MenuButton menuPerfil;
    @FXML private Label lblAvatarIniciais;
    @FXML private Label lblUsuarioNome;
    @FXML private Label lblUsuarioCargo;

    @FXML private StackPane spaOutlet;

    private List<Button> navButtons;

    private static final String ICON_LUA = "M21 12.79A9 9 0 1 1 11.21 3 7 7 0 0 0 21 12.79z";
    private static final String ICON_SOL = "M12 7a5 5 0 1 0 0 10 5 5 0 0 0 0-10zm0-5v2m0 16v2M4.22 4.22l1.42 1.42m12.72 12.72l1.42 1.42M2 12h2m16 0h2M4.22 19.78l1.42-1.42M18.36 5.64l1.42-1.42";

    @FXML
    public void initialize() {
        navButtons = List.of(
                btnNavDashboard,
                btnNavProdutos,
                btnNavVendas,
                btnNavCarregamentos,
                btnNavReceitas,
                btnNavConfiguracoes
        );

        // Aplica tema atual e ícone correto
        Platform.runLater(() -> {
            Scene scene = btnTema.getScene();
            if (scene != null) {
                ThemeManager.applyTheme(scene);
                atualizarIconeTema();
            }
        });

        // Carrega a Visão Geral (dashboard-view.fxml) como tela inicial da SPA
        carregarSubView("dashboard-view.fxml", btnNavDashboard);
    }

    /**
     * Recebe o nome do usuário vindo do LoginController
     */
    public void configurarSessao(String usuario) {
        if (usuario == null || usuario.isBlank()) return;

        lblUsuarioNome.setText(usuario);
        String iniciais = usuario.length() >= 2
                ? usuario.substring(0, 2).toUpperCase()
                : usuario.toUpperCase();
        lblAvatarIniciais.setText(iniciais);
        lblUsuarioCargo.setText("admin".equalsIgnoreCase(usuario) ? "Administrador" : "Operador");
    }

    // =========================================================================
    // NAVEGAÇÃO SPA
    // =========================================================================


    private void abrirProdutos(String filtroInicial) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("produtos-view.fxml"));
            Node view = loader.load();

            ProdutosController controller = loader.getController();
            if (filtroInicial != null && !filtroInicial.isBlank()) {
                controller.aplicarFiltroInicial(filtroInicial);
            }

            spaOutlet.getChildren().setAll(view);
            marcarBotaoAtivo(btnNavProdutos);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @FXML
    private void onNavDashboard(ActionEvent event) {
        carregarSubView("dashboard-view.fxml", btnNavDashboard);
    }

    @FXML
    void onNavVendas(ActionEvent event) {
        carregarSubView("vendas-view.fxml", btnNavVendas);
    }

    @FXML
    private void onNavProdutos(ActionEvent event) {
        abrirProdutos(null);
    }

    @FXML
    void onNavCarregamentos(ActionEvent event) {
        carregarSubView("carregamentos-view.fxml", btnNavCarregamentos);
    }

    @FXML
    private void onNavReceitas(ActionEvent event) {
        carregarSubView("receitas-view.fxml", btnNavReceitas);
    }

    @FXML
    private void onNavConfiguracoes(ActionEvent event) {
        exibirPlaceholderModulo("Configurações", "Gerenciamento de perfis de usuário e preferências (UC001 – UC003)", btnNavConfiguracoes);
    }

    public void carregarSubView(String fxmlFile, Button botaoAtivo) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Node view = loader.load();

            // Injeta referência do MainController caso seja o DashboardController
            Object controller = loader.getController();
            if (controller instanceof DashboardController) {
                DashboardController dashboardController = (DashboardController) controller;
                dashboardController.setMainController(this);
            }

            spaOutlet.getChildren().setAll(view);
            marcarBotaoAtivo(botaoAtivo);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void exibirPlaceholderModulo(String titulo, String subtitulo, Button botaoAtivo) {
        VBox container = new VBox(20);
        container.getStyleClass().add("content-area");

        VBox header = new VBox(2);
        Label lblTitulo = new Label(titulo);
        lblTitulo.getStyleClass().add("page-title");
        Label lblSub = new Label(subtitulo);
        lblSub.getStyleClass().add("page-subtitle");
        header.getChildren().addAll(lblTitulo, lblSub);

        StackPane card = new StackPane();
        card.getStyleClass().add("placeholder-card");
        Label lblAviso = new Label("Módulo \"" + titulo + "\" pronto para receber seu arquivo .fxml dedicado.");
        lblAviso.getStyleClass().add("page-subtitle");
        card.getChildren().add(lblAviso);

        container.getChildren().addAll(header, card);
        spaOutlet.getChildren().setAll(container);
        marcarBotaoAtivo(botaoAtivo);
    }

    private void marcarBotaoAtivo(Button ativo) {
        for (Button btn : navButtons) {
            btn.getStyleClass().remove("nav-btn-active");
        }
        if (ativo != null && !ativo.getStyleClass().contains("nav-btn-active")) {
            ativo.getStyleClass().add("nav-btn-active");
        }
    }

    // =========================================================================
    // AÇÕES DO CABEÇALHO (TEMA, BUSCA, LOGOUT)
    // =========================================================================
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
    private void onBuscaGlobal(ActionEvent event) {
        String termo = txtBuscaGlobal.getText();
        abrirProdutos(termo);
    }

    @FXML
    private void onSair(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("login-view.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) spaOutlet.getScene().getWindow();
            Scene scene = new Scene(root, 1100, 700);
            ThemeManager.applyTheme(scene);

            stage.setTitle("Bestand - Login");
            stage.setScene(scene);
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}