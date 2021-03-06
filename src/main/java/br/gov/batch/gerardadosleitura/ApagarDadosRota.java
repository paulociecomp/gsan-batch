package br.gov.batch.gerardadosleitura;

import java.util.ArrayList;
import java.util.List;

import javax.batch.api.Batchlet;
import javax.batch.runtime.context.JobContext;
import javax.ejb.EJB;
import javax.inject.Inject;
import javax.inject.Named;

import br.gov.batch.BatchLogger;
import br.gov.batch.util.BatchUtil;
import br.gov.model.faturamento.DebitoCreditoSituacao;
import br.gov.model.faturamento.FaturamentoGrupo;
import br.gov.model.micromedicao.ArquivoTextoRoteiroEmpresa;
import br.gov.model.micromedicao.Rota;
import br.gov.servicos.arrecadacao.DebitoAutomaticoMovimentoRepositorio;
import br.gov.servicos.cadastro.ClienteContaRepositorio;
import br.gov.servicos.cobranca.CobrancaDocumentoItemRepositorio;
import br.gov.servicos.cobranca.parcelamento.ParcelamentoItemRepositorio;
import br.gov.servicos.faturamento.ContaCategoriaConsumoFaixaRepositorio;
import br.gov.servicos.faturamento.ContaCategoriaRepositorio;
import br.gov.servicos.faturamento.ContaGeralRepositorio;
import br.gov.servicos.faturamento.ContaImpostosDeduzidosRepositorio;
import br.gov.servicos.faturamento.ContaImpressaoRepositorio;
import br.gov.servicos.faturamento.ContaRepositorio;
import br.gov.servicos.faturamento.CreditoRealizadoCategoriaRepositorio;
import br.gov.servicos.faturamento.CreditoRealizadoRepositorio;
import br.gov.servicos.faturamento.CreditoRealizarRepositorio;
import br.gov.servicos.faturamento.DebitoCobradoCategoriaRepositorio;
import br.gov.servicos.faturamento.DebitoCobradoRepositorio;
import br.gov.servicos.faturamento.DebitoCobrarRepositorio;
import br.gov.servicos.micromedicao.ArquivoTextoRoteiroEmpresaDivisaoRepositorio;
import br.gov.servicos.micromedicao.ArquivoTextoRoteiroEmpresaRepositorio;
import br.gov.servicos.micromedicao.RotaRepositorio;

@Named
public class ApagarDadosRota implements Batchlet{
	
	@EJB
	private BatchLogger logger;
	
	@EJB
	private ClienteContaRepositorio clienteContaRepositorio;

	@EJB
	private ContaImpostosDeduzidosRepositorio contaImpostosDeduzidosRepositorio;
	
	@EJB
	private DebitoAutomaticoMovimentoRepositorio debitoAutomaticoMovimentoRepositorio;
	
	@EJB
	private DebitoCobradoCategoriaRepositorio debitoCobradoCategoriaRepositorio;
	
	@EJB
	private DebitoCobradoRepositorio debitoCobradoRepositorio;
	
	@EJB
	private CreditoRealizadoCategoriaRepositorio creditoRealizadoCategoriaRepositorio;
	
	@EJB
	private CreditoRealizadoRepositorio creditoRealizadoRepositorio;
	
	@EJB
	private DebitoCobrarRepositorio debitoCobrarRepositorio;
	
	@EJB
	private CreditoRealizarRepositorio creditoRealizarRepositorio;
	
	@EJB
	private ContaGeralRepositorio contaGeralRepositorio;
	
	@EJB
	private ParcelamentoItemRepositorio parcelamentoItemRepositorio;
	
	@EJB
	private CobrancaDocumentoItemRepositorio cobrancaDocumentoItemRepositorio;
	
	@EJB
	private ContaRepositorio contaRepositorio;
	
	@EJB
	private ContaCategoriaConsumoFaixaRepositorio contaCategoriaConsumoFaixaRepositorio;
	
	@EJB
	private ContaCategoriaRepositorio contaCategoriaRepositorio;
	
	@EJB
	private ContaImpressaoRepositorio contaImpressaoRepositorio;
	
	@EJB
	private ArquivoTextoRoteiroEmpresaRepositorio arquivoRepositorio;
	
	@EJB
	private ArquivoTextoRoteiroEmpresaDivisaoRepositorio arquivoDivisaoRepositorio; 

	@EJB
	private RotaRepositorio rotaRepositorio;
	
    @Inject
    private BatchUtil util;
    
	@Inject
    protected JobContext jobCtx;
	
