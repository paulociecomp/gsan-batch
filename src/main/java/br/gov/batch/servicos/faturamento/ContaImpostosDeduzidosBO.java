package br.gov.batch.servicos.faturamento;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import br.gov.model.faturamento.Conta;
import br.gov.model.faturamento.ContaImpostosDeduzidos;
import br.gov.model.faturamento.ImpostoTipo;
import br.gov.servicos.faturamento.ContaImpostosDeduzidosRepositorio;
import br.gov.servicos.to.ImpostoDeduzidoTO;
import br.gov.servicos.to.ImpostosDeduzidosContaTO;

@Stateless
public class ContaImpostosDeduzidosBO {

	@EJB
	private ContaImpostosDeduzidosRepositorio contaImpostosDeduzidosRepositorio;
	
	public void inserirContaImpostosDeduzidos(Conta conta, ImpostosDeduzidosContaTO impostosDeduzidosContaTO) {
		Collection<ImpostoDeduzidoTO> impostosDeduzidos = impostosDeduzidosContaTO.getListaImpostosDeduzidos();
		Collection<ContaImpostosDeduzidos> contasImpostosDeduzidos = new ArrayList<ContaImpostosDeduzidos>();
		
		if (impostosDeduzidos != null){
			for (ImpostoDeduzidoTO imposto : impostosDeduzidos) {
				ContaImpostosDeduzidos contaImpostosDeduzidos = new ContaImpostosDeduzidos();
				contaImpostosDeduzidos.setConta(conta);
				
				ImpostoTipo impostoTipo = new ImpostoTipo();
				impostoTipo.setId(imposto.getIdImpostoTipo());
				
				contaImpostosDeduzidos.setImpostoTipo(impostoTipo);
				contaImpostosDeduzidos.setValorImposto(imposto.getValor());
				contaImpostosDeduzidos.setPercentualAliquota(imposto.getPercentualAliquota());
				contaImpostosDeduzidos.setValorBaseCalculo(impostosDeduzidosContaTO.getValorBaseCalculo());
				contaImpostosDeduzidos.setUltimaAlteracao(new Date());
				
				contasImpostosDeduzidos.add(contaImpostosDeduzidos);
			}
		}

		if (contasImpostosDeduzidos != null && !contasImpostosDeduzidos.isEmpty()) {
			contaImpostosDeduzidosRepositorio.inserir(contasImpostosDeduzidos);
		}
	}
}
