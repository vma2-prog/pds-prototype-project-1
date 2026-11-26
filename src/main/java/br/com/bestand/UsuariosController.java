package br.com.bestand;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ListView;
import javafx.scene.control.ListCell;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class UsuariosController {
    @FXML private ListView<PerfilUsuario> listaPerfis;
    @FXML private TextField txtNome;
    @FXML private PasswordField txtSenha;
    @FXML private GridPane boxPermissoes;
    @FXML private Button btnNovo;
    @FXML private Button btnRemover;
    @FXML private Button btnSalvar;

    private UsuarioService usuarioService;
    private final ObservableList<PerfilUsuario> perfis = FXCollections.observableArrayList();
    private final Map<Permissao, CheckBox> caixasPermissao = new EnumMap<>(Permissao.class);
    private PerfilUsuario perfilSelecionado;
    private PerfilUsuario usuarioLogado;

    private static final double LARGURA_MINIMA_COLUNA = 210;
    private static final double ESPACAMENTO_COLUNAS = 16;

    public void configurarSessao(PerfilUsuario usuarioLogado) {
        this.usuarioLogado = usuarioLogado;
        if (perfilSelecionado != null) {
            configurarFormulario(perfilSelecionado);
        }
    }

    @FXML
    public void initialize() {
        try {
            usuarioService = new UsuarioService();
        } catch (IllegalStateException e) {
            mostrarMensagem(Alert.AlertType.ERROR, "Banco de perfis indisponível", e.getMessage());
            listaPerfis.setDisable(true);
            txtNome.setDisable(true);
            txtSenha.setDisable(true);
            boxPermissoes.setDisable(true);
            btnNovo.setDisable(true);
            btnSalvar.setDisable(true);
            btnRemover.setDisable(true);
        }
        listaPerfis.setItems(perfis);
        listaPerfis.setCellFactory(view -> new ListCell<PerfilUsuario>() {
            @Override
            protected void updateItem(PerfilUsuario perfil, boolean empty) {
                super.updateItem(perfil, empty);
                setText(empty || perfil == null ? null : perfil.getNome());
            }
        });
        for (Permissao permissao : Permissao.values()) {
            CheckBox checkBox = new CheckBox(permissao.getDescricao());
            checkBox.getStyleClass().add("permission-checkbox");
            checkBox.setWrapText(true);
            checkBox.setMaxWidth(Double.MAX_VALUE);
            caixasPermissao.put(permissao, checkBox);
        }
        boxPermissoes.widthProperty().addListener((observable, anterior, largura) ->
                atualizarGradePermissoes(largura.doubleValue()));
        atualizarGradePermissoes(boxPermissoes.getWidth());
        listaPerfis.getSelectionModel().selectedItemProperty().addListener(
                (observable, anterior, selecionado) -> selecionarPerfil(selecionado));
        if (usuarioService != null) {
            atualizarLista(null);
        }
    }

    @FXML
    private void onNovo(ActionEvent event) {
        if (usuarioService == null) {
            return;
        }
        listaPerfis.getSelectionModel().clearSelection();
        selecionarPerfil(null);
    }

    @FXML
    private void onCancelar(ActionEvent event) {
        if (perfilSelecionado == null) {
            listaPerfis.getSelectionModel().clearSelection();
        } else {
            selecionarPerfil(perfilSelecionado);
        }
    }

    @FXML
    private void onSalvar(ActionEvent event) {
        if (usuarioService == null) {
            return;
        }
        char[] senha = txtSenha.getText().toCharArray();
        try {
            Set<Permissao> permissoes = obterPermissoesSelecionadas();
            PerfilUsuario salvo;
            if (perfilSelecionado == null) {
                salvo = usuarioService.cadastrar(txtNome.getText(), senha, permissoes);
            } else if (perfilSelecionado.getNome().equalsIgnoreCase("admin")) {
                salvo = usuarioService.alterarSenhaPropria(
                        usuarioLogado == null ? null : usuarioLogado.getNome(),
                        perfilSelecionado.getNome(),
                        senha
                );
            } else {
                salvo = usuarioService.atualizar(perfilSelecionado.getNome(), senha, permissoes);
            }
            String nomeSalvo = salvo.getNome();
            atualizarLista(nomeSalvo);
            mostrarMensagem(Alert.AlertType.INFORMATION, "Perfil salvo",
                    nomeSalvo.equalsIgnoreCase("admin")
                            ? "Sua senha foi alterada com sucesso."
                            : "As informações do perfil foram salvas com sucesso.");
        } catch (IllegalArgumentException | IllegalStateException e) {
            mostrarMensagem(Alert.AlertType.WARNING, "Não foi possível salvar o perfil", e.getMessage());
        } finally {
            java.util.Arrays.fill(senha, '\0');
            txtSenha.clear();
        }
    }

    @FXML
    private void onRemover(ActionEvent event) {
        if (usuarioService == null) {
            return;
        }
        if (perfilSelecionado == null) {
            mostrarMensagem(Alert.AlertType.WARNING, "Nenhum perfil selecionado",
                    "Selecione um perfil antes de removê-lo.");
            return;
        }
        Alert confirmacao = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacao.setTitle("Remover perfil");
        confirmacao.setHeaderText("Remover o perfil \"" + perfilSelecionado.getNome() + "\"?");
        confirmacao.setContentText("Esta ação não pode ser desfeita.");
        Optional<ButtonType> resposta = confirmacao.showAndWait();
        if (resposta.isEmpty() || resposta.get() != ButtonType.OK) {
            return;
        }

        try {
            usuarioService.remover(perfilSelecionado.getNome());
            atualizarLista(null);
            mostrarMensagem(Alert.AlertType.INFORMATION, "Perfil removido",
                    "O perfil foi removido com sucesso.");
        } catch (IllegalArgumentException | IllegalStateException e) {
            mostrarMensagem(Alert.AlertType.WARNING, "Não foi possível remover o perfil", e.getMessage());
        }
    }

    private void selecionarPerfil(PerfilUsuario perfil) {
        perfilSelecionado = perfil;
        configurarFormulario(perfil);
    }

    private void configurarFormulario(PerfilUsuario perfil) {
        boolean novoPerfil = perfil == null;
        boolean perfilAdmin = perfil != null && perfil.getNome().equalsIgnoreCase("admin");
        boolean adminAlterandoPropriaSenha = perfilAdmin && usuarioLogado != null
                && usuarioLogado.getNome().equalsIgnoreCase(perfil.getNome());
        txtNome.setText(novoPerfil ? "" : perfil.getNome());
        txtNome.setDisable(!novoPerfil);
        txtSenha.clear();
        txtSenha.setDisable(perfilAdmin && !adminAlterandoPropriaSenha);
        txtSenha.setPromptText(novoPerfil
                ? "Senha obrigatória"
                : adminAlterandoPropriaSenha
                ? "Informe sua nova senha"
                : "Deixe vazio para manter a senha atual");
        for (Permissao permissao : Permissao.values()) {
            CheckBox checkBox = caixasPermissao.get(permissao);
            checkBox.setSelected(perfil != null && perfil.temPermissao(permissao));
            checkBox.setDisable(perfilAdmin);
        }
        btnSalvar.setText(adminAlterandoPropriaSenha ? "Alterar minha senha" : "Salvar perfil");
        btnSalvar.setDisable(perfilAdmin && !adminAlterandoPropriaSenha);
        btnRemover.setDisable(novoPerfil || perfilAdmin);
    }

    private void atualizarGradePermissoes(double largura) {
        if (largura <= 0) {
            return;
        }
        int colunas = Math.max(1, (int) Math.floor(
                (largura + ESPACAMENTO_COLUNAS) / (LARGURA_MINIMA_COLUNA + ESPACAMENTO_COLUNAS)));
        boxPermissoes.getChildren().clear();
        boxPermissoes.getColumnConstraints().clear();
        double larguraPercentual = 100.0 / colunas;
        for (int coluna = 0; coluna < colunas; coluna++) {
            ColumnConstraints constraints = new ColumnConstraints();
            constraints.setPercentWidth(larguraPercentual);
            constraints.setHgrow(Priority.ALWAYS);
            constraints.setFillWidth(true);
            boxPermissoes.getColumnConstraints().add(constraints);
        }
        int indice = 0;
        for (CheckBox checkBox : caixasPermissao.values()) {
            boxPermissoes.add(checkBox, indice % colunas, indice / colunas);
            indice++;
        }
    }

    private Set<Permissao> obterPermissoesSelecionadas() {
        EnumSet<Permissao> selecionadas = EnumSet.noneOf(Permissao.class);
        for (Map.Entry<Permissao, CheckBox> entry : caixasPermissao.entrySet()) {
            if (entry.getValue().isSelected()) {
                selecionadas.add(entry.getKey());
            }
        }
        return selecionadas;
    }

    private void atualizarLista(String nomeSelecionado) {
        if (usuarioService == null) {
            return;
        }
        List<PerfilUsuario> carregados = usuarioService.listarPerfis();
        perfis.setAll(carregados);
        if (nomeSelecionado == null) {
            listaPerfis.getSelectionModel().clearSelection();
            selecionarPerfil(null);
            return;
        }
        perfis.stream()
                .filter(perfil -> perfil.getNome().equalsIgnoreCase(nomeSelecionado))
                .findFirst()
                .ifPresent(perfil -> listaPerfis.getSelectionModel().select(perfil));
    }

    private void mostrarMensagem(Alert.AlertType tipo, String titulo, String mensagem) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensagem == null ? "Ocorreu um erro inesperado." : mensagem);
        alerta.showAndWait();
    }
}
