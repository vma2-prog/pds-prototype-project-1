package br.com.bestand.ui;

import br.com.bestand.database.DatabaseManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;

import java.sql.*;

public class CategoryView extends VBox {

    private TableView<Category> table = new TableView<>();
    private ObservableList<Category> list = FXCollections.observableArrayList();

    public CategoryView() {
        this.getStyleClass().add("page-panel");
        this.setSpacing(16);
        this.setPadding(new Insets(8, 0, 0, 0));

        HBox header = new HBox(15);
        header.getStyleClass().add("section-bar");
        Label title = new Label("Gerenciamento de Categorias");
        title.getStyleClass().add("page-title");
        title.setStyle("-fx-font-size: 20px;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button btnNova = new Button("+ Nova Categoria");
        btnNova.getStyleClass().add("btn-primary");
        btnNova.setOnAction(e -> showCategoryDialog(null));

        header.getChildren().addAll(title, spacer, btnNova);

        TableColumn<Category, String> colNome = new TableColumn<>("Nome");
        colNome.setCellValueFactory(new PropertyValueFactory<>("nome"));

        TableColumn<Category, String> colDesc = new TableColumn<>("Descrição");
        colDesc.setCellValueFactory(new PropertyValueFactory<>("descricao"));

        TableColumn<Category, Integer> colQtd = new TableColumn<>("Produtos Associados");
        colQtd.setCellValueFactory(new PropertyValueFactory<>("qtdProdutos"));

        TableColumn<Category, Void> colAcoes = new TableColumn<>("Ações");
        colAcoes.setCellFactory(param -> new TableCell<>() {
            private final Button btnDel = new Button("Remover");
            {
                btnDel.getStyleClass().add("btn-danger");
                btnDel.setOnAction(e -> {
                    Category c = getTableView().getItems().get(getIndex());
                    deleteCategory(c);
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) setGraphic(null);
                else setGraphic(btnDel);
            }
        });

        table.getColumns().addAll(colNome, colDesc, colQtd, colAcoes);
        table.setItems(list);
        table.getStyleClass().add("modern-table");
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        carregarCategorias();
        this.getChildren().addAll(header, table);
    }

    private void carregarCategorias() {
        list.clear();
        String sql = """
            SELECT c.id, c.nome, c.descricao, COUNT(p.id) as qtd
            FROM categorias c
            LEFT JOIN produtos p ON p.categoria_id = c.id
            GROUP BY c.id, c.nome, c.descricao
        """;
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new Category(
                    rs.getInt("id"),
                    rs.getString("nome"),
                    rs.getString("descricao"),
                    rs.getInt("qtd")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void showCategoryDialog(Category cat) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Nova Categoria");
        VBox form = new VBox(10);
        form.getStyleClass().add("modal-form");
        form.setPadding(new Insets(20));
        TextField txtNome = new TextField();
        TextArea txtDesc = new TextArea();
        form.getChildren().addAll(new Label("Nome:"), txtNome, new Label("Descrição:"), txtDesc);
        dialog.getDialogPane().getStyleClass().add("dialog-pane");
        dialog.getDialogPane().setContent(form);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.showAndWait().ifPresent(type -> {
            if (type == ButtonType.OK && !txtNome.getText().trim().isEmpty()) {
                try (Connection conn = DatabaseManager.getConnection();
                     PreparedStatement pstmt = conn.prepareStatement("INSERT INTO categorias(nome, descricao) VALUES(?, ?)")) {
                    pstmt.setString(1, txtNome.getText().trim());
                    pstmt.setString(2, txtDesc.getText().trim());
                    pstmt.executeUpdate();
                    carregarCategorias();
                } catch (SQLException ex) {
                    Alert a = new Alert(Alert.AlertType.ERROR, "Categoria duplicada ou inválida.");
                    a.show();
                }
            }
        });
    }

    private void deleteCategory(Category c) {
        if (c.getQtdProdutos() > 0) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Não é possível excluir uma categoria que possui produtos associados. Reassocie os produtos primeiro.");
            alert.show();
            return;
        }
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement("DELETE FROM categorias WHERE id = ?")) {
            pstmt.setInt(1, c.getId());
            pstmt.executeUpdate();
            carregarCategorias();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static class Category {
        private final int id;
        private final String nome;
        private final String descricao;
        private final int qtdProdutos;

        public Category(int id, String nome, String descricao, int qtdProdutos) {
            this.id = id;
            this.nome = nome;
            this.descricao = descricao;
            this.qtdProdutos = qtdProdutos;
        }

        public int getId() { return id; }
        public String getNome() { return nome; }
        public String getDescricao() { return descricao; }
        public int getQtdProdutos() { return qtdProdutos; }
    }
}
