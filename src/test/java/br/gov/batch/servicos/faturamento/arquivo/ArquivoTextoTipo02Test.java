package br.gov.batch.servicos.faturamento.arquivo;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collection;

import org.easymock.EasyMockRunner;
import org.easymock.Mock;
import org.easymock.TestSubject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import br.gov.batch.servicos.faturamento.to.ArquivoTextoTO;
import br.gov.model.cadastro.Categoria;
import br.gov.model.cadastro.ICategoria;
import br.gov.model.cadastro.Imovel;
import br.gov.model.cadastro.SistemaParametros;
import br.gov.model.cadastro.Subcategoria;
import br.gov.servicos.cadastro.ImovelSubcategoriaRepositorio;

@RunWith(EasyMockRunner.class)
public class ArquivoTextoTipo02Test {

	@TestSubject
	private ArquivoTextoTipo02 arquivo;
	
	private int TAMANHO_LINHA = 94;

	@Mock
	private ImovelSubcategoriaRepositorio imovelSubcategoriaRepositorioMock;

	@Mock
	private SistemaParametros sistemaParametrosMock;

	private ArquivoTextoTO to;
	private Collection<ICategoria> categorias;
	private Collection<ICategoria> subcategorias;

	@Before
	public void setup() {
		Categoria categoria = new Categoria();
		categoria.setId(1);
		categoria.setQuantidadeEconomias(1);
		categoria.setFatorEconomias((short) 1);
		categoria.setDescricao("CATEGORIA 1");
		categoria.setDescricaoAbreviada("CTG 1");

		Subcategoria subcategoria = new Subcategoria();
		subcategoria.setId(1);
		subcategoria.setQuantidadeEconomias(1);
		subcategoria.setDescricao("SUBCATEGORIA 1");
		subcategoria.setDescricaoAbreviada("SCTG 1");
		subcategoria.setCategoria(categoria);

		categorias = new ArrayList<ICategoria>();
		categorias.add(categoria);

		subcategorias = new ArrayList<ICategoria>();
		subcategorias.add(subcategoria);

		arquivo = new ArquivoTextoTipo02();
		to = new ArquivoTextoTO();
		to.setImovel(new Imovel(1));
	}

	@Test
	public void buildArquivoTextoTipo02Categoria() {
		expect(sistemaParametrosMock.indicadorTarifaCategoria()).andStubReturn(true);
		replay(sistemaParametrosMock);
		expect(imovelSubcategoriaRepositorioMock.buscarQuantidadeEconomiasPorImovel(1)).andStubReturn(categorias);
		replay(imovelSubcategoriaRepositorioMock);

		assertNotNull(arquivo.build(to));
	}

	@Test
	public void buildArquivoTextoTipo02Subcategoria() {
		expect(sistemaParametrosMock.indicadorTarifaCategoria()).andStubReturn(false);
		replay(sistemaParametrosMock);
		expect(imovelSubcategoriaRepositorioMock.buscarQuantidadeEconomiasPorImovel(1)).andStubReturn(subcategorias);
		replay(imovelSubcategoriaRepositorioMock);

		assertNotNull(arquivo.build(to));
	}

	@Test
	public void buildArquivoTextoTipo02CategoriaTamanhoLinha() {
		expect(sistemaParametrosMock.indicadorTarifaCategoria()).andStubReturn(true);
		replay(sistemaParametrosMock);
		expect(imovelSubcategoriaRepositorioMock.buscarQuantidadeEconomiasPorImovel(1)).andStubReturn(categorias);
		replay(imovelSubcategoriaRepositorioMock);

		String linha = arquivo.build(to);
		assertTrue(linha.length() >= TAMANHO_LINHA);
	}

	@Test
	public void buildArquivoTextoTipo02SubcategoriaTamanhoLinha() {
		expect(sistemaParametrosMock.indicadorTarifaCategoria()).andStubReturn(false);
		replay(sistemaParametrosMock);
		expect(imovelSubcategoriaRepositorioMock.buscarQuantidadeEconomiasPorImovel(1)).andStubReturn(subcategorias);
		replay(imovelSubcategoriaRepositorioMock);

		String linha = arquivo.build(to);
		assertTrue(linha.length() >= linha.length());
	}
}