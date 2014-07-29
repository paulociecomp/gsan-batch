package br.gov.batch.gerardadosleitura;

import java.util.Calendar;

import javax.batch.api.chunk.ItemProcessor;
import javax.ejb.EJB;
import javax.inject.Inject;
import javax.inject.Named;

import org.jboss.logging.Logger;

import br.gov.batch.servicos.faturamento.FaturamentoImovelBO;
import br.gov.batch.servicos.faturamento.FaturamentoImovelTO;
import br.gov.batch.util.BatchUtil;
import br.gov.model.cadastro.Imovel;
import br.gov.model.faturamento.FaturamentoGrupo;
import br.gov.model.micromedicao.Rota;
import br.gov.servicos.faturamento.FaturamentoAtividadeCronRotaRepositorio;
import br.gov.servicos.to.CronogramaFaturamentoRotaTO;

@Named
public class GerarConta implements ItemProcessor {
	private static Logger logger = Logger.getLogger(GerarConta.class);
	
	@EJB
	private FaturamentoImovelBO faturamentoImovelBO;
	
	@EJB
	private FaturamentoAtividadeCronRotaRepositorio faturamentoAtividadeCronRotaRepositorio;
	
    @Inject
    private BatchUtil util;
    
	public GerarConta() {
	}

    public Imovel processItem(Object param) throws Exception {
    	Imovel imovel = (Imovel) param;
    	Integer idRota             = Integer.valueOf(util.parametroDoBatch("idRota"));
    	Integer idGrupoFaturamento = Integer.valueOf(util.parametroDoBatch("idGrupoFaturamento"));
    	Integer anoMesFaturamento  = Integer.valueOf(util.parametroDoBatch("anoMesFaturamento"));
    	
    	CronogramaFaturamentoRotaTO cronogramaFaturamentoRotaTO = faturamentoAtividadeCronRotaRepositorio.pesquisaFaturamentoAtividadeCronogramaRota(idRota, idGrupoFaturamento, anoMesFaturamento);
    	
    	Rota rota = new Rota();
    	rota.setId(idRota);
    	
    	FaturamentoGrupo faturamentoGrupo = new FaturamentoGrupo();
    	faturamentoGrupo.setId(idGrupoFaturamento);
    	
    	FaturamentoImovelTO to = new FaturamentoImovelTO();
    	to.setRota(rota);
    	to.setImovel(imovel);
    	to.setFaturamentoGrupo(faturamentoGrupo);
    	to.setAnoMesFaturamento(anoMesFaturamento);
    	to.setDataVencimentoConta(cronogramaFaturamentoRotaTO.getDataVencimentoConta());
    	to.setGerarAtividadeGrupoFaturamento(true);
    	
		faturamentoImovelBO.preDeterminarFaturamentoImovel(to);
    	
        return imovel;
    }
}