package br.com.bestand.ui;

import br.com.bestand.database.DatabaseManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;

import java.sql.*;

public class SupplierView extends VBox {

    private TableView<Supplier> table = new TableView<>();
    private ObservableList<Supplier> list = FXCollections.observableArrayList();

    public SupplierView() {
        this.getStyleClass().add("page-panel");
        this.setSpacing(16);
        this.setPadding(new Insets(8, 0, 0, 0));

        HBox header = new HBox(15);
        header.getStyleClass().add("section-bar");
        Label title = new Label("Gerenciamento de Fornecedores");
        title.getStyleClass().add("page-title");
        title.setStyle("-fx-font-size: 20px;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button btnNovo = new Button("+ Novo Fornecedor");
        btnNovo.getStyleClass().add("btn-primary");
        btnNovo.setOnAction(e -> showDialog());

        header.getChildren().addAll(title, spacer, btnNovo);

        TableColumn<Supplier, String> colNome = new TableColumn<>("Empresa / Fornecedor");
        colNome.setCellValueFactory(new PropertyValueFactory<>("nome"));

        TableColumn<Supplier, String> colContato = new TableColumn<>("Contato");
        colContato.setCellValueFactory(new PropertyValueFactory<>("contato"));

        TableColumn<Supplier, String> colTel = new TableColumn<>("Telefone");
        colTel.setCellValueFactory(new PropertyValueFactory<>("telefone"));

        TableColumn<Supplier, String> colEmail = new TableColumn<>("E-mail");
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));

        table.getColumns().addAll(colNome, colContato, colTel, colEmail);
        table.setItems(list);
        table.getStyleClass().add("modern-table");
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        carregarFornecedores();
        this.getChildren().addAll(header, table);
    }

    private void carregarFornecedores() {
        list.clear();
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM fornecedores")) {
            while (rs.next()) {
                list.add(new Supplier(
                    rs.getInt("id"),
                    rs.getString("nome"),
                    rs.getString("contato"),
                    rs.getString("telefone"),
                    rs.getString("email")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void showDialog() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Cadastrar Fornecedor");
        VBox form = new VBox(10);
        form.getStyleClass().add("modal-form");
        form.setPadding(new Insets(20));

        TextField txtNome = new TextField();
        TextField txtContato = new TextField();
        TextField txtTel = new TextField();
        TextField txtEmail = new TextField();

        form.getChildren().addAll(
            new Label("Razão Social / Nome:"), txtNome,
            new Label("Pessoa de Contato:"), txtContato,
            new Label("Telefone:"), txtTel,
            new Label("E-mail:"), txtEmail
        );
        dialog.getDialogPane().getStyleClass().add("dialog-pane");
        dialog.getDialogPane().setContent(form);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.showAndWait().ifPresent(type -> {
            if (type == ButtonType.OK && !txtNome.getText().trim().isEmpty()) {
                try (Connection conn = DatabaseManager.getConnection();
                     PreparedStatement pstmt = conn.prepareStatement("INSERT INTO fornecedores(nome, contato, telefone, email) VALUES(?, ?, ?, ?)")) {
                    pstmt.setString(1, txtNome.getText().trim());
                    pstmt.setString(2, txtContato.getText().trim());
                    pstmt.setString(3, txtTel.getText().trim());
                    pstmt.setString(4, txtEmail.getText().trim());
                    pstmt.executeUpdate();
                    carregarFornecedores();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        });
    }

    public static class Supplier {
        private final int id;
        private final String nome;
        private final String contato;
        private final String telefone;
        private final String email;

        public Supplier(int id, String nome, String contato, String telefone, String email) {
            this.id = id;
            this.nome = nome;
            this.contato = contato;
            this.telefone = telefone;
            this.email = email;
        }

        public int getId() { return id; }
        public String getNome() { return nome; }
        public String getContato() { return contato; }
        public String getTelefone() { return telefone; }
        public String getEmail() { return email; }
    }
}
