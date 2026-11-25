package br.com.bestand;

import org.junit.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class LojaServiceTest {

    @Test
    public void saleAndPartialReturnUpdateStockAndFinancialSummary() {
        LojaService loja = new LojaService();
        LojaService.Produto produto = loja.getProdutos().get(0);

        LojaService.Venda venda = loja.registrarVenda(produto, 2);
        LojaService.Devolucao devolucao = loja.registrarDevolucao(venda, 1);
        LojaService.ResumoFinanceiro resumo = loja.calcularResumo(LocalDate.now(), LocalDate.now());

        assertEquals(33, produto.getEstoque());
        assertEquals(new BigDecimal("579.80"), venda.getTotal());
        assertEquals(new BigDecimal("289.90"), devolucao.getValor());
        assertEquals(new BigDecimal("579.80"), resumo.getVendas());
        assertEquals(new BigDecimal("289.90"), resumo.getDevolucoes());
        assertEquals(new BigDecimal("289.90"), resumo.getSaldo());
        assertEquals("Devolução parcial", venda.situacaoProperty().get());
    }

    @Test
    public void loadingAddsStockAndCountsAsCost() {
        LojaService loja = new LojaService();
        LojaService.Produto produto = loja.getProdutos().get(0);

        loja.registrarCarregamento("Fornecedor teste", produto, 5, new BigDecimal("12.50"));
        LojaService.ResumoFinanceiro resumo = loja.calcularResumo(LocalDate.now(), LocalDate.now());

        assertEquals(39, produto.getEstoque());
        assertEquals(new BigDecimal("62.50"), resumo.getCarregamentos());
        assertEquals(new BigDecimal("-62.50"), resumo.getSaldo());
        assertEquals(1, resumo.getQuantidadeCarregamentos());
    }

    @Test
    public void invalidSaleAndReturnDoNotChangeStockOrHistory() {
        LojaService loja = new LojaService();
        LojaService.Produto produto = loja.getProdutos().get(0);
        LojaService.Venda venda = loja.registrarVenda(produto, 1);
        int estoqueAposVenda = produto.getEstoque();

        try {
            loja.registrarVenda(produto, 1000);
            fail("A venda não deve exceder o estoque.");
        } catch (IllegalArgumentException esperado) {
            assertEquals(1, loja.getVendas().size());
        }

        try {
            loja.registrarDevolucao(venda, 2);
            fail("A devolução não deve exceder a quantidade vendida.");
        } catch (IllegalArgumentException esperado) {
            assertEquals(0, loja.getDevolucoes().size());
        }

        assertEquals(estoqueAposVenda, produto.getEstoque());
        assertEquals(1, loja.getVendas().size());
        assertEquals(0, loja.getDevolucoes().size());
    }
}
