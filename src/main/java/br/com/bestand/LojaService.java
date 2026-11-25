package br.com.bestand;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public final class LojaService {

    private static final LojaService INSTANCIA = new LojaService();
    private static final DateTimeFormatter FORMATO_DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final ObservableList<Produto> produtos = FXCollections.observableArrayList();
    private final ObservableList<Produto> produtosVisiveis = FXCollections.unmodifiableObservableList(produtos);
    private final ObservableList<Venda> vendas = FXCollections.observableArrayList();
    private final ObservableList<Venda> vendasVisiveis = FXCollections.unmodifiableObservableList(vendas);
    private final ObservableList<Devolucao> devolucoes = FXCollections.observableArrayList();
    private final ObservableList<Devolucao> devolucoesVisiveis = FXCollections.unmodifiableObservableList(devolucoes);
    private final ObservableList<Carregamento> carregamentos = FXCollections.observableArrayList();
    private final ObservableList<Carregamento> carregamentosVisiveis = FXCollections.unmodifiableObservableList(carregamentos);
    private final ObservableList<Movimentacao> movimentacoes = FXCollections.observableArrayList();
    private final ObservableList<Movimentacao> movimentacoesVisiveis = FXCollections.unmodifiableObservableList(movimentacoes);

    private int proximaVenda = 1;
    private int proximaDevolucao = 1;
    private int proximoCarregamento = 1;

    public LojaService() {
        carregarProdutosDemonstrativos();
    }

    public static LojaService getInstance() {
        return INSTANCIA;
    }

    public ObservableList<Produto> getProdutos() {
        return produtosVisiveis;
    }

    public ObservableList<Venda> getVendas() {
        return vendasVisiveis;
    }

    public ObservableList<Devolucao> getDevolucoes() {
        return devolucoesVisiveis;
    }

    public ObservableList<Carregamento> getCarregamentos() {
        return carregamentosVisiveis;
    }

    public ObservableList<Movimentacao> getMovimentacoes() {
        return movimentacoesVisiveis;
    }

    public void removerProduto(Produto produto) {
        if (!produtos.remove(Objects.requireNonNull(produto, "Produto obrigatório."))) {
            throw new IllegalArgumentException("O produto não está mais no catálogo.");
        }
    }

    public Venda registrarVenda(Produto produto, int quantidade) {
        validarProdutoAtivo(produto);
        if (quantidade <= 0) {
            throw new IllegalArgumentException("A quantidade da venda deve ser maior que zero.");
        }
        if (quantidade > produto.getEstoque()) {
            throw new IllegalArgumentException("Estoque insuficiente. Disponível: " + produto.getEstoque() + " un.");
        }

        BigDecimal precoUnitario = BigDecimal.valueOf(produto.getPrecoVenda()).setScale(2, RoundingMode.HALF_UP);
        BigDecimal total = precoUnitario.multiply(BigDecimal.valueOf(quantidade));
        Venda venda = new Venda(
                String.format("VND-%04d", proximaVenda++),
                LocalDateTime.now(),
                produto,
                quantidade,
                precoUnitario,
                total
        );

        produto.retirarEstoque(quantidade);
        vendas.add(venda);
        movimentacoes.add(new Movimentacao(
                venda.getDataHora(),
                "Venda",
                venda.getId() + " · " + produto.getNome() + " (" + quantidade + " un.)",
                total
        ));
        return venda;
    }

    public Devolucao registrarDevolucao(Venda venda, int quantidade) {
        if (venda == null || !vendas.contains(venda)) {
            throw new IllegalArgumentException("Selecione uma venda válida.");
        }
        if (quantidade <= 0) {
            throw new IllegalArgumentException("A quantidade da devolução deve ser maior que zero.");
        }
        if (quantidade > venda.getQuantidadeDisponivelParaDevolucao()) {
            throw new IllegalArgumentException(
                    "Quantidade acima do limite. Ainda podem ser devolvidas "
                            + venda.getQuantidadeDisponivelParaDevolucao() + " un."
            );
        }

        BigDecimal valor = venda.getPrecoUnitario().multiply(BigDecimal.valueOf(quantidade));
        Devolucao devolucao = new Devolucao(
                String.format("DEV-%04d", proximaDevolucao++),
                LocalDateTime.now(),
                venda,
                quantidade,
                valor
        );

        venda.registrarQuantidadeDevolvida(quantidade);
        venda.getProduto().adicionarEstoque(quantidade);
        devolucoes.add(devolucao);
        movimentacoes.add(new Movimentacao(
                devolucao.getDataHora(),
                "Devolução",
                devolucao.getId() + " · " + venda.getProduto().getNome() + " (" + quantidade + " un.)",
                valor.negate()
        ));
        return devolucao;
    }

    public Carregamento registrarCarregamento(
            String fornecedor,
            Produto produto,
            int quantidade,
            BigDecimal custoUnitario
    ) {
        validarProdutoAtivo(produto);
        if (fornecedor == null || fornecedor.isBlank()) {
            throw new IllegalArgumentException("Informe o fornecedor do carregamento.");
        }
        if (quantidade <= 0) {
            throw new IllegalArgumentException("A quantidade do carregamento deve ser maior que zero.");
        }
        if (custoUnitario == null || custoUnitario.signum() <= 0) {
            throw new IllegalArgumentException("O custo unitário deve ser maior que zero.");
        }

        BigDecimal custo = custoUnitario.setScale(2, RoundingMode.HALF_UP);
        Carregamento carregamento = new Carregamento(
                String.format("CAR-%04d", proximoCarregamento++),
                LocalDateTime.now(),
                fornecedor.trim(),
                produto,
                quantidade,
                custo,
                custo.multiply(BigDecimal.valueOf(quantidade))
        );

        produto.adicionarEstoque(quantidade);
        carregamentos.add(carregamento);
        movimentacoes.add(new Movimentacao(
                carregamento.getDataHora(),
                "Carregamento",
                carregamento.getId() + " · " + fornecedor.trim() + " · "
                        + produto.getNome() + " (" + quantidade + " un.)",
                carregamento.getCustoTotal().negate()
        ));
        return carregamento;
    }

    public ResumoFinanceiro calcularResumo(LocalDate inicio, LocalDate fim) {
        if (inicio == null || fim == null || fim.isBefore(inicio)) {
            throw new IllegalArgumentException("Informe um período válido.");
        }

        BigDecimal vendasPeriodo = BigDecimal.ZERO;
        BigDecimal devolucoesPeriodo = BigDecimal.ZERO;
        BigDecimal carregamentosPeriodo = BigDecimal.ZERO;
        int quantidadeVendas = 0;
        int quantidadeDevolucoes = 0;
        int quantidadeCarregamentos = 0;

        for (Movimentacao movimentacao : movimentacoes) {
            LocalDate data = movimentacao.getDataHora().toLocalDate();
            if (data.isBefore(inicio) || data.isAfter(fim)) {
                continue;
            }

            switch (movimentacao.getTipo()) {
                case "Venda" -> {
                    vendasPeriodo = vendasPeriodo.add(movimentacao.getValor());
                    quantidadeVendas++;
                }
                case "Devolução" -> {
                    devolucoesPeriodo = devolucoesPeriodo.add(movimentacao.getValor().abs());
                    quantidadeDevolucoes++;
                }
                case "Carregamento" -> {
                    carregamentosPeriodo = carregamentosPeriodo.add(movimentacao.getValor().abs());
                    quantidadeCarregamentos++;
                }
                default -> throw new IllegalStateException("Tipo de movimentação desconhecido: " + movimentacao.getTipo());
            }
        }

        BigDecimal saldo = vendasPeriodo.subtract(devolucoesPeriodo).subtract(carregamentosPeriodo);
        return new ResumoFinanceiro(
                vendasPeriodo,
                devolucoesPeriodo,
                carregamentosPeriodo,
                saldo,
                quantidadeVendas,
                quantidadeDevolucoes,
                quantidadeCarregamentos
        );
    }

    private void validarProdutoAtivo(Produto produto) {
        if (produto == null || !produtos.contains(produto)) {
            throw new IllegalArgumentException("Selecione um produto disponível no catálogo.");
        }
    }

    private void carregarProdutosDemonstrativos() {
        produtos.addAll(
                new Produto("PRD-001", "Teclado Mecânico RGB", 34, 10, 289.90),
                new Produto("PRD-002", "Mouse Sem Fio Ergonômico", 6, 10, 149.50),
                new Produto("PRD-003", "Monitor LED 24\" Full HD", 0, 5, 899.00),
                new Produto("PRD-004", "Headset USB com Microfone", 18, 8, 179.00),
                new Produto("PRD-005", "Cabo HDMI 2.1 Ultra HD 2m", 52, 15, 45.00),
                new Produto("PRD-006", "Webcam Full HD 1080p", 4, 8, 220.00),
                new Produto("PRD-007", "SSD NVMe 1TB M.2", 0, 6, 459.90)
        );
    }

    public static final class Produto {
        private final String codigo;
        private final SimpleStringProperty nome;
        private final int estoqueMinimo;
        private final SimpleIntegerProperty estoque;
        private final SimpleDoubleProperty precoVenda;
        private final SimpleStringProperty situacao;

        public Produto(String codigo, String nome, int estoque, int estoqueMinimo, double precoVenda) {
            this.codigo = codigo;
            this.nome = new SimpleStringProperty(nome);
            this.estoqueMinimo = estoqueMinimo;
            this.estoque = new SimpleIntegerProperty(estoque);
            this.precoVenda = new SimpleDoubleProperty(precoVenda);
            this.situacao = new SimpleStringProperty(calcularSituacao(estoque));
        }

        public String getCodigo() { return codigo; }
        public String getNome() { return nome.get(); }
        public int getEstoque() { return estoque.get(); }
        public double getPrecoVenda() { return precoVenda.get(); }
        public String getSituacao() { return situacao.get(); }

        public SimpleStringProperty nomeProperty() { return nome; }
        public SimpleIntegerProperty estoqueProperty() { return estoque; }
        public SimpleDoubleProperty precoVendaProperty() { return precoVenda; }
        public SimpleStringProperty situacaoProperty() { return situacao; }

        private void adicionarEstoque(int quantidade) {
            estoque.set(estoque.get() + quantidade);
            situacao.set(calcularSituacao(estoque.get()));
        }

        private void retirarEstoque(int quantidade) {
            estoque.set(estoque.get() - quantidade);
            situacao.set(calcularSituacao(estoque.get()));
        }

        private String calcularSituacao(int quantidade) {
            if (quantidade <= 0) return "Sem estoque";
            if (quantidade <= estoqueMinimo) return "Estoque baixo";
            return "Estoque normal";
        }
    }

    public static final class Venda {
        private final String id;
        private final LocalDateTime dataHora;
        private final Produto produto;
        private final int quantidade;
        private final BigDecimal precoUnitario;
        private final BigDecimal total;
        private final SimpleIntegerProperty quantidadeDevolvida = new SimpleIntegerProperty();
        private final SimpleStringProperty situacao = new SimpleStringProperty("Concluída");

        private Venda(String id, LocalDateTime dataHora, Produto produto, int quantidade, BigDecimal precoUnitario, BigDecimal total) {
            this.id = id;
            this.dataHora = dataHora;
            this.produto = produto;
            this.quantidade = quantidade;
            this.precoUnitario = precoUnitario;
            this.total = total;
        }

        public String getId() { return id; }
        public LocalDateTime getDataHora() { return dataHora; }
        public String getDataFormatada() { return FORMATO_DATA.format(dataHora); }
        public Produto getProduto() { return produto; }
        public int getQuantidade() { return quantidade; }
        public BigDecimal getPrecoUnitario() { return precoUnitario; }
        public BigDecimal getTotal() { return total; }
        public int getQuantidadeDevolvida() { return quantidadeDevolvida.get(); }
        public int getQuantidadeDisponivelParaDevolucao() { return quantidade - quantidadeDevolvida.get(); }
        public SimpleIntegerProperty quantidadeDevolvidaProperty() { return quantidadeDevolvida; }
        public SimpleStringProperty situacaoProperty() { return situacao; }

        private void registrarQuantidadeDevolvida(int quantidade) {
            quantidadeDevolvida.set(quantidadeDevolvida.get() + quantidade);
            if (getQuantidadeDisponivelParaDevolucao() == 0) {
                situacao.set("Devolvida");
            } else {
                situacao.set("Devolução parcial");
            }
        }
    }

    public static final class Devolucao {
        private final String id;
        private final LocalDateTime dataHora;
        private final Venda venda;
        private final int quantidade;
        private final BigDecimal valor;

        private Devolucao(String id, LocalDateTime dataHora, Venda venda, int quantidade, BigDecimal valor) {
            this.id = id;
            this.dataHora = dataHora;
            this.venda = venda;
            this.quantidade = quantidade;
            this.valor = valor;
        }

        public String getId() { return id; }
        public LocalDateTime getDataHora() { return dataHora; }
        public String getDataFormatada() { return FORMATO_DATA.format(dataHora); }
        public Venda getVenda() { return venda; }
        public int getQuantidade() { return quantidade; }
        public BigDecimal getValor() { return valor; }
    }

    public static final class Carregamento {
        private final String id;
        private final LocalDateTime dataHora;
        private final String fornecedor;
        private final Produto produto;
        private final int quantidade;
        private final BigDecimal custoUnitario;
        private final BigDecimal custoTotal;

        private Carregamento(
                String id,
                LocalDateTime dataHora,
                String fornecedor,
                Produto produto,
                int quantidade,
                BigDecimal custoUnitario,
                BigDecimal custoTotal
        ) {
            this.id = id;
            this.dataHora = dataHora;
            this.fornecedor = fornecedor;
            this.produto = produto;
            this.quantidade = quantidade;
            this.custoUnitario = custoUnitario;
            this.custoTotal = custoTotal;
        }

        public String getId() { return id; }
        public LocalDateTime getDataHora() { return dataHora; }
        public String getDataFormatada() { return FORMATO_DATA.format(dataHora); }
        public String getFornecedor() { return fornecedor; }
        public Produto getProduto() { return produto; }
        public int getQuantidade() { return quantidade; }
        public BigDecimal getCustoUnitario() { return custoUnitario; }
        public BigDecimal getCustoTotal() { return custoTotal; }
    }

    public static final class Movimentacao {
        private final LocalDateTime dataHora;
        private final String tipo;
        private final String referencia;
        private final BigDecimal valor;

        private Movimentacao(LocalDateTime dataHora, String tipo, String referencia, BigDecimal valor) {
            this.dataHora = dataHora;
            this.tipo = tipo;
            this.referencia = referencia;
            this.valor = valor;
        }

        public LocalDateTime getDataHora() { return dataHora; }
        public String getDataFormatada() { return FORMATO_DATA.format(dataHora); }
        public String getTipo() { return tipo; }
        public String getReferencia() { return referencia; }
        public BigDecimal getValor() { return valor; }
    }

    public static final class ResumoFinanceiro {
        private final BigDecimal vendas;
        private final BigDecimal devolucoes;
        private final BigDecimal carregamentos;
        private final BigDecimal saldo;
        private final int quantidadeVendas;
        private final int quantidadeDevolucoes;
        private final int quantidadeCarregamentos;

        private ResumoFinanceiro(
                BigDecimal vendas,
                BigDecimal devolucoes,
                BigDecimal carregamentos,
                BigDecimal saldo,
                int quantidadeVendas,
                int quantidadeDevolucoes,
                int quantidadeCarregamentos
        ) {
            this.vendas = vendas;
            this.devolucoes = devolucoes;
            this.carregamentos = carregamentos;
            this.saldo = saldo;
            this.quantidadeVendas = quantidadeVendas;
            this.quantidadeDevolucoes = quantidadeDevolucoes;
            this.quantidadeCarregamentos = quantidadeCarregamentos;
        }

        public BigDecimal getVendas() { return vendas; }
        public BigDecimal getDevolucoes() { return devolucoes; }
        public BigDecimal getCarregamentos() { return carregamentos; }
        public BigDecimal getSaldo() { return saldo; }
        public int getQuantidadeVendas() { return quantidadeVendas; }
        public int getQuantidadeDevolucoes() { return quantidadeDevolucoes; }
        public int getQuantidadeCarregamentos() { return quantidadeCarregamentos; }
    }
}
