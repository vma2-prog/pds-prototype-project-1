package br.com.bestand;

import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.text.NumberFormat;
import java.util.Comparator;
import java.util.Locale;
import java.util.Optional;

public class ProdutosController {

    @FXML private TextField txtPesquisa;
    @FXML private ComboBox<String> cmbOrdenacao;

    @FXML private TableView<LojaService.Produto> tblProdutos;
    @FXML private TableColumn<LojaService.Produto, String> colProduto;
    @FXML private TableColumn<LojaService.Produto, Number> colEstoque;
    @FXML private TableColumn<LojaService.Produto, Number> colPrecoVenda;
    @FXML private TableColumn<LojaService.Produto, String> colSituacao;
    @FXML private TableColumn<LojaService.Produto, Void> colAcoes;

    private final ObservableList<LojaService.Produto> listaMestra = LojaService.getInstance().getProdutos();
    private FilteredList<LojaService.Produto> listaFiltrada;
    private SortedList<LojaService.Produto> listaOrdenada;

    private static final NumberFormat MOEDA_BR = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));

    @FXML
    public void initialize() {
        configurarOrdenacao();
        configurarColunas();
        configurarFiltrosEOrdenacao();
    }

    public void aplicarFiltroInicial(String termo) {
        if (termo != null && !termo.isBlank()) {
            txtPesquisa.setText(termo);
        }
    }

    private void configurarOrdenacao() {
        cmbOrdenacao.getItems().addAll(
                "Nome (A → Z)",
                "Nome (Z → A)",
                "Estoque (Menor → Maior)",
                "Estoque (Maior → Menor)",
                "Preço (Menor → Maior)",
                "Preço (Maior → Menor)"
        );
        cmbOrdenacao.getSelectionModel().selectFirst();
    }

    private void configurarColunas() {
        // 1. Coluna Produto (Nome em destaque + Código abaixo)
        colProduto.setCellValueFactory(data -> data.getValue().nomeProperty());
        colProduto.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                } else {
                    LojaService.Produto p = getTableRow().getItem();
                    Label lblNome = new Label(p.getNome());
                    lblNome.getStyleClass().add("produto-nome");

                    Label lblCod = new Label("Cód: " + p.getCodigo());
                    lblCod.getStyleClass().add("produto-codigo");

                    VBox box = new VBox(2, lblNome, lblCod);
                    setGraphic(box);
                }
            }
        });

        // 2. Coluna Estoque
        colEstoque.setCellValueFactory(data -> data.getValue().estoqueProperty());
        colEstoque.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Number item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.intValue() + " un.");
                }
            }
        });

        // 3. Coluna Preço de Venda
        colPrecoVenda.setCellValueFactory(data -> data.getValue().precoVendaProperty());
        colPrecoVenda.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Number item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(MOEDA_BR.format(item.doubleValue()));
                }
            }
        });

        // 4. Coluna Situação (Estoque normal, baixo ou sem estoque)
        colSituacao.setCellValueFactory(data -> data.getValue().situacaoProperty());
        colSituacao.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String situacao, boolean empty) {
                super.updateItem(situacao, empty);
                if (empty || situacao == null) {
                    setGraphic(null);
                } else {
                    Label badge = new Label(situacao);
                    badge.getStyleClass().add("status-pill");

                    switch (situacao) {
                        case "Sem estoque" -> badge.getStyleClass().add("status-zerado");
                        case "Estoque baixo" -> badge.getStyleClass().add("status-baixo");
                        default -> badge.getStyleClass().add("status-normal");
                    }
                    setGraphic(badge);
                }
            }
        });

        // 5. Coluna Ações (Botão reticências "..." abrindo menu Editar / Remover)
        colAcoes.setCellFactory(col -> new TableCell<>() {
            private final Button btnDots = new Button("...");
            private final ContextMenu menuAcoes = new ContextMenu();

            {
                btnDots.getStyleClass().add("action-dots-btn");

                MenuItem itemEditar = new MenuItem("Editar produto");
                itemEditar.getStyleClass().add("menu-item-edit");
                itemEditar.setOnAction(e -> {
                    LojaService.Produto p = getTableRow().getItem();
                    if (p != null) onEditarProduto(p);
                });

                MenuItem itemRemover = new MenuItem("Remover produto");
                itemRemover.getStyleClass().add("menu-item-danger");
                itemRemover.setOnAction(e -> {
                    LojaService.Produto p = getTableRow().getItem();
                    if (p != null) onRemoverProduto(p);
                });

                menuAcoes.getItems().addAll(itemEditar, itemRemover);

                btnDots.setOnAction(e -> {
                    if (menuAcoes.isShowing()) {
                        menuAcoes.hide();
                    } else {
                        menuAcoes.show(btnDots, Side.BOTTOM, -110, 4);
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                } else {
                    HBox box = new HBox(btnDots);
                    box.setAlignment(Pos.CENTER);
                    setGraphic(box);
                }
            }
        });
    }

    private void configurarFiltrosEOrdenacao() {
        listaFiltrada = new FilteredList<>(listaMestra, p -> true);

        txtPesquisa.textProperty().addListener((obs, oldVal, newVal) -> {
            String termo = (newVal == null) ? "" : newVal.trim().toLowerCase();
            listaFiltrada.setPredicate(produto -> {
                if (termo.isEmpty()) return true;
                return produto.getNome().toLowerCase().contains(termo)
                        || produto.getCodigo().toLowerCase().contains(termo);
            });
        });

        listaOrdenada = new SortedList<>(listaFiltrada);
        aplicarCriterioOrdenacao(cmbOrdenacao.getValue());

        cmbOrdenacao.valueProperty().addListener((obs, oldVal, newVal) -> aplicarCriterioOrdenacao(newVal));

        tblProdutos.setItems(listaOrdenada);
    }

    private void aplicarCriterioOrdenacao(String criterio) {
        if (criterio == null) return;

        Comparator<LojaService.Produto> comparator = switch (criterio) {
            case "Nome (Z → A)" -> Comparator.comparing(LojaService.Produto::getNome, String.CASE_INSENSITIVE_ORDER).reversed();
            case "Estoque (Menor → Maior)" -> Comparator.comparingInt(LojaService.Produto::getEstoque);
            case "Estoque (Maior → Menor)" -> Comparator.comparingInt(LojaService.Produto::getEstoque).reversed();
            case "Preço (Menor → Maior)" -> Comparator.comparingDouble(LojaService.Produto::getPrecoVenda);
            case "Preço (Maior → Menor)" -> Comparator.comparingDouble(LojaService.Produto::getPrecoVenda).reversed();
            default -> Comparator.comparing(LojaService.Produto::getNome, String.CASE_INSENSITIVE_ORDER);
        };

        listaOrdenada.setComparator(comparator);
    }

    // =========================================================================
    // AÇÕES (NOVO, EDITAR E REMOVER)
    // =========================================================================
    @FXML
    private void onNovoProduto(ActionEvent event) {
        Alert info = new Alert(Alert.AlertType.INFORMATION);
        info.setTitle("Novo Produto");
        info.setHeaderText("Cadastro de Produto (UC004)");
        info.setContentText("Aqui abriremos o modal/formulário de cadastro de novo produto.");
        info.showAndWait();
    }

    private void onEditarProduto(LojaService.Produto produto) {
        Alert info = new Alert(Alert.AlertType.INFORMATION);
        info.setTitle("Editar Produto");
        info.setHeaderText("Editando: " + produto.getNome() + " (" + produto.getCodigo() + ")");
        info.setContentText("Aqui abriremos o formulário preenchido com os dados deste produto (UC005).");
        info.showAndWait();
    }

    private void onRemoverProduto(LojaService.Produto produto) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Remover Produto");
        confirm.setHeaderText("Deseja remover \"" + produto.getNome() + "\"?");
        confirm.setContentText("Esta ação removerá o item do catálogo (UC006).");

        Optional<ButtonType> resultado = confirm.showAndWait();
        if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
            LojaService.getInstance().removerProduto(produto);
        }
    }

}