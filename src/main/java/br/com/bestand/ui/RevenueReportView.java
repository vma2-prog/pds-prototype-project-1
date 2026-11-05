package br.com.bestand.ui;

import br.com.bestand.database.DatabaseManager;
import javafx.geometry.Insets;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.sql.*;
import java.time.LocalDate;

public class RevenueReportView extends VBox {

    private DatePicker dpInicio = new DatePicker(LocalDate.now().minusDays(30));
    private DatePicker dpFim = new DatePicker(LocalDate.now());
    private Label lblVendas = new Label("R$ 0,00");
    private Label lblDevolucoes = new Label("R$ 0,00");
    private Label lblResultado = new Label("R$ 0,00");

    public RevenueReportView() {
        this.getStyleClass().add("page-panel");
        this.setSpacing(16);
        this.setPadding(new Insets(8, 0, 0, 0));

        Label title = new Label("Relatório de Receitas e Movimentações");
        title.getStyleClass().add("page-title");
        title.setStyle("-fx-font-size: 20px;");

        HBox filterBar = new HBox(12);
        Button btnConsultar = new Button("Consultar");
        btnConsultar.getStyleClass().add("btn-primary");
        btnConsultar.setOnAction(e -> carregarRelatorio());

        filterBar.getChildren().addAll(new Label("Data Inicial:"), dpInicio, new Label("Data Final:"), dpFim, btnConsultar);

        HBox cards = new HBox(16);
        cards.getChildren().addAll(
            createCard("Total em Vendas", lblVendas, "badge-turquesa"),
            createCard("Total em Devoluções", lblDevolucoes, "badge-coral"),
            createCard("Resultado Líquido do Período", lblResultado, "badge-azul")
        );

        this.getChildren().addAll(title, filterBar, cards);
        carregarRelatorio();
    }

    private VBox createCard(String title, Label lblValue, String badgeClass) {
        VBox card = new VBox(8);
        card.getStyleClass().add("card");
        HBox.setHgrow(card, Priority.ALWAYS);
        lblValue.getStyleClass().add("card-value");
        Label lblT = new Label(title);
        lblT.getStyleClass().add("card-title");
        card.getChildren().addAll(lblT, lblValue);
        return card;
    }

    private void carregarRelatorio() {
        if (dpInicio.getValue().isAfter(dpFim.getValue())) {
            Alert a = new Alert(Alert.AlertType.ERROR, "Data inicial deve ser anterior ou igual à data final.");
            a.show();
            return;
        }

        try (Connection conn = DatabaseManager.getConnection()) {
            String sqlVendas = "SELECT COALESCE(SUM(valor_total), 0) FROM vendas WHERE data_hora BETWEEN ? AND ?";
            double totalVendas = 0;
            try (PreparedStatement pstmt = conn.prepareStatement(sqlVendas)) {
                pstmt.setString(1, dpInicio.getValue().toString());
                pstmt.setString(2, dpFim.getValue().plusDays(1).toString());
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) totalVendas = rs.getDouble(1);
            }

            String sqlDev = "SELECT COALESCE(SUM(valor_estornado), 0) FROM devolucoes WHERE data_hora BETWEEN ? AND ?";
            double totalDev = 0;
            try (PreparedStatement pstmt = conn.prepareStatement(sqlDev)) {
                pstmt.setString(1, dpInicio.getValue().toString());
                pstmt.setString(2, dpFim.getValue().plusDays(1).toString());
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) totalDev = rs.getDouble(1);
            }

            lblVendas.setText(String.format("R$ %.2f", totalVendas));
            lblDevolucoes.setText(String.format("R$ %.2f", totalDev));
            lblResultado.setText(String.format("R$ %.2f", totalVendas - totalDev));

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
