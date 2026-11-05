package br.com.bestand.ui;

import br.com.bestand.database.DatabaseManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;

import java.sql.*;

public class ProductView extends VBox {

    private TableView<Product> table = new TableView<>();
    private ObservableList<Product> productList = FXCollections.observableArrayList();
    private TextField searchField = new TextField();

    public ProductView() {
        this.getStyleClass().add("page-panel");
        this.setSpacing(16);
        this.setPadding(new Insets(8, 0, 0, 0));

        HBox header = new HBox(15);
        header.getStyleClass().add("section-bar");
        Label title = new Label("Gerenciamento de Produtos");
        title.getStyleClass().add("page-title");
        title.setStyle("-fx-font-size: 20px;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button btnCadastrar = new Button("+ Cadastrar produto");
        btnCadastrar.getStyleClass().add("btn-primary");
        btnCadastrar.setOnAction(e -> showProductDialog(null));

        header.getChildren().addAll(title, spacer, btnCadastrar);

        HBox filterBar = new HBox(12);
        searchField.setPromptText("Pesquisar por nome ou código...");
        searchField.setPrefWidth(320);
        searchField.getStyleClass().add("search-field");
        searchField.textProperty().addListener((obs, oldVal, newVal) -> loadProducts(newVal));

        filterBar.getChildren().add(searchField);

        setupTable();
        loadProducts("");

        this.getChildren().addAll(header, filterBar, table);
    }

    private void setupTable() {
        TableColumn<Product, String> colCodigo = new TableColumn<>("Código");
        colCodigo.setCellValueFactory(new PropertyValueFactory<>("codigo"));

        TableColumn<Product, String> colNome = new TableColumn<>("Produto");
        colNome.setCellValueFactory(new PropertyValueFactory<>("nome"));

        TableColumn<Product, Integer> colEstoque = new TableColumn<>("Estoque");
        colEstoque.setCellValueFactory(new PropertyValueFactory<>("estoque"));

        TableColumn<Product, Integer> colMin = new TableColumn<>("Estoque Mínimo");
        colMin.setCellValueFactory(new PropertyValueFactory<>("estoqueMinimo"));

        TableColumn<Product, Double> colCusto = new TableColumn<>("Custo (R$)");
        colCusto.setCellValueFactory(new PropertyValueFactory<>("custoAtual"));

        TableColumn<Product, Double> colPreco = new TableColumn<>("Preço (R$)");
        colPreco.setCellValueFactory(new PropertyValueFactory<>("precoVenda"));

        TableColumn<Product, String> colStatus = new TableColumn<>("Status");
        colStatus.setCellValueFactory(cellData -> {
            Product p = cellData.getValue();
            if (p.getEstoque() <= 0) return new javafx.beans.property.SimpleStringProperty("Sem Estoque");
            if (p.getEstoque() <= p.getEstoqueMinimo()) return new javafx.beans.property.SimpleStringProperty("Estoque Baixo");
            return new javafx.beans.property.SimpleStringProperty("Normal");
        });

        TableColumn<Product, Void> colAcoes = new TableColumn<>("Ações");
        colAcoes.setCellFactory(param -> new TableCell<>() {
            private final Button btnEdit = new Button("Editar");
            private final Button btnDel = new Button("Remover");
            {
                btnEdit.getStyleClass().add("btn-secondary");
                btnDel.getStyleClass().add("btn-danger");

                btnEdit.setOnAction(e -> {
                    Product p = getTableView().getItems().get(getIndex());
                    showProductDialog(p);
                });
                btnDel.setOnAction(e -> {
                    Product p = getTableView().getItems().get(getIndex());
                    deleteProduct(p);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) setGraphic(null);
                else setGraphic(new HBox(8, btnEdit, btnDel));
            }
        });

        table.getColumns().addAll(colCodigo, colNome, colEstoque, colMin, colCusto, colPreco, colStatus, colAcoes);
        table.setItems(productList);
        table.setPlaceholder(new Label("Nenhum produto cadastrado."));
        table.getStyleClass().add("modern-table");
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    private void loadProducts(String filter) {
        productList.clear();
        String sql = "SELECT * FROM produtos WHERE nome LIKE ? OR codigo LIKE ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, "%" + filter + "%");
            pstmt.setString(2, "%" + filter + "%");
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                productList.add(new Product(
                    rs.getInt("id"),
                    rs.getString("codigo"),
                    rs.getString("nome"),
                    rs.getInt("estoque"),
                    rs.getInt("estoque_minimo"),
                    rs.getDouble("custo_atual"),
                    rs.getDouble("preco_venda")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void showProductDialog(Product product) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(product == null ? "Cadastrar Produto" : "Editar Produto");

        VBox form = new VBox(10);
        form.getStyleClass().add("modal-form");
        form.setPadding(new Insets(20));

        TextField txtNome = new TextField(product == null ? "" : product.getNome());
        TextField txtCodigo = new TextField(product == null ? "" : product.getCodigo());
        TextField txtPreco = new TextField(product == null ? "" : String.valueOf(product.getPrecoVenda()));
        TextField txtMin = new TextField(product == null ? "" : String.valueOf(product.getEstoqueMinimo()));

        form.getChildren().addAll(
            new Label("Nome do Produto:"), txtNome,
            new Label("Código:"), txtCodigo,
            new Label("Preço de Venda (R$):"), txtPreco,
            new Label("Estoque Mínimo:"), txtMin
        );

        dialog.getDialogPane().getStyleClass().add("dialog-pane");
        dialog.getDialogPane().setContent(form);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.showAndWait().ifPresent(type -> {
            if (type == ButtonType.OK) {
                try {
                    String nome = txtNome.getText().trim();
                    String codigo = txtCodigo.getText().trim();
                    double preco = Double.parseDouble(txtPreco.getText());
                    int min = Integer.parseInt(txtMin.getText());

                    if (nome.isEmpty() || codigo.isEmpty() || preco < 0 || min < 0) {
                        showError("Preencha todos os campos obrigatórios corretamente.");
                        return;
                    }

                    if (product == null) {
                        String sql = "INSERT INTO produtos(codigo, nome, estoque, estoque_minimo, custo_atual, preco_venda) VALUES(?, ?, 0, ?, 0.0, ?)";
                        try (Connection conn = DatabaseManager.getConnection();
                             PreparedStatement pstmt = conn.prepareStatement(sql)) {
                            pstmt.setString(1, codigo);
                            pstmt.setString(2, nome);
                            pstmt.setInt(3, min);
                            pstmt.setDouble(4, preco);
                            pstmt.executeUpdate();
                        }
                    } else {
                        String sql = "UPDATE produtos SET codigo = ?, nome = ?, estoque_minimo = ?, preco_venda = ? WHERE id = ?";
                        try (Connection conn = DatabaseManager.getConnection();
                             PreparedStatement pstmt = conn.prepareStatement(sql)) {
                            pstmt.setString(1, codigo);
                            pstmt.setString(2, nome);
                            pstmt.setInt(3, min);
                            pstmt.setDouble(4, preco);
                            pstmt.setInt(5, product.getId());
                            pstmt.executeUpdate();
                        }
                    }
                    loadProducts("");
                } catch (Exception ex) {
                    showError("Erro ao salvar produto: " + ex.getMessage());
                }
            }
        });
    }

    private void deleteProduct(Product p) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Deseja realmente remover o produto " + p.getNome() + "?", ButtonType.YES, ButtonType.NO);
        alert.showAndWait().ifPresent(type -> {
            if (type == ButtonType.YES) {
                try (Connection conn = DatabaseManager.getConnection();
                     PreparedStatement pstmt = conn.prepareStatement("DELETE FROM produtos WHERE id = ?")) {
                    pstmt.setInt(1, p.getId());
                    pstmt.executeUpdate();
                    loadProducts("");
                } catch (SQLException ex) {
                    showError("Não é possível remover produto associado a vendas/movimentações.");
                }
            }
        });
    }

    private void showError(String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR, msg);
        a.show();
    }

    public static class Product {
        private final int id;
        private final String codigo;
        private final String nome;
        private final int estoque;
        private final int estoqueMinimo;
        private final double custoAtual;
        private final double precoVenda;

        public Product(int id, String codigo, String nome, int estoque, int estoqueMinimo, double custoAtual, double precoVenda) {
            this.id = id;
            this.codigo = codigo;
            this.nome = nome;
            this.estoque = estoque;
            this.estoqueMinimo = estoqueMinimo;
            this.custoAtual = custoAtual;
            this.precoVenda = precoVenda;
        }

        public int getId() { return id; }
        public String getCodigo() { return codigo; }
        public String getNome() { return nome; }
        public int getEstoque() { return estoque; }
        public int getEstoqueMinimo() { return estoqueMinimo; }
        public double getCustoAtual() { return custoAtual; }
        public double getPrecoVenda() { return precoVenda; }

        @Override
        public String toString() {
            return codigo + " - " + nome;
        }
    }
}
