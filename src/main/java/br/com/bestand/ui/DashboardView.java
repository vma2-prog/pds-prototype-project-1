package br.com.bestand.ui;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

public class DashboardView extends VBox {

    public DashboardView() {
        this.setSpacing(20);

        HBox panelHeader = new HBox();
        panelHeader.setAlignment(Pos.CENTER_LEFT);
        panelHeader.setPadding(new Insets(2, 0, 0, 0));
        Label lblTitle = new Label("Painel");
        lblTitle.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #ffffff;");

        Region headerSpacer = new Region();
        HBox.setHgrow(headerSpacer, Priority.ALWAYS);

        ComboBox<String> comboPeriodo = new ComboBox<>();
        comboPeriodo.getItems().addAll("Últimos 30 dias", "Últimos 7 dias", "Este mês");
        comboPeriodo.setValue("Últimos 30 dias");
        comboPeriodo.setPrefWidth(180);
        comboPeriodo.setPrefHeight(34);
        comboPeriodo.getStyleClass().add("search-field");

        panelHeader.getChildren().addAll(lblTitle, headerSpacer, comboPeriodo);

        HBox kpiRow = new HBox(16);
        kpiRow.getChildren().addAll(
            createKpiCard("Produtos cadastrados", "1.248", "↑ 12%", "badge-azul"),
            createKpiCard("Unidades em estoque", "8.420", "↑ 8%", "badge-turquesa"),
            createKpiCard("Estoque baixo", "18", "↓ 5%", "badge-amarelo"),
            createKpiCard("Sem estoque", "5", "↓ 20%", "badge-coral")
        );
        for (javafx.scene.Node n : kpiRow.getChildren()) {
            HBox.setHgrow(n, Priority.ALWAYS);
        }

        HBox chartsRow = new HBox(16);

        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis(0, 400, 100);
        LineChart<String, Number> lineChart = new LineChart<>(xAxis, yAxis);
        lineChart.setTitle("Movimentações do estoque");

        XYChart.Series<String, Number> entradas = new XYChart.Series<>();
        entradas.setName("Entradas");
        entradas.getData().add(new XYChart.Data<>("Seg", 200));
        entradas.getData().add(new XYChart.Data<>("Ter", 300));
        entradas.getData().add(new XYChart.Data<>("Qua", 180));
        entradas.getData().add(new XYChart.Data<>("Qui", 250));
        entradas.getData().add(new XYChart.Data<>("Sex", 320));
        entradas.getData().add(new XYChart.Data<>("Sáb", 290));
        entradas.getData().add(new XYChart.Data<>("Dom", 310));

        XYChart.Series<String, Number> saidas = new XYChart.Series<>();
        saidas.setName("Saídas");
        saidas.getData().add(new XYChart.Data<>("Seg", 120));
        saidas.getData().add(new XYChart.Data<>("Ter", 210));
        saidas.getData().add(new XYChart.Data<>("Qua", 90));
        saidas.getData().add(new XYChart.Data<>("Qui", 190));
        saidas.getData().add(new XYChart.Data<>("Sex", 280));
        saidas.getData().add(new XYChart.Data<>("Sáb", 220));
        saidas.getData().add(new XYChart.Data<>("Dom", 240));

        lineChart.getData().addAll(entradas, saidas);
        lineChart.getStyleClass().add("card");

        PieChart pieChart = new PieChart();
        pieChart.setTitle("Distribuição por categoria");
        pieChart.getData().add(new PieChart.Data("Informática 34%", 34));
        pieChart.getData().add(new PieChart.Data("Periféricos 20%", 20));
        pieChart.getData().add(new PieChart.Data("Acessórios 22%", 22));
        pieChart.getData().add(new PieChart.Data("Eletrônicos 10%", 10));
        pieChart.getData().add(new PieChart.Data("Outros 6%", 6));

        StackPane donutPane = new StackPane();
        Circle hole = new Circle(45, Color.web("#222038"));
        VBox centerLabel = new VBox(
            new Label("1.248"),
            new Label("Total")
        );
        centerLabel.setAlignment(Pos.CENTER);
        ((Label) centerLabel.getChildren().get(0)).setStyle("-fx-font-weight: bold; -fx-text-fill: #ffffff; -fx-font-size: 14px;");
        ((Label) centerLabel.getChildren().get(1)).setStyle("-fx-text-fill: #a29dbd; -fx-font-size: 11px;");

        donutPane.getChildren().addAll(pieChart, hole, centerLabel);
        donutPane.getStyleClass().add("card");

        CategoryAxis barXAxis = new CategoryAxis();
        NumberAxis barYAxis = new NumberAxis(0, 750, 250);
        BarChart<String, Number> barChart = new BarChart<>(barXAxis, barYAxis);
        barChart.setTitle("Tendência de estoque");
        barChart.setLegendVisible(false);

        XYChart.Series<String, Number> barSeries = new XYChart.Series<>();
        barSeries.getData().add(new XYChart.Data<>("Jan", 420));
        barSeries.getData().add(new XYChart.Data<>("Fev", 410));
        barSeries.getData().add(new XYChart.Data<>("Mar", 480));
        barSeries.getData().add(new XYChart.Data<>("Abr", 620));
        barSeries.getData().add(new XYChart.Data<>("Mai", 610));
        barSeries.getData().add(new XYChart.Data<>("Jun", 710));

        barChart.getData().add(barSeries);
        barChart.getStyleClass().add("card");

        HBox.setHgrow(lineChart, Priority.ALWAYS);
        HBox.setHgrow(donutPane, Priority.ALWAYS);
        HBox.setHgrow(barChart, Priority.ALWAYS);
        chartsRow.getChildren().addAll(lineChart, donutPane, barChart);

        HBox bottomRow = new HBox(16);

        VBox alertsCard = new VBox(12);
        alertsCard.getStyleClass().add("card");
        alertsCard.setPrefWidth(380);

        HBox alertsHeader = new HBox();
        Label lblAlerts = new Label("Alertas de estoque");
        lblAlerts.setStyle("-fx-font-weight: bold; -fx-text-fill: #ffffff;");
        Region alertSpacer = new Region();
        HBox.setHgrow(alertSpacer, Priority.ALWAYS);
        Hyperlink linkAlerts = new Hyperlink("Ver todos →");
        linkAlerts.setStyle("-fx-text-fill: #7a6fbe;");
        alertsHeader.getChildren().addAll(lblAlerts, alertSpacer, linkAlerts);

        VBox alertsList = new VBox(10);
        alertsList.getChildren().addAll(
            createAlertItem("#E69A72", "Teclado Mecânico", "Sem estoque", "0 un."),
            createAlertItem("#D2B36F", "Monitor 24 polegadas", "Estoque baixo (4 un.)", "4 un."),
            createAlertItem("#7FA9CF", "Mouse sem fio", "Abaixo do mínimo (8 un.)", "8 un.")
        );

        alertsCard.getChildren().addAll(alertsHeader, alertsList);

        VBox tableCard = new VBox(12);
        tableCard.getStyleClass().add("card");
        HBox.setHgrow(tableCard, Priority.ALWAYS);

        HBox tableHeader = new HBox();
        Label lblAttention = new Label("Produtos que precisam de atenção");
        lblAttention.setStyle("-fx-font-weight: bold; -fx-text-fill: #ffffff;");
        Region tableSpacer = new Region();
        HBox.setHgrow(tableSpacer, Priority.ALWAYS);
        Hyperlink linkTable = new Hyperlink("Ver todos →");
        linkTable.setStyle("-fx-text-fill: #7a6fbe;");
        tableHeader.getChildren().addAll(lblAttention, tableSpacer, linkTable);

        TableView<AttentionProduct> table = new TableView<>();
        TableColumn<AttentionProduct, String> colProd = new TableColumn<>("Produto");
        colProd.setCellValueFactory(new PropertyValueFactory<>("nome"));

        TableColumn<AttentionProduct, String> colSku = new TableColumn<>("SKU");
        colSku.setCellValueFactory(new PropertyValueFactory<>("sku"));

        TableColumn<AttentionProduct, Integer> colAtual = new TableColumn<>("Quantidade atual");
        colAtual.setCellValueFactory(new PropertyValueFactory<>("qtdAtual"));

        TableColumn<AttentionProduct, Integer> colMin = new TableColumn<>("Estoque mínimo");
        colMin.setCellValueFactory(new PropertyValueFactory<>("estoqueMinimo"));

        TableColumn<AttentionProduct, String> colSit = new TableColumn<>("Situação");
        colSit.setCellValueFactory(new PropertyValueFactory<>("situacao"));

        table.getColumns().addAll(colProd, colSku, colAtual, colMin, colSit);

        ObservableList<AttentionProduct> items = FXCollections.observableArrayList(
            new AttentionProduct("Teclado Mecânico", "TEC-094", 8, 15, "Estoque baixo"),
            new AttentionProduct("Mouse sem fio", "MOU-03B", 0, 10, "Sem estoque"),
            new AttentionProduct("Monitor 24 polegadas", "MON-011", 4, 8, "Estoque baixo")
        );
        table.setItems(items);
        table.setPrefHeight(160);

        tableCard.getChildren().addAll(tableHeader, table);

        bottomRow.getChildren().addAll(alertsCard, tableCard);

        this.getChildren().addAll(panelHeader, kpiRow, chartsRow, bottomRow);
    }

