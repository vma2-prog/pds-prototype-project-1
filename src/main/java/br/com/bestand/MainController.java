package br.com.bestand;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
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
    private PerfilUsuario perfilSessao;

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

    public void configurarSessao(PerfilUsuario perfil) {
        if (perfil == null) {
            throw new IllegalArgumentException("O perfil da sessão é obrigatório.");
        }
        perfilSessao = perfil;
        lblUsuarioNome.setText(perfil.getNome());
        String nome = perfil.getNome();
        lblAvatarIniciais.setText(nome.substring(0, Math.min(2, nome.length())).toUpperCase());
        lblUsuarioCargo.setText(nome.equalsIgnoreCase("admin") ? "Administrador" : "Usuário");
        atualizarNavegacaoPorPermissoes();
    }

    // =========================================================================
    // NAVEGAÇÃO SPA
    // =========================================================================


    private void abrirProdutos(String filtroInicial) {
        if (!permitir(Permissao.GERENCIAR_PRODUTOS)) {
            return;
        }
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
        if (!podeAcessarVendas()) {
            return;
        }
        carregarSubView("vendas-view.fxml", btnNavVendas);
    }

    @FXML
    private void onNavProdutos(ActionEvent event) {
        abrirProdutos(null);
    }

    @FXML
    void onNavCarregamentos(ActionEvent event) {
        if (!permitir(Permissao.REGISTRAR_CARREGAMENTOS)) {
            return;
        }
        carregarSubView("carregamentos-view.fxml", btnNavCarregamentos);
    }

    @FXML
    private void onNavReceitas(ActionEvent event) {
        if (!permitir(Permissao.CONSULTAR_RECEITAS)) {
            return;
        }
        carregarSubView("receitas-view.fxml", btnNavReceitas);
    }

    @FXML
    private void onNavConfiguracoes(ActionEvent event) {
        if (!permitir(Permissao.GERENCIAR_USUARIOS)) {
            return;
        }
        carregarSubView("usuarios-view.fxml", btnNavConfiguracoes);
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
            } else if (controller instanceof VendasController vendasController && perfilSessao != null) {
                vendasController.configurarPermissoes(perfilSessao.getPermissoes());
            }

            spaOutlet.getChildren().setAll(view);
            marcarBotaoAtivo(botaoAtivo);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void marcarBotaoAtivo(Button ativo) {
        for (Button btn : navButtons) {
            btn.getStyleClass().remove("nav-btn-active");
        }
        if (ativo != null && !ativo.getStyleClass().contains("nav-btn-active")) {
            ativo.getStyleClass().add("nav-btn-active");
        }
    }

    private void atualizarNavegacaoPorPermissoes() {
        configurarBotao(btnNavProdutos, perfilSessao.temPermissao(Permissao.GERENCIAR_PRODUTOS));
        configurarBotao(btnNavVendas, perfilSessao.temPermissao(Permissao.REGISTRAR_VENDAS)
                || perfilSessao.temPermissao(Permissao.REGISTRAR_DEVOLUCOES));
        configurarBotao(btnNavCarregamentos, perfilSessao.temPermissao(Permissao.REGISTRAR_CARREGAMENTOS));
        configurarBotao(btnNavReceitas, perfilSessao.temPermissao(Permissao.CONSULTAR_RECEITAS));
        configurarBotao(btnNavConfiguracoes, perfilSessao.temPermissao(Permissao.GERENCIAR_USUARIOS));
    }

    private void configurarBotao(Button botao, boolean disponivel) {
        botao.setVisible(disponivel);
        botao.setManaged(disponivel);
    }

    private boolean permitir(Permissao permissao) {
        if (perfilSessao != null && perfilSessao.temPermissao(permissao)) {
            return true;
        }
        mostrarAcessoNegado();
        return false;
    }

    private boolean podeAcessarVendas() {
        if (perfilSessao != null && (perfilSessao.temPermissao(Permissao.REGISTRAR_VENDAS)
                || perfilSessao.temPermissao(Permissao.REGISTRAR_DEVOLUCOES))) {
            return true;
        }
        mostrarAcessoNegado();
        return false;
    }

    private void mostrarAcessoNegado() {
        Alert alerta = new Alert(Alert.AlertType.WARNING);
        alerta.setTitle("Acesso não permitido");
        alerta.setHeaderText(null);
        alerta.setContentText("Seu perfil não tem permissão para acessar esta função.");
        alerta.showAndWait();
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