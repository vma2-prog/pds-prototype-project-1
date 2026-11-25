package br.com.bestand;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

import java.text.NumberFormat;
import java.util.Locale;

public class VendasController {

    @FXML private ComboBox<LojaService.Produto> cmbProduto;
    @FXML private ComboBox<LojaService.Venda> cmbVendaDevolucao;
    @FXML private TextField txtQuantidadeVenda;
    @FXML private TextField txtQuantidadeDevolucao;
    @FXML private Label lblQtdDisponivel;

    @FXML private TableView<LojaService.Venda> tblVendas;
    @FXML private TableColumn<LojaService.Venda, String> colDataVenda;
    @FXML private TableColumn<LojaService.Venda, String> colIdVenda;
    @FXML private TableColumn<LojaService.Venda, String> colProdutoVenda;
    @FXML private TableColumn<LojaService.Venda, Number> colQuantidadeVenda;
    @FXML private TableColumn<LojaService.Venda, String> colTotalVenda;
    @FXML private TableColumn<LojaService.Venda, String> colStatusVenda;

    private final LojaService loja = LojaService.getInstance();
    private FilteredList<LojaService.Venda> vendasDisponiveis;
    private final NumberFormat moeda = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("pt-BR"));

    @FXML
    public void initialize() {
        configurarProdutos();
        configurarDevolucoes();
        configurarTabela();
        tblVendas.setItems(loja.getVendas());
    }

    private void configurarProdutos() {
        cmbProduto.setItems(loja.getProdutos());
        cmbProduto.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(LojaService.Produto produto, boolean empty) {
                super.updateItem(produto, empty);
                setText(empty || produto == null ? null
                        : produto.getNome() + " · " + moeda.format(produto.getPrecoVenda()));
            }
        });
        cmbProduto.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(LojaService.Produto produto, boolean empty) {
                super.updateItem(produto, empty);
                setText(empty || produto == null ? null
                        : produto.getNome() + " · " + moeda.format(produto.getPrecoVenda()));
            }
        });
    }

    private void configurarDevolucoes() {
        vendasDisponiveis = new FilteredList<>(loja.getVendas(),
                venda -> venda.getQuantidadeDisponivelParaDevolucao() > 0);
        cmbVendaDevolucao.setItems(vendasDisponiveis);
        cmbVendaDevolucao.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(LojaService.Venda venda, boolean empty) {
                super.updateItem(venda, empty);
                setText(empty || venda == null ? null
                        : venda.getId() + " · " + venda.getProduto().getNome()
                        + " · até " + venda.getQuantidadeDisponivelParaDevolucao() + " un.");
            }
        });
        cmbVendaDevolucao.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(LojaService.Venda venda, boolean empty) {
                super.updateItem(venda, empty);
                setText(empty || venda == null ? null
                        : venda.getId() + " · " + venda.getProduto().getNome()
                        + " · até " + venda.getQuantidadeDisponivelParaDevolucao() + " un.");
            }
        });
        cmbVendaDevolucao.getSelectionModel().selectedItemProperty().addListener(
                (observable, anterior, venda) -> atualizarQuantidadeDisponivel(venda)
        );
    }

    private void configurarTabela() {
        colDataVenda.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getDataFormatada()));
        colIdVenda.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getId()));
        colProdutoVenda.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getProduto().getNome()));
        colQuantidadeVenda.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getQuantidade()));
        colTotalVenda.setCellValueFactory(data -> new SimpleStringProperty(moeda.format(data.getValue().getTotal())));
        colStatusVenda.setCellValueFactory(data -> data.getValue().situacaoProperty());
    }

    private void atualizarQuantidadeDisponivel(LojaService.Venda venda) {
        lblQtdDisponivel.setText(venda == null ? "Disponível: —"
                : "Disponível: " + venda.getQuantidadeDisponivelParaDevolucao() + " un.");
    }

    @FXML
    private void onRegistrarVenda(ActionEvent event) {
        try {
            LojaService.Produto produto = cmbProduto.getValue();
            int quantidade = Integer.parseInt(txtQuantidadeVenda.getText().trim());
            LojaService.Venda venda = loja.registrarVenda(produto, quantidade);
            tblVendas.refresh();
            txtQuantidadeVenda.clear();
            mostrarMensagem(Alert.AlertType.INFORMATION, "Venda registrada",
                    venda.getId() + " · " + venda.getProduto().getNome()
                            + " · " + quantidade + " un. · " + moeda.format(venda.getTotal()));
        } catch (NumberFormatException e) {
            mostrarMensagem(Alert.AlertType.WARNING, "Quantidade inválida", "Informe um número inteiro maior que zero.");
        } catch (IllegalArgumentException e) {
            mostrarMensagem(Alert.AlertType.WARNING, "Não foi possível registrar a venda", e.getMessage());
        }
    }

    @FXML
    private void onRegistrarDevolucao(ActionEvent event) {
        try {
            LojaService.Venda venda = cmbVendaDevolucao.getValue();
            int quantidade = Integer.parseInt(txtQuantidadeDevolucao.getText().trim());
            LojaService.Devolucao devolucao = loja.registrarDevolucao(venda, quantidade);

            vendasDisponiveis.setPredicate(item -> item.getQuantidadeDisponivelParaDevolucao() > 0);
            cmbVendaDevolucao.getSelectionModel().clearSelection();
            atualizarQuantidadeDisponivel(null);
            txtQuantidadeDevolucao.clear();
            tblVendas.refresh();
            mostrarMensagem(Alert.AlertType.INFORMATION, "Devolução registrada",
                    devolucao.getId() + " · " + quantidade + " un. · estorno de " + moeda.format(devolucao.getValor()));
        } catch (NumberFormatException e) {
            mostrarMensagem(Alert.AlertType.WARNING, "Quantidade inválida", "Informe um número inteiro maior que zero.");
        } catch (IllegalArgumentException e) {
            mostrarMensagem(Alert.AlertType.WARNING, "Não foi possível registrar a devolução", e.getMessage());
        }
    }

    private void mostrarMensagem(Alert.AlertType tipo, String titulo, String mensagem) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensagem);
        alerta.showAndWait();
    }
}