    private VBox createKpiCard(String title, String value, String trend, String badgeClass) {
        VBox card = new VBox(8);
        card.getStyleClass().add("card");

        Label lblTitle = new Label(title);
        lblTitle.getStyleClass().add("card-title");

        Label lblValue = new Label(value);
        lblValue.getStyleClass().add("card-value");

        Label lblTrend = new Label(trend);
        lblTrend.getStyleClass().add(badgeClass);

        HBox bottom = new HBox(10, lblValue, lblTrend);
        bottom.setAlignment(Pos.CENTER_LEFT);

        card.getChildren().addAll(lblTitle, bottom);
        return card;
    }

    private HBox createAlertItem(String dotColorHex, String title, String subtitle, String qty) {
        HBox box = new HBox(10);
        box.setAlignment(Pos.CENTER_LEFT);

        Circle dot = new Circle(4, Color.web(dotColorHex));

        VBox texts = new VBox(2);
        Label lblT = new Label(title);
        lblT.setStyle("-fx-text-fill: #ffffff; -fx-font-weight: bold; -fx-font-size: 13px;");
        Label lblS = new Label(subtitle);
        lblS.setStyle("-fx-text-fill: #a29dbd; -fx-font-size: 11px;");
        texts.getChildren().addAll(lblT, lblS);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label lblQ = new Label(qty);
        lblQ.setStyle("-fx-text-fill: #ffffff; -fx-font-weight: bold;");

        box.getChildren().addAll(dot, texts, spacer, lblQ);
        return box;
    }

    public static class AttentionProduct {
        private final String nome;
        private final String sku;
        private final int qtdAtual;
        private final int estoqueMinimo;
        private final String situacao;

        public AttentionProduct(String nome, String sku, int qtdAtual, int estoqueMinimo, String situacao) {
            this.nome = nome;
            this.sku = sku;
            this.qtdAtual = qtdAtual;
            this.estoqueMinimo = estoqueMinimo;
            this.situacao = situacao;
        }

        public String getNome() { return nome; }
        public String getSku() { return sku; }
        public int getQtdAtual() { return qtdAtual; }
        public int getEstoqueMinimo() { return estoqueMinimo; }
        public String getSituacao() { return situacao; }
    }
}

