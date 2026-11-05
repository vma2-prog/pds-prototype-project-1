package br.com.bestand.ui;

import br.com.bestand.database.DatabaseManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;

import java.sql.*;

public class UserView extends VBox {

    private TableView<Usuario> table = new TableView<>();
    private ObservableList<Usuario> list = FXCollections.observableArrayList();

    public UserView() {
        this.getStyleClass().add("page-panel");
        this.setSpacing(16);
        this.setPadding(new Insets(8, 0, 0, 0));

        HBox header = new HBox(15);
        header.getStyleClass().add("section-bar");
        Label title = new Label("Usuários e Permissões");
        title.getStyleClass().add("page-title");
        title.setStyle("-fx-font-size: 20px;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button btnNovo = new Button("+ Novo Usuário");
        btnNovo.getStyleClass().add("btn-primary");
        btnNovo.setOnAction(e -> showModalUser());

        header.getChildren().addAll(title, spacer, btnNovo);

        TableColumn<Usuario, Integer> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));

        TableColumn<Usuario, String> colNome = new TableColumn<>("Nome do Usuário");
        colNome.setCellValueFactory(new PropertyValueFactory<>("nome"));

        TableColumn<Usuario, String> colPerm = new TableColumn<>("Permissões");
        colPerm.setCellValueFactory(new PropertyValueFactory<>("permissoes"));

        TableColumn<Usuario, Void> colAcoes = new TableColumn<>("Ações");
        colAcoes.setCellFactory(param -> new TableCell<>() {
            private final Button btnDel = new Button("Remover");
            {
                btnDel.getStyleClass().add("btn-danger");
                btnDel.setOnAction(e -> {
                    int index = getIndex();
                    if (index >= 0 && index < getTableView().getItems().size()) {
                        Usuario u = getTableView().getItems().get(index);
                        deleteUser(u);
                    }
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                int index = getIndex();
                if (empty || index < 0 || index >= getTableView().getItems().size()) {
                    setGraphic(null);
                    return;
                }
                Usuario u = getTableView().getItems().get(index);
                if (u == null || "admin".equalsIgnoreCase(u.getNome())) {
                    setGraphic(null);
                } else {
                    setGraphic(btnDel);
                }
            }
        });

        table.getColumns().addAll(colId, colNome, colPerm, colAcoes);
        table.setItems(list);
        table.getStyleClass().add("modern-table");
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        carregarUsuarios();
        this.getChildren().addAll(header, table);
    }

    private void carregarUsuarios() {
        list.clear();
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM usuarios")) {
            while (rs.next()) {
                list.add(new Usuario(
                    rs.getInt("id"),
                    rs.getString("nome"),
                    rs.getString("permissoes")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void showModalUser() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Cadastrar Perfil de Usuário");

        VBox form = new VBox(10);
        form.getStyleClass().add("modal-form");
        form.setPadding(new Insets(20));

        TextField txtNome = new TextField();
        PasswordField txtSenha = new PasswordField();
        CheckBox cbVendas = new CheckBox("Acesso a Vendas & Operações");
        CheckBox cbEstoque = new CheckBox("Gerenciamento de Estoque");

        form.getChildren().addAll(
            new Label("Nome de Usuário (Sem espaços ou caracteres especiais):"), txtNome,
            new Label("Senha:"), txtSenha,
            new Label("Permissões:"), cbVendas, cbEstoque
        );

        dialog.getDialogPane().getStyleClass().add("dialog-pane");
        dialog.getDialogPane().setContent(form);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.showAndWait().ifPresent(type -> {
            if (type == ButtonType.OK) {
                String nome = txtNome.getText().trim();
                String senha = txtSenha.getText().trim();

                if (nome.contains(" ") || nome.matches(".*[^a-zA-Z0-9].*")) {
                    Alert a = new Alert(Alert.AlertType.ERROR, "Nome de perfil contém espaços ou caracteres inválidos.");
                    a.show();
                    return;
                }

                if (nome.isEmpty() || senha.isEmpty()) {
                    Alert a = new Alert(Alert.AlertType.ERROR, "Preencha os campos obrigatórios.");
                    a.show();
                    return;
                }

                try (Connection conn = DatabaseManager.getConnection();
                     PreparedStatement pstmt = conn.prepareStatement("INSERT INTO usuarios(nome, senha, permissoes) VALUES(?, ?, ?)")) {
                    pstmt.setString(1, nome);
                    pstmt.setString(2, senha);
                    pstmt.setString(3, "PRODUTOS,VENDAS");
                    pstmt.executeUpdate();
                    carregarUsuarios();
                } catch (SQLException ex) {
                    Alert a = new Alert(Alert.AlertType.ERROR, "Nome de usuário já cadastrado.");
                    a.show();
                }
            }
        });
    }

    private void deleteUser(Usuario u) {
        if ("admin".equalsIgnoreCase(u.getNome())) {
            Alert a = new Alert(Alert.AlertType.WARNING, "O perfil 'admin' não pode ser removido.");
            a.show();
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Deseja remover o perfil " + u.getNome() + "?", ButtonType.YES, ButtonType.NO);
        alert.showAndWait().ifPresent(type -> {
            if (type == ButtonType.YES) {
                try (Connection conn = DatabaseManager.getConnection();
                     PreparedStatement pstmt = conn.prepareStatement("DELETE FROM usuarios WHERE id = ?")) {
                    pstmt.setInt(1, u.getId());
                    pstmt.executeUpdate();
                    carregarUsuarios();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public static class Usuario {
        private final int id;
        private final String nome;
        private final String permissoes;

        public Usuario(int id, String nome, String permissoes) {
            this.id = id;
            this.nome = nome;
            this.permissoes = permissoes;
        }

        public int getId() { return id; }
        public String getNome() { return nome; }
        public String getPermissoes() { return permissoes; }
    }
}
