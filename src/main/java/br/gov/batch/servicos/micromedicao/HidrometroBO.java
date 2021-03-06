package br.gov.batch.servicos.micromedicao;

import java.util.Date;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import br.gov.batch.servicos.cadastro.ImovelBO;
import br.gov.model.faturamento.FaturamentoGrupo;
import br.gov.model.micromedicao.MedicaoHistorico;
import br.gov.model.util.Utilitarios;
import br.gov.servicos.micromedicao.HidrometroInstalacaoHistoricoRepositorio;
import br.gov.servicos.micromedicao.MedicaoHistoricoRepositorio;
import br.gov.servicos.to.HidrometroTO;

@Stateless
public class HidrometroBO {

	@EJB
	private ImovelBO imovelBO;
	
	@EJB
	private MedicaoHistoricoBO medicaoHistoricoBO;

	@EJB
	private MedicaoHistoricoRepositorio medicaoHistoricoRepositorio;

	@EJB
	private HidrometroInstalacaoHistoricoRepositorio hidrometroInstalacaoHistoricoRepositorio;

	public boolean houveInstalacaoOuSubstituicao(Integer idImovel) {
		Date dataLeituraAnteriorFaturada = this.obterDataLeituraAnterior(idImovel);
		Date dataInstalacaoHidrometro = this.obterDataInstalacaoHidrometro(idImovel);

		if (dataLeituraAnteriorFaturada != null && dataInstalacaoHidrometro != null) {
			if (dataInstalacaoHidrometro.before(new Date()) && !dataLeituraAnteriorFaturada.after(dataInstalacaoHidrometro)) {
				return true;
			}
		}

		return false;
	}

	private Date obterDataLeituraAnterior(Integer idImovel) {
		Date dataLeitura = null;

		FaturamentoGrupo faturamentoGrupo = imovelBO.pesquisarFaturamentoGrupo(idImovel);

		Integer anoMesReferencia = faturamentoGrupo.getAnoMesReferencia();

		MedicaoHistorico medicaoAtual = medicaoHistoricoRepositorio.buscarPorLigacaoAgua(idImovel, anoMesReferencia);

		if (medicaoAtual != null) {
			dataLeitura = medicaoAtual.getDataLeituraAnteriorFaturamento();
		} else {
			dataLeitura = this.obterDataMedicao(idImovel, Utilitarios.reduzirMeses(anoMesReferencia, 1));
		}

		return dataLeitura;
	}

	//TODO: Pode trocar para recuperar de agua primeiro e depois de poço?
	private Date obterDataInstalacaoHidrometro(Integer idImovel) {
		HidrometroTO instalacao = hidrometroInstalacaoHistoricoRepositorio.dadosInstalacaoHidrometroAgua(idImovel);
		
		if (instalacao == null) {
		    instalacao = hidrometroInstalacaoHistoricoRepositorio.dadosInstalacaoHidrometroPoco(idImovel);
		}

		return instalacao != null ? instalacao.getDataInstalacao() : null;
	}

	private Date obterDataMedicao(Integer idImovel, Integer anoMesReferencia) {
	    MedicaoHistorico medicao = medicaoHistoricoBO.getMedicaoHistorico(idImovel, anoMesReferencia);
	    
	    Date dataMedicao = null;
	    if (medicao != null) {
	        dataMedicao = medicao.getDataLeituraAtualFaturamento();
	    }else{
	        dataMedicao = this.obterDataInstalacaoHidrometro(idImovel);
	    }

		return dataMedicao;
	}

}