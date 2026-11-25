package br.com.bestand;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class DashboardController {

    @FXML private Label lblTotalProdutos;
    @FXML private Label lblUnidadesEstoque;
    @FXML private Label lblProdutosZerados;
    @FXML private Label lblResultadoMes;
    @FXML private Label lblEstadoResultado;
    @FXML private Label lblDetalheResultado;
    @FXML private Label lblMesReferencia;

    @FXML private LineChart<String, Number> chartMovimentacoes;

    @FXML private TableView<RegistroResumo> tblUltimosRegistros;
    @FXML private TableColumn<RegistroResumo, String> colTipo;
    @FXML private TableColumn<RegistroResumo, String> colReferencia;
    @FXML private TableColumn<RegistroResumo, String> colResponsavel;

    private final LojaService loja = LojaService.getInstance();
    private final NumberFormat moeda = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("pt-BR"));
    private MainController mainController;

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    @FXML
    public void initialize() {
        atualizarMesReferencia();
        atualizarResumoEstoque();
        atualizarGrafico();
        atualizarResultado();
        carregarTabelaRecente();
    }

    private void atualizarMesReferencia() {
        String mes = YearMonth.now()
                .format(DateTimeFormatter.ofPattern("MMMM 'de' yyyy", Locale.forLanguageTag("pt-BR")));
        lblMesReferencia.setText(Character.toUpperCase(mes.charAt(0)) + mes.substring(1));
    }

    private void atualizarResumoEstoque() {
        int unidades = 0;
        int zerados = 0;
        for (LojaService.Produto produto : loja.getProdutos()) {
            unidades += produto.getEstoque();
            if (produto.getEstoque() == 0) {
                zerados++;
            }
        }

        lblTotalProdutos.setText(Integer.toString(loja.getProdutos().size()));
        lblUnidadesEstoque.setText(NumberFormat.getIntegerInstance(Locale.forLanguageTag("pt-BR")).format(unidades));
        lblProdutosZerados.setText(Integer.toString(zerados));
    }

    private void atualizarGrafico() {
        YearMonth mesAtual = YearMonth.now();
        LocalDate hoje = LocalDate.now();
        int ultimoDia = hoje.getDayOfMonth();

        XYChart.Series<String, Number> vendas = new XYChart.Series<>();
        vendas.setName("Vendas");
        XYChart.Series<String, Number> devolucoes = new XYChart.Series<>();
        devolucoes.setName("Devoluções");
        XYChart.Series<String, Number> carregamentos = new XYChart.Series<>();
        carregamentos.setName("Carregamentos");

        for (int dia = 1; dia <= ultimoDia; dia++) {
            LocalDate data = mesAtual.atDay(dia);
            String categoria = String.format(Locale.ROOT, "%02d", dia);
            vendas.getData().add(new XYChart.Data<>(categoria, contarVendas(data)));
            devolucoes.getData().add(new XYChart.Data<>(categoria, contarDevolucoes(data)));
            carregamentos.getData().add(new XYChart.Data<>(categoria, contarCarregamentos(data)));
        }

        chartMovimentacoes.getData().setAll(vendas, devolucoes, carregamentos);
    }

    private int contarVendas(LocalDate data) {
        return (int) loja.getVendas().stream()
                .filter(venda -> venda.getDataHora().toLocalDate().equals(data))
                .count();
    }

    private int contarDevolucoes(LocalDate data) {
        return (int) loja.getDevolucoes().stream()
                .filter(devolucao -> devolucao.getDataHora().toLocalDate().equals(data))
                .count();
    }

    private int contarCarregamentos(LocalDate data) {
        return (int) loja.getCarregamentos().stream()
                .filter(carregamento -> carregamento.getDataHora().toLocalDate().equals(data))
                .count();
    }

    private void atualizarResultado() {
        YearMonth mes = YearMonth.now();
        LojaService.ResumoFinanceiro resumo = loja.calcularResumo(mes.atDay(1), LocalDate.now());

        lblResultadoMes.setText(moeda.format(resumo.getSaldo()));
        lblDetalheResultado.setText(
                "Vendas " + moeda.format(resumo.getVendas())
                        + " − devoluções " + moeda.format(resumo.getDevolucoes())
                        + " − carregamentos " + moeda.format(resumo.getCarregamentos())
                        + ". Não inclui outras despesas."
        );

        lblEstadoResultado.getStyleClass().removeAll("result-positive", "result-negative", "result-neutral");
        int comparacao = resumo.getSaldo().compareTo(BigDecimal.ZERO);
        if (comparacao > 0) {
            lblEstadoResultado.setText("Superávit estimado");
            lblEstadoResultado.getStyleClass().add("result-positive");
        } else if (comparacao < 0) {
            lblEstadoResultado.setText("Déficit estimado");
            lblEstadoResultado.getStyleClass().add("result-negative");
        } else {
            lblEstadoResultado.setText("Equilíbrio");
            lblEstadoResultado.getStyleClass().add("result-neutral");
        }
    }

    private void carregarTabelaRecente() {
        colTipo.setCellValueFactory(data -> data.getValue().tipoProperty());
        colReferencia.setCellValueFactory(data -> data.getValue().referenciaProperty());
        colResponsavel.setCellValueFactory(data -> data.getValue().responsavelProperty());

        List<LojaService.Movimentacao> recentes = new ArrayList<>(loja.getMovimentacoes());
        recentes.sort(Comparator.comparing(LojaService.Movimentacao::getDataHora).reversed());

        ObservableList<RegistroResumo> registros = FXCollections.observableArrayList();
        recentes.stream().limit(6).forEach(movimentacao -> registros.add(
                new RegistroResumo(
                        movimentacao.getTipo(),
                        movimentacao.getReferencia() + " (" + moeda.format(movimentacao.getValor()) + ")",
                        "operador"
                )
        ));

        tblUltimosRegistros.setItems(registros);
        tblUltimosRegistros.setPlaceholder(new Label("Nenhuma movimentação registrada nesta sessão."));
    }

    @FXML
    private void onNovoCarregamento(ActionEvent event) {
        if (mainController != null) {
            mainController.onNavCarregamentos(event);
        }
    }

    @FXML
    private void onNovaVenda(ActionEvent event) {
        if (mainController != null) {
            mainController.onNavVendas(event);
        }
    }

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
