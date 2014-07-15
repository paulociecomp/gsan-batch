package br.gov.batch.servicos.faturamento;

import java.util.Collection;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import br.gov.model.Status;
import br.gov.model.atendimentopublico.LigacaoAguaSituacao;
import br.gov.model.atendimentopublico.LigacaoEsgotoSituacao;
import br.gov.model.cadastro.Imovel;
import br.gov.model.faturamento.CreditoRealizar;
import br.gov.model.faturamento.DebitoCobrar;
import br.gov.model.faturamento.DebitoCreditoSituacao;
import br.gov.servicos.arrecadacao.DevolucaoRepositorio;
import br.gov.servicos.arrecadacao.PagamentoRepositorio;
import br.gov.servicos.faturamento.CreditoRealizarRepositorio;
import br.gov.servicos.faturamento.DebitoCobrarRepositorio;

@Stateless
public class AnalisadorGeracaoConta {
	
	@EJB
	private CreditoRealizarRepositorio creditoRealizarRepositorio;
	
	@EJB
	private DebitoCobrarRepositorio debitoCobrarRepositorio;
	
	@EJB
	private PagamentoRepositorio pagamentoRepositorio;
	
	@EJB
	private DevolucaoRepositorio devolucaoRepositorio;
	
	public AnalisadorGeracaoConta(){}
	
	public boolean verificarNaoGeracaoConta(boolean valoresAguaEsgotoZerados, int anoMesFaturamento, Imovel imovel) throws Exception {
		return !(verificarSituacaoImovelParaGerarConta(valoresAguaEsgotoZerados, imovel) && verificarDebitosECreditosParaGerarConta(anoMesFaturamento, imovel));
	}

	public boolean verificarDebitosECreditosParaGerarConta(int anoMesFaturamento, Imovel imovel) {
		
		Collection<DebitoCobrar> debitosACobrar = debitoCobrarRepositorio.debitosCobrarPorImovelComPendenciaESemRevisao(imovel);
		if (naoHaDebitosACobrar(debitosACobrar) || paralisacaoFaturamento(imovel) || !pagamentoRepositorio.existeDebitoSemPagamento(debitosACobrar)) {
			return false;
		}
		
		boolean segundaCondicaoGerarConta = true;
		Collection<CreditoRealizar> creditosARealizar = creditoRealizarRepositorio.buscarCreditoRealizarPorImovel(imovel.getId(), 
																													DebitoCreditoSituacao.NORMAL, 
																													anoMesFaturamento);

		if (naoHaCreditoARealizar(creditosARealizar) || devolucaoRepositorio.existeCreditoComDevolucao(creditosARealizar)) {
			segundaCondicaoGerarConta = haDebitosCobrarAtivos(debitosACobrar);
		}

		return segundaCondicaoGerarConta;
	}

	public boolean verificarSituacaoImovelParaGerarConta(boolean valoresAguaEsgotoZerados, Imovel imovel) {
		return !valoresAguaEsgotoZerados && (aguaEsgotoLigados(imovel) || imovelPertenceACondominio(imovel));
	}

	private boolean haDebitosCobrarAtivos(Collection<DebitoCobrar> debitosACobrar) {
		boolean haDebitosCobrarAtivos = false;
		for (DebitoCobrar debitoACobrar: debitosACobrar) {
			if (debitoACobrar.getDebitoTipo().getIndicadorGeracaoConta() == Status.ATIVO) {
				haDebitosCobrarAtivos = true;
				break;
			}
		}
		return haDebitosCobrarAtivos;
	}

	private boolean naoHaCreditoARealizar(Collection<CreditoRealizar> creditosRealizar) {
		return creditosRealizar == null || creditosRealizar.isEmpty();
	}

	private boolean paralisacaoFaturamento(Imovel imovel) {
		return imovel.getFaturamentoSituacaoTipo() != null && imovel.getFaturamentoSituacaoTipo().getParalisacaoFaturamento() == Status.ATIVO;
	}

	private boolean naoHaDebitosACobrar(Collection<DebitoCobrar> colecaoDebitosACobrar) {
		return colecaoDebitosACobrar == null || colecaoDebitosACobrar.isEmpty();
	}

	private boolean imovelPertenceACondominio(Imovel imovel) {
		return imovel.getImovelCondominio() != null;
	}

	private boolean aguaEsgotoLigados(Imovel imovel) {
		return imovel.getLigacaoAguaSituacao() != null
				&& imovel.getLigacaoAguaSituacao().getId().equals(LigacaoAguaSituacao.LIGADO)
				&& imovel.getLigacaoEsgotoSituacao() != null
				&& imovel.getLigacaoEsgotoSituacao().getId().equals(LigacaoEsgotoSituacao.LIGADO);
	}		
}
