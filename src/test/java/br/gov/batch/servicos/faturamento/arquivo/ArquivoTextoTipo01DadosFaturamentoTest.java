package br.gov.batch.servicos.faturamento.arquivo;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.easymock.EasyMockRunner;
import org.easymock.Mock;
import org.easymock.TestSubject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import br.gov.batch.servicos.faturamento.FaturamentoAtividadeCronogramaBO;
import br.gov.batch.servicos.faturamento.FaturamentoSituacaoBO;
import br.gov.model.Status;
import br.gov.model.atendimentopublico.LigacaoAguaSituacao;
import br.gov.model.atendimentopublico.LigacaoEsgotoSituacao;
import br.gov.model.cadastro.Categoria;
import br.gov.model.cadastro.ICategoria;
import br.gov.model.cadastro.Imovel;
import br.gov.model.cadastro.ImovelPerfil;
import br.gov.model.cadastro.Subcategoria;
import br.gov.model.faturamento.Conta;
import br.gov.model.faturamento.FaturamentoAtividade;
import br.gov.model.faturamento.FaturamentoGrupo;
import br.gov.servicos.cadastro.ImovelSubcategoriaRepositorio;

@RunWith(EasyMockRunner.class)
public class ArquivoTextoTipo01DadosFaturamentoTest {

	@TestSubject
	private ArquivoTextoTipo01DadosFaturamento arquivo;
	
	@Mock
    private FaturamentoSituacaoBO faturamentoSituacaoBOMock;
	
	@Mock
    private FaturamentoAtividadeCronogramaBO faturamentoAtividadeCronogramaBOMock;
	
	@Mock
    private ImovelSubcategoriaRepositorio imovelSubcategoriaRepositorioMock;
	
	private Imovel imovel;
	private FaturamentoGrupo faturamentoGrupo;
	private Conta conta;
	
	@Before
	public void init() {
		
		imovel = new Imovel(1234567);
    	imovel.setIndicadorImovelCondominio(Status.INATIVO.getId());
    	imovel.setImovelPerfil(new ImovelPerfil(1));
		
		faturamentoGrupo = new FaturamentoGrupo(1);
        faturamentoGrupo.setAnoMesReferencia(201501);
        
        LigacaoAguaSituacao ligacaoAguaSituacao = new LigacaoAguaSituacao(LigacaoAguaSituacao.LIGADO);
    	ligacaoAguaSituacao.setSituacaoFaturamento(Status.ATIVO.getId());
    	ligacaoAguaSituacao.setIndicadorAbastecimento(Status.ATIVO.getId());
    	imovel.setLigacaoAguaSituacao(ligacaoAguaSituacao);
    	
    	LigacaoEsgotoSituacao ligacaoEsgotoSituacao = new LigacaoEsgotoSituacao(LigacaoEsgotoSituacao.POTENCIAL);
    	ligacaoEsgotoSituacao.setSituacaoFaturamento(Status.INATIVO.getId());
    	imovel.setLigacaoEsgotoSituacao(ligacaoEsgotoSituacao);
    	
        conta = new Conta();
        conta.setDataVencimentoConta(new Date());
        conta.setDataValidadeConta(new Date());
        conta.setLigacaoAguaSituacao(ligacaoAguaSituacao);
        conta.setLigacaoEsgotoSituacao(ligacaoEsgotoSituacao);
        conta.setReferencia(201501);
        conta.setId(999999999);
        conta.setDigitoVerificadorConta(Short.valueOf("1"));
        conta.setFaturamentoGrupo(faturamentoGrupo);
        
        arquivo = new ArquivoTextoTipo01DadosFaturamento(imovel, conta);
	}
	
	private List<ICategoria> categoriasSetUp() {
    	Categoria categoria = new Categoria();
    	
    	categoria.setConsumoAlto(50);
    	categoria.setConsumoEstouro(50);
    	categoria.setNumeroConsumoMaximoEc(500);
    	categoria.setMediaBaixoConsumo(30);
    	categoria.setQuantidadeEconomias(1);
    	categoria.setVezesMediaAltoConsumo(new BigDecimal("2.0"));
    	categoria.setVezesMediaEstouro(new BigDecimal("3.0"));
    	categoria.setPorcentagemMediaBaixoConsumo(new BigDecimal("50.0"));
    	
    	List<ICategoria> categorias = new ArrayList<ICategoria>();
    	categorias.add(categoria);
    	
    	return categorias;
    }
    
    private List<ICategoria> subcategoriasSetUp() {
    	Subcategoria subcategoria = new Subcategoria();
    	
    	subcategoria.setIndicadorSazonalidade(Status.INATIVO.getId());

    	List<ICategoria> subcategorias = new ArrayList<ICategoria>();
    	subcategorias.add(subcategoria);
    	
    	return subcategorias;
    }
    
    @Test
    public void buildArquivoDadosCobranca() {
    	carregarMocks();
    	
    	Map<Integer, StringBuilder> mapDados = arquivo.build();
    	
    	String linha = getLinha(mapDados);
    	
    	
    	System.out.println(mapDados.get(8));
    	System.out.println(mapDados.get(10));
    	System.out.println(mapDados.get(14));
    	System.out.println(mapDados.get(36));
    	
    	System.out.println(mapDados.get(38));
    	System.out.println(mapDados.get(11));
    	System.out.println(mapDados.get(20));
    	System.out.println(mapDados.get(37));
    	
    	System.out.println(mapDados.get(39));
    	System.out.println(mapDados.get(40));
    	System.out.println(mapDados.get(41));
    	
    	assertNotNull(mapDados);
    	assertEquals(11, mapDados.keySet().size());
    	//assertEquals(linha, "                                     20150123131         20112");
    	assertEquals(linha, "                                     20150123123112         20112");
    }
    
    private String getLinha(Map<Integer, StringBuilder> mapDados) {
    	StringBuilder builder = new StringBuilder();
    	
    	Collection<StringBuilder> dados = mapDados.values();
    	
    	Iterator<StringBuilder> it = dados.iterator();
    	
    	while (it.hasNext()) {
    		builder.append(it.next());
    	}
    	
    	return builder.toString();
    }
    
	public void carregarMocks() {
    	List<ICategoria> categorias = categoriasSetUp(); 
    	List<ICategoria> subcategorias = subcategoriasSetUp();
    	
    	expect(imovelSubcategoriaRepositorioMock.buscarQuantidadeEconomiasCategoria(imovel.getId())).andReturn(categorias);
    	expect(imovelSubcategoriaRepositorioMock.buscarQuantidadeEconomiasSubcategoria(imovel.getId())).andReturn(subcategorias);
    	replay(imovelSubcategoriaRepositorioMock);
    	
    	expect(faturamentoAtividadeCronogramaBOMock.obterDataPrevistaDoCronogramaAnterior(faturamentoGrupo, FaturamentoAtividade.EFETUAR_LEITURA)).andReturn(new Date());
    	replay(faturamentoAtividadeCronogramaBOMock);
    	
    	expect(faturamentoSituacaoBOMock.verificarParalisacaoFaturamentoAgua(imovel, faturamentoGrupo.getAnoMesReferencia())).andReturn(Status.ATIVO);
    	expect(faturamentoSituacaoBOMock.verificarParalisacaoFaturamentoEsgoto(imovel, faturamentoGrupo.getAnoMesReferencia())).andReturn(Status.INATIVO);
    	replay(faturamentoSituacaoBOMock);
    }
}