package br.com.bestand.ui;

import br.com.bestand.database.DatabaseManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;

import java.sql.*;
import java.time.LocalDateTime;

public class SalesView extends VBox {

    private ObservableList<ItemCarrinho> carrinho = FXCollections.observableArrayList();
    private TableView<ItemCarrinho> tableCarrinho = new TableView<>();
    private Label lblTotal = new Label("R$ 0,00");
    private ComboBox<String> comboPagamento = new ComboBox<>();
    private ComboBox<ProductView.Product> comboProdutos = new ComboBox<>();
    private TextField txtQuantidade = new TextField("1");

    public SalesView() {
        this.setSpacing(16);

        Label title = new Label("Registro de Venda");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #ffffff;");

        HBox mainLayout = new HBox(20);

        VBox area1 = new VBox(12);
        area1.getStyleClass().add("card");
        area1.setPrefWidth(320);

        Label lblSec1 = new Label("1. Seleção de Produtos");
        lblSec1.setStyle("-fx-font-weight: bold; -fx-text-fill: #ffffff;");

        comboProdutos.setPromptText("Selecione um produto...");
        comboProdutos.setPrefWidth(Double.MAX_VALUE);
        carregarProdutosCombo();

        Button btnAdd = new Button("Adicionar à Venda");
        btnAdd.getStyleClass().add("btn-primary");
        btnAdd.setOnAction(e -> adicionarItem());

        area1.getChildren().addAll(lblSec1, new Label("Produto:"), comboProdutos, new Label("Quantidade:"), txtQuantidade, btnAdd);

        VBox area2 = new VBox(12);
        area2.getStyleClass().add("card");
        HBox.setHgrow(area2, Priority.ALWAYS);

        Label lblSec2 = new Label("2. Produtos Selecionados");
        lblSec2.setStyle("-fx-font-weight: bold; -fx-text-fill: #ffffff;");

        TableColumn<ItemCarrinho, String> colNome = new TableColumn<>("Produto");
        colNome.setCellValueFactory(new PropertyValueFactory<>("nome"));

        TableColumn<ItemCarrinho, Integer> colQtd = new TableColumn<>("Qtd");
        colQtd.setCellValueFactory(new PropertyValueFactory<>("quantidade"));

        TableColumn<ItemCarrinho, Double> colPreco = new TableColumn<>("Preço Unit.");
        colPreco.setCellValueFactory(new PropertyValueFactory<>("precoUnitario"));

        TableColumn<ItemCarrinho, Double> colSubtotal = new TableColumn<>("Subtotal");
        colSubtotal.setCellValueFactory(new PropertyValueFactory<>("subtotal"));

        tableCarrinho.getColumns().addAll(colNome, colQtd, colPreco, colSubtotal);
        tableCarrinho.setItems(carrinho);

        area2.getChildren().addAll(lblSec2, tableCarrinho);

        VBox area3 = new VBox(16);
        area3.getStyleClass().add("card");
        area3.setPrefWidth(280);

        Label lblSec3 = new Label("3. Resumo da Venda");
        lblSec3.setStyle("-fx-font-weight: bold; -fx-text-fill: #ffffff;");

        lblTotal.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: #7FC7C5;");

        comboPagamento.getItems().addAll("Dinheiro", "Cartão de Crédito", "Cartão de Débito", "PIX");
        comboPagamento.setValue("PIX");
        comboPagamento.setPrefWidth(Double.MAX_VALUE);

        Button btnFinalizar = new Button("Confirmar Venda");
        btnFinalizar.getStyleClass().add("btn-primary");
        btnFinalizar.setMaxWidth(Double.MAX_VALUE);
        btnFinalizar.setOnAction(e -> finalizarVenda());

        area3.getChildren().addAll(lblSec3, new Label("Valor Total:"), lblTotal, new Label("Forma de Pagamento:"), comboPagamento, btnFinalizar);

        mainLayout.getChildren().addAll(area1, area2, area3);
        this.getChildren().addAll(title, mainLayout);
    }

