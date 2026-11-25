package br.com.bestand;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Locale;

public class ReceitasController {

    @FXML private DatePicker dpInicio;
    @FXML private DatePicker dpFim;

    @FXML private Label lblTotalVendas;
    @FXML private Label lblTotalDevolucoes;
    @FXML private Label lblTotalCarregamentos;
    @FXML private Label lblSaldo;
    @FXML private Label lblQtdVendas;
    @FXML private Label lblQtdDevolucoes;
    @FXML private Label lblQtdCarregamentos;
    @FXML private Label lblEstadoSaldo;

    @FXML private LineChart<String, Number> chartFinanceiro;
    @FXML private TableView<LojaService.Movimentacao> tblMovimentacoes;
    @FXML private TableColumn<LojaService.Movimentacao, String> colData;
    @FXML private TableColumn<LojaService.Movimentacao, String> colTipo;
    @FXML private TableColumn<LojaService.Movimentacao, String> colReferencia;
    @FXML private TableColumn<LojaService.Movimentacao, String> colValor;

    private final LojaService loja = LojaService.getInstance();
    private final NumberFormat moeda = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("pt-BR"));
    private FilteredList<LojaService.Movimentacao> movimentacoesDoPeriodo;

    @FXML
    public void initialize() {
        YearMonth mesAtual = YearMonth.now();
        dpInicio.setValue(mesAtual.atDay(1));
        dpFim.setValue(mesAtual.atEndOfMonth());

        movimentacoesDoPeriodo = new FilteredList<>(loja.getMovimentacoes());
        configurarTabela();
        tblMovimentacoes.setItems(movimentacoesDoPeriodo);
        tblMovimentacoes.setPlaceholder(new Label("Nenhum lançamento neste período."));
        atualizarRelatorio();
    }

    private void configurarTabela() {
        colData.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getDataFormatada()));
        colTipo.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getTipo()));
        colReferencia.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getReferencia()));
        colValor.setCellValueFactory(data -> new SimpleStringProperty(moeda.format(data.getValue().getValor())));
    }

    @FXML
    private void onAplicarPeriodo(ActionEvent event) {
        try {
            atualizarRelatorio();
        } catch (IllegalArgumentException e) {
            mostrarErro(e.getMessage());
        }
    }

    private void atualizarRelatorio() {
        LocalDate inicio = dpInicio.getValue();
        LocalDate fim = dpFim.getValue();
        LojaService.ResumoFinanceiro resumo = loja.calcularResumo(inicio, fim);

        lblTotalVendas.setText(moeda.format(resumo.getVendas()));
        lblTotalDevolucoes.setText(moeda.format(resumo.getDevolucoes()));
        lblTotalCarregamentos.setText(moeda.format(resumo.getCarregamentos()));
        lblSaldo.setText(moeda.format(resumo.getSaldo()));
        lblQtdVendas.setText(resumo.getQuantidadeVendas() + " vendas no período");
        lblQtdDevolucoes.setText(resumo.getQuantidadeDevolucoes() + " devoluções no período");
        lblQtdCarregamentos.setText(resumo.getQuantidadeCarregamentos() + " entradas no período");
        atualizarEstadoSaldo(resumo.getSaldo());

        movimentacoesDoPeriodo.setPredicate(movimentacao -> {
            LocalDate data = movimentacao.getDataHora().toLocalDate();
            return !data.isBefore(inicio) && !data.isAfter(fim);
        });
        atualizarGrafico(inicio, fim);
    }

    private void atualizarEstadoSaldo(BigDecimal saldo) {
        lblEstadoSaldo.getStyleClass().removeAll("result-positive", "result-negative", "result-neutral");
        int comparacao = saldo.compareTo(BigDecimal.ZERO);
        if (comparacao > 0) {
            lblEstadoSaldo.setText("Superávit estimado");
            lblEstadoSaldo.getStyleClass().add("result-positive");
        } else if (comparacao < 0) {
            lblEstadoSaldo.setText("Déficit estimado");
            lblEstadoSaldo.getStyleClass().add("result-negative");
        } else {
            lblEstadoSaldo.setText("Equilíbrio");
            lblEstadoSaldo.getStyleClass().add("result-neutral");
        }
    }

    private void atualizarGrafico(LocalDate inicio, LocalDate fim) {
        XYChart.Series<String, Number> vendas = new XYChart.Series<>();
        vendas.setName("Vendas");
        XYChart.Series<String, Number> devolucoes = new XYChart.Series<>();
        devolucoes.setName("Devoluções");
        XYChart.Series<String, Number> carregamentos = new XYChart.Series<>();
        carregamentos.setName("Carregamentos");

        for (LocalDate dia = inicio; !dia.isAfter(fim); dia = dia.plusDays(1)) {
            BigDecimal valorVendas = BigDecimal.ZERO;
            BigDecimal valorDevolucoes = BigDecimal.ZERO;
            BigDecimal valorCarregamentos = BigDecimal.ZERO;

            for (LojaService.Movimentacao movimentacao : movimentacoesDoPeriodo) {
                if (!movimentacao.getDataHora().toLocalDate().equals(dia)) {
                    continue;
                }
                switch (movimentacao.getTipo()) {
                    case "Venda" -> valorVendas = valorVendas.add(movimentacao.getValor());
                    case "Devolução" -> valorDevolucoes = valorDevolucoes.add(movimentacao.getValor());
                    case "Carregamento" -> valorCarregamentos = valorCarregamentos.add(movimentacao.getValor());
                    default -> throw new IllegalStateException(
                            "Tipo de movimentação desconhecido: " + movimentacao.getTipo()
                    );
                }
            }

            String categoria = String.format(Locale.ROOT, "%02d/%02d", dia.getDayOfMonth(), dia.getMonthValue());
            vendas.getData().add(new XYChart.Data<>(categoria, valorVendas));
            devolucoes.getData().add(new XYChart.Data<>(categoria, valorDevolucoes));
            carregamentos.getData().add(new XYChart.Data<>(categoria, valorCarregamentos));
        }
        chartFinanceiro.getData().setAll(vendas, devolucoes, carregamentos);
    }

    private void mostrarErro(String mensagem) {
        Alert alerta = new Alert(Alert.AlertType.WARNING);
        alerta.setTitle("Período inválido");
        alerta.setHeaderText(null);
        alerta.setContentText(mensagem);
        alerta.showAndWait();
    }
}
