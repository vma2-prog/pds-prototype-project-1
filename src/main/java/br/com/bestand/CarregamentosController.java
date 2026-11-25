package br.com.bestand;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

public class CarregamentosController {

    @FXML private TextField txtFornecedor;
    @FXML private TextField txtQuantidade;
    @FXML private TextField txtCustoUnitario;
    @FXML private ComboBox<LojaService.Produto> cmbProduto;

    @FXML private TableView<LojaService.Carregamento> tblCarregamentos;
    @FXML private TableColumn<LojaService.Carregamento, String> colData;
    @FXML private TableColumn<LojaService.Carregamento, String> colId;
    @FXML private TableColumn<LojaService.Carregamento, String> colFornecedor;
    @FXML private TableColumn<LojaService.Carregamento, String> colProduto;
    @FXML private TableColumn<LojaService.Carregamento, Number> colQuantidade;
    @FXML private TableColumn<LojaService.Carregamento, String> colCustoUnitario;
    @FXML private TableColumn<LojaService.Carregamento, String> colCustoTotal;

    private final LojaService loja = LojaService.getInstance();
    private final NumberFormat moeda = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("pt-BR"));

    @FXML
    public void initialize() {
        cmbProduto.setItems(loja.getProdutos());
        cmbProduto.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(LojaService.Produto produto, boolean empty) {
                super.updateItem(produto, empty);
                setText(empty || produto == null ? null
                        : produto.getNome());
            }
        });
        cmbProduto.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(LojaService.Produto produto, boolean empty) {
                super.updateItem(produto, empty);
                setText(empty || produto == null ? null
                        : produto.getNome());
            }
        });
        configurarTabela();
        tblCarregamentos.setItems(loja.getCarregamentos());
    }

    private void configurarTabela() {
        colData.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getDataFormatada()));
        colId.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getId()));
        colFornecedor.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getFornecedor()));
        colProduto.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getProduto().getNome()));
        colQuantidade.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getQuantidade()));
        colCustoUnitario.setCellValueFactory(data -> new SimpleStringProperty(moeda.format(data.getValue().getCustoUnitario())));
        colCustoTotal.setCellValueFactory(data -> new SimpleStringProperty(moeda.format(data.getValue().getCustoTotal())));
    }

    @FXML
    private void onRegistrarCarregamento(ActionEvent event) {
        try {
            int quantidade = Integer.parseInt(txtQuantidade.getText().trim());
            BigDecimal custo = new BigDecimal(txtCustoUnitario.getText().trim().replace(",", "."));
            LojaService.Carregamento carregamento = loja.registrarCarregamento(
                    txtFornecedor.getText(),
                    cmbProduto.getValue(),
                    quantidade,
                    custo
            );
            tblCarregamentos.refresh();
            txtFornecedor.clear();
            txtQuantidade.clear();
            txtCustoUnitario.clear();
            mostrarMensagem(Alert.AlertType.INFORMATION, "Carregamento registrado",
                    carregamento.getId() + " · " + quantidade + " un. de "
                            + carregamento.getProduto().getNome() + " · custo "
                            + moeda.format(carregamento.getCustoTotal()));
        } catch (NumberFormatException e) {
            mostrarMensagem(Alert.AlertType.WARNING, "Dados inválidos",
                    "Informe a quantidade em números inteiros e o custo por unidade, por exemplo: 25,90.");
        } catch (IllegalArgumentException e) {
            mostrarMensagem(Alert.AlertType.WARNING, "Não foi possível registrar o carregamento", e.getMessage());
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