	public String process() throws Exception {
    	Integer idRota = Integer.valueOf(util.parametroDoJob("idRota"));
    	Integer referencia = Integer.valueOf(util.parametroDoJob("anoMesFaturamento"));
    	Integer grupoFaturamento = Integer.valueOf(util.parametroDoJob("idGrupoFaturamento"));
    	
    	logger.info(util.parametroDoJob("idProcessoIniciado"), "Exclusao de dados prefaturados para a rota: " + idRota);
    	
    	Rota rota = rotaRepositorio.obterPorID(idRota);
    	
    	this.apagarArquivoDaRota(rota, grupoFaturamento, referencia);
    	
    	List<Integer> idsContas  = new ArrayList<Integer>();
    	List<Integer> idsImoveis = new ArrayList<Integer>();
    	
    	if (rota.isAlternativa()){
    		idsContas = contaRepositorio.idsContasDeImovelComRotaAlternativa(idRota, referencia, DebitoCreditoSituacao.PRE_FATURADA.getId(), grupoFaturamento);
    		idsImoveis = contaRepositorio.imoveisDeContasComRotaAlternativa(idRota, referencia, DebitoCreditoSituacao.PRE_FATURADA.getId(), grupoFaturamento);
    	}else{
    		idsContas = contaRepositorio.idsContasDeImovelSemRotaAlternativa(idRota, referencia, DebitoCreditoSituacao.PRE_FATURADA.getId(), grupoFaturamento);
    		idsImoveis = contaRepositorio.imoveisDeContasSemRotaAlternativa(idRota, referencia, DebitoCreditoSituacao.PRE_FATURADA.getId(), grupoFaturamento);
    	}
    	
    	if (idsContas.size() > 0){
    		contaImpressaoRepositorio.apagarImpressaoDasContas(idsContas);
    		contaCategoriaConsumoFaixaRepositorio.apagarCategoriaConsumoFaixaDasContas(idsContas);
    		contaCategoriaRepositorio.apagarCategoriaDasContas(idsContas);
    		clienteContaRepositorio.apagarClientesConta(idsContas);
    		contaImpostosDeduzidosRepositorio.apagarImpostosDeduzidosDeContas(idsContas);
    		debitoAutomaticoMovimentoRepositorio.apagarMovimentosDebitoAutomaticoDasConta(idsContas);
    		
    		List<Integer> idsDebitosCobrados = debitoCobradoRepositorio.debitosCobradosDasContas(idsContas);
    		debitoCobradoCategoriaRepositorio.apagarCategoriasdosDebitosCobrados(idsDebitosCobrados);
    		debitoCobradoRepositorio.apagarDebitosCobradosDasContas(idsContas);
    		
    		List<Integer> idsCreditosRealizados = creditoRealizadoRepositorio.creditosRealizadosDasContas(idsContas);
    		creditoRealizadoCategoriaRepositorio.apagarCategoriasDosCreditosRealizados(idsCreditosRealizados);
    		creditoRealizadoRepositorio.apagarCreditosRealizadosDasContas(idsContas);
    		
    		debitoCobrarRepositorio.reduzirParcelasCobradas(referencia, grupoFaturamento, idsImoveis);
    		creditoRealizarRepositorio.atualizarParcelas(referencia, idsImoveis);
    		creditoRealizarRepositorio.atualizarValorResidual(idsImoveis);
    		contaGeralRepositorio.alterarHistoricoParaContasDeletadasPorReprocessamento(idsContas);
    		parcelamentoItemRepositorio.eliminarParcelamentosDeContas(idsContas);
    		cobrancaDocumentoItemRepositorio.apagarItensCobrancaDasContas(idsContas);
    		contaRepositorio.apagar(idsContas);
    	}
		
		return null;
	}
	
	private void apagarArquivoDaRota(Rota rota, Integer idGrupoFaturamento, Integer referencia) {
		FaturamentoGrupo grupo = new FaturamentoGrupo(idGrupoFaturamento);
		grupo.setAnoMesReferencia(referencia);
		rota.setFaturamentoGrupo(grupo);
		
		ArquivoTextoRoteiroEmpresa arquivo = arquivoRepositorio.pesquisarPorRotaEReferencia(rota.getId(), rota.getFaturamentoGrupo().getAnoMesReferencia());
		
		if (arquivo != null) {
			arquivoRepositorio.excluir(arquivo.getId());
			arquivoDivisaoRepositorio.deletarPorArquivoTextoRoteiroEmpresa(arquivo.getId());
		}
	}

	public void stop() throws Exception {
		
	}
}
