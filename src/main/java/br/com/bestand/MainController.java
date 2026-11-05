package br.com.bestand;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;

public class MainController {

    // Referências aos elementos da interface definidos no FXML
    @FXML
    private TextField txtBuscar;

    @FXML
    private Label lblTotalProdutos;

    @FXML
    private Label lblUnitsEstoque;

    @FXML
    private Label lblEstoqueBaixo;

    @FXML
    private Label lblSemEstoque;

    @FXML
    private BarChart<String, Number> chartTendencia;

    // Método executado automaticamente assim que a tela é carregada
    public void initialize() {
        // Inicializa dados mockados/exemplo nos cards e no gráfico
        carregarDadosIniciais();
    }

    private void carregarDadosIniciais() {
        // Exemplo de valores baseados no seu protótipo
        if (lblTotalProdutos != null) lblTotalProdutos.setText("1.248");
        if (lblUnitsEstoque != null) lblUnitsEstoque.setText("8.420");
        if (lblEstoqueBaixo != null) lblEstoqueBaixo.setText("18");
        if (lblSemEstoque != null) lblSemEstoque.setText("5");

        // Configurando o gráfico de tendência se ele existir na tela
        if (chartTendencia != null) {
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Movimentações");
            
            // Dados fictícios para as barras do gráfico
            series.getData().add(new XYChart.Data<>("Sem 1", 300));
            series.getData().add(new XYChart.Data<>("Sem 2", 450));
            series.getData().add(new XYChart.Data<>("Sem 3", 380));
            series.getData().add(new XYChart.Data<>("Sem 4", 410));
            series.getData().add(new XYChart.Data<>("Sem 5", 490));

            chartTendencia.getData().add(series);
        }
    }

    // Exemplo de ação ao utilizar a barra de busca
    @FXML
    protected void onBuscarProdutos() {
        String termoBusca = txtBuscar.getText();
        System.out.println("Buscando por: " + termoBusca);
    }
}