    private void carregarProdutosCombo() {
        ObservableList<ProductView.Product> list = FXCollections.observableArrayList();
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM produtos WHERE estoque > 0")) {
            while (rs.next()) {
                list.add(new ProductView.Product(
                    rs.getInt("id"), rs.getString("codigo"), rs.getString("nome"),
                    rs.getInt("estoque"), rs.getInt("estoque_minimo"),
                    rs.getDouble("custo_atual"), rs.getDouble("preco_venda")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        comboProdutos.setItems(list);
    }

    private void adicionarItem() {
        ProductView.Product p = comboProdutos.getValue();
        if (p == null) return;

        try {
            int qtd = Integer.parseInt(txtQuantidade.getText());
            if (qtd <= 0 || qtd > p.getEstoque()) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Quantidade indisponível em estoque!");
                alert.show();
                return;
            }

            carrinho.add(new ItemCarrinho(p.getId(), p.getNome(), qtd, p.getPrecoVenda(), p.getCustoAtual()));
            atualizarTotal();
        } catch (NumberFormatException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Informe uma quantidade válida.");
            alert.show();
        }
    }

    private void atualizarTotal() {
        double total = carrinho.stream().mapToDouble(ItemCarrinho::getSubtotal).sum();
        lblTotal.setText(String.format("R$ %.2f", total));
    }

    private void finalizarVenda() {
        if (carrinho.isEmpty()) return;

        double total = carrinho.stream().mapToDouble(ItemCarrinho::getSubtotal).sum();
        String pagamento = comboPagamento.getValue();

        try (Connection conn = DatabaseManager.getConnection()) {
            conn.setAutoCommit(false);

            String sqlVenda = "INSERT INTO vendas(data_hora, valor_total, metodo_pagamento, usuario_responsavel) VALUES(?, ?, ?, ?)";
            int vendaId;
            try (PreparedStatement pstmt = conn.prepareStatement(sqlVenda, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setString(1, LocalDateTime.now().toString());
                pstmt.setDouble(2, total);
                pstmt.setString(3, pagamento);
                pstmt.setString(4, "admin");
                pstmt.executeUpdate();
                
                ResultSet rs = pstmt.getGeneratedKeys();
                rs.next();
                vendaId = rs.getInt(1);
            }

            String sqlItem = "INSERT INTO itens_venda(venda_id, produto_id, quantidade, preco_unitario, custo_unitario_historico) VALUES(?, ?, ?, ?, ?)";
            String sqlEstoque = "UPDATE produtos SET estoque = estoque - ? WHERE id = ?";

            try (PreparedStatement pstmtItem = conn.prepareStatement(sqlItem);
                 PreparedStatement pstmtEstoque = conn.prepareStatement(sqlEstoque)) {

                for (ItemCarrinho item : carrinho) {
                    pstmtItem.setInt(1, vendaId);
                    pstmtItem.setInt(2, item.getProdutoId());
                    pstmtItem.setInt(3, item.getQuantidade());
                    pstmtItem.setDouble(4, item.getPrecoUnitario());
                    pstmtItem.setDouble(5, item.getCustoHistorico());
                    pstmtItem.executeUpdate();

                    pstmtEstoque.setInt(1, item.getQuantidade());
                    pstmtEstoque.setInt(2, item.getProdutoId());
                    pstmtEstoque.executeUpdate();
                }
            }

            conn.commit();
            Alert alert = new Alert(Alert.AlertType.INFORMATION, "Venda #" + vendaId + " realizada com sucesso!");
            alert.show();
            carrinho.clear();
            atualizarTotal();
            carregarProdutosCombo();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static class ItemCarrinho {
        private final int produtoId;
        private final String nome;
        private final int quantidade;
        private final double precoUnitario;
        private final double custoHistorico;

        public ItemCarrinho(int produtoId, String nome, int quantidade, double precoUnitario, double custoHistorico) {
            this.produtoId = produtoId;
            this.nome = nome;
            this.quantidade = quantidade;
            this.precoUnitario = precoUnitario;
            this.custoHistorico = custoHistorico;
        }

        public int getProdutoId() { return produtoId; }
        public String getNome() { return nome; }
        public int getQuantidade() { return quantidade; }
        public double getPrecoUnitario() { return precoUnitario; }
        public double getCustoHistorico() { return custoHistorico; }
        public double getSubtotal() { return quantidade * precoUnitario; }
    }
}
