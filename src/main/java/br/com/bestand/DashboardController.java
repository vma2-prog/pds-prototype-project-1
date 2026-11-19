package br.com.bestand;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

public class DashboardController {

    @FXML private Label lblTotalProdutos;
    @FXML private Label lblUnidadesEstoque;
    @FXML private Label lblProdutosZerados;
    @FXML private Label lblResultadoMes;

    @FXML private BarChart<String, Number> barChartFinanceiro;

    @FXML private TableView<RegistroResumo> tblUltimosRegistros;
    @FXML private TableColumn<RegistroResumo, String> colTipo;
    @FXML private TableColumn<RegistroResumo, String> colReferencia;
    @FXML private TableColumn<RegistroResumo, String> colResponsavel;

    private MainController mainController;

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    @FXML
    public void initialize() {
        carregarGraficoExemplo();
        carregarTabelaExemplo();
    }

    private void carregarGraficoExemplo() {
        XYChart.Series<String, Number> serieVendas = new XYChart.Series<>();
        serieVendas.setName("Vendas");
        serieVendas.getData().add(new XYChart.Data<>("12/11", 1100));
        serieVendas.getData().add(new XYChart.Data<>("13/11", 1450));
        serieVendas.getData().add(new XYChart.Data<>("14/11", 950));
        serieVendas.getData().add(new XYChart.Data<>("15/11", 1650));
        serieVendas.getData().add(new XYChart.Data<>("16/11", 1300));
        serieVendas.getData().add(new XYChart.Data<>("17/11", 1800));
        serieVendas.getData().add(new XYChart.Data<>("18/11", 1500));

        XYChart.Series<String, Number> serieDevolucoes = new XYChart.Series<>();
        serieDevolucoes.setName("Devoluções");
        serieDevolucoes.getData().add(new XYChart.Data<>("12/11", 150));
        serieDevolucoes.getData().add(new XYChart.Data<>("13/11", 0));
        serieDevolucoes.getData().add(new XYChart.Data<>("14/11", 300));
        serieDevolucoes.getData().add(new XYChart.Data<>("15/11", 100));
        serieDevolucoes.getData().add(new XYChart.Data<>("16/11", 0));
        serieDevolucoes.getData().add(new XYChart.Data<>("17/11", 200));
        serieDevolucoes.getData().add(new XYChart.Data<>("18/11", 120));

        barChartFinanceiro.getData().addAll(serieVendas, serieDevolucoes);
    }

    private void carregarTabelaExemplo() {
        colTipo.setCellValueFactory(data -> data.getValue().tipoProperty());
        colReferencia.setCellValueFactory(data -> data.getValue().referenciaProperty());
        colResponsavel.setCellValueFactory(data -> data.getValue().responsavelProperty());

        ObservableList<RegistroResumo> registros = FXCollections.observableArrayList(
                new RegistroResumo("Venda", "#VND-1042 (R$ 340,00)", "admin"),
                new RegistroResumo("Carregamento", "Fornecedor Alpha (+50 un.)", "admin"),
                new RegistroResumo("Devolução", "Ref. #VND-1039 (-R$ 85,00)", "operador"),
                new RegistroResumo("Venda", "#VND-1041 (R$ 120,00)", "operador")
        );

        tblUltimosRegistros.setItems(registros);
    }

    @FXML
    private void onNovoCarregamento(ActionEvent event) {
        if (mainController != null) {
            // Atalho direto para a aba de Carregamentos
            mainController.onNavCarregamentos(event);
        }
    }

    @FXML
    private void onNovaVenda(ActionEvent event) {
        if (mainController != null) {
            // Atalho direto para a aba de Vendas
            mainController.onNavVendas(event);
        }
    }

    // Modelo simples de linha para a tabela de resumo do Dashboard
    public static class RegistroResumo {
        private final SimpleStringProperty tipo;
        private final SimpleStringProperty referencia;
        private final SimpleStringProperty responsavel;

        public RegistroResumo(String tipo, String referencia, String responsavel) {
            this.tipo = new SimpleStringProperty(tipo);
            this.referencia = new SimpleStringProperty(referencia);
            this.responsavel = new SimpleStringProperty(responsavel);
        }

        public SimpleStringProperty tipoProperty() { return tipo; }
        public SimpleStringProperty referenciaProperty() { return referencia; }
        public SimpleStringProperty responsavelProperty() { return responsavel; }
    }
}