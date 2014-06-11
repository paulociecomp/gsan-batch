package br.gov.cadastro.servicos;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import javax.ejb.Stateless;

import br.gov.model.cadastro.Categoria;

@Stateless
public class CategoriaBO {

	public Collection<BigDecimal> obterValorPorCategoria(Collection<Categoria> colecaoCategorias, BigDecimal valor) {

		Collection<BigDecimal> colecaoValoresPorCategoria = new ArrayList<BigDecimal>();

		//acuama a quantidae de ecnomias das acategorias
		int somatorioQuantidadeEconomiasCadaCategoria = 0;
		if (colecaoCategorias != null && !colecaoCategorias.isEmpty()) {
			Iterator<Categoria> iteratorColecaoCategorias = colecaoCategorias.iterator();

			while (iteratorColecaoCategorias.hasNext()) {
				Categoria categoria = (Categoria) iteratorColecaoCategorias.next();
				somatorioQuantidadeEconomiasCadaCategoria = somatorioQuantidadeEconomiasCadaCategoria + categoria.getQuantidadeEconomiasCategoria().intValue();
			}
		}

		//	 calcula o fator de multiplicação
		BigDecimal fatorMultiplicacao = valor.divide(new BigDecimal(somatorioQuantidadeEconomiasCadaCategoria),2,BigDecimal.ROUND_DOWN);

		BigDecimal valorPorCategoriaAcumulado = new BigDecimal(0);


		//	 para cada categoria, calcula o Valor por Cageoria
		if (colecaoCategorias != null && !colecaoCategorias.isEmpty()) {
			Iterator<Categoria> iteratorColecaoCategorias = colecaoCategorias.iterator();

			while (iteratorColecaoCategorias.hasNext()) {
				Categoria categoria = (Categoria) iteratorColecaoCategorias.next();

				BigDecimal valorPorCategoria = new BigDecimal(0);

				valorPorCategoria = fatorMultiplicacao.multiply(new BigDecimal(categoria.getQuantidadeEconomiasCategoria()));

				BigDecimal valorTruncado = valorPorCategoria.setScale(2, BigDecimal.ROUND_DOWN);

				valorPorCategoriaAcumulado = valorPorCategoriaAcumulado.add(valorTruncado);

				colecaoValoresPorCategoria.add(valorTruncado);
			}
		}

		valorPorCategoriaAcumulado = valorPorCategoriaAcumulado.setScale(7);

		// caso o valor por categoria acumulado seja menor que o valor
		// acuma a diferença no valor por cageoria da primeira
		if (valorPorCategoriaAcumulado.setScale(2, BigDecimal.ROUND_HALF_UP).compareTo(valor.setScale(2, BigDecimal.ROUND_HALF_UP)) == -1) {

			BigDecimal diferenca = valor.subtract(valorPorCategoriaAcumulado);

			diferenca = diferenca.setScale(2, BigDecimal.ROUND_HALF_UP);

			BigDecimal categoriaPrimeira = (BigDecimal) colecaoValoresPorCategoria.iterator().next();

			categoriaPrimeira = categoriaPrimeira.add(diferenca);

			((ArrayList<BigDecimal>)colecaoValoresPorCategoria).set(0, categoriaPrimeira);

		}

		return colecaoValoresPorCategoria;
	}
}