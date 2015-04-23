package br.gov.batch.util;

import java.util.Properties;

import javax.batch.api.Batchlet;
import javax.batch.operations.JobOperator;
import javax.batch.runtime.BatchRuntime;
import javax.ejb.EJB;
import javax.inject.Inject;
import javax.inject.Named;

import org.jboss.logging.Logger;

import br.gov.batch.gerardadosleitura.ControleExecucaoAtividade;
import br.gov.batch.servicos.batch.ProcessoBatchBO;
import br.gov.batch.to.ControleAtividadeTO;
import br.gov.model.batch.ProcessoIniciado;
import br.gov.model.batch.ProcessoParametro;
import static br.gov.model.batch.ProcessoParametro.NOMES.ATIVIDADE_INICIADA;
import br.gov.servicos.batch.ProcessoIniciadoRepositorio;

@Named
public class FinalizarAtividadeJob implements Batchlet{
	
    private static Logger logger = Logger.getLogger(IniciarAtividadeJob.class);
    
    @Inject
    private BatchUtil util;
    
    @EJB
    private ControleExecucaoAtividade controle;
    
    @EJB
    private ProcessoIniciadoRepositorio iniciadoRepositorio;
    
    @EJB
    private ProcessoBatchBO processoBO;
    
	public String process() throws Exception {
	    logger.info("Finalizando JOB");
	    
        Integer idProcessoIniciado = Integer.valueOf(util.parametroDoJob("idProcessoIniciado"));
        
        ProcessoIniciado iniciado = iniciadoRepositorio.obterPorID(idProcessoIniciado);
        
        if (iniciado.emProcessamento()){
            Integer idControleAtividade = Integer.valueOf(util.parametroDoJob("idControleAtividade"));
            
            while (!controle.execucaoConcluida(idControleAtividade)){
                Thread.sleep(1000);
                logger.info("Ainda nao terminou");
            }
            
            logger.info("TERMINOU!!!!!");
            
            String[]  ids =  util.parametroDoJob("idsRota").replaceAll("\"", "").split(",");
            
            if (processoBO.continuaExecucao(util.parametroDoJob(ATIVIDADE_INICIADA.toString()), idProcessoIniciado)){
                
            }
            
            ControleAtividadeTO to = processoBO.iniciarProximaAtividadeBatch(idProcessoIniciado, idControleAtividade, ids.length);
            
            if (to != null){
                Properties properties = util.parametrosDoJob();
                properties.put("idControleAtividade", String.valueOf(to.getIdControleAtividade()));
                
                JobOperator jo = BatchRuntime.getJobOperator();
                jo.start(to.getNomeArquivoBatch(), properties);
            }
        }
	    
		return null;
	}

	public void stop() throws Exception {
		
	}
}
