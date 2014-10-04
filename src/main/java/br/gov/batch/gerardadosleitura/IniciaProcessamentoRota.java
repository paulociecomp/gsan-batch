package br.gov.batch.gerardadosleitura;

import java.util.Properties;

import javax.batch.api.chunk.ItemProcessor;
import javax.batch.operations.JobOperator;
import javax.batch.runtime.BatchRuntime;
import javax.batch.runtime.context.JobContext;
import javax.inject.Inject;
import javax.inject.Named;

import org.jboss.logging.Logger;

import br.gov.batch.util.BatchUtil;

@Named
public class IniciaProcessamentoRota implements ItemProcessor {
	private static Logger logger = Logger.getLogger(IniciaProcessamentoRota.class);
	
    @Inject
    private BatchUtil util;
    
    @Inject
    private ControleProcessoRota controle;
    
	@Inject
    protected JobContext jobCtx;
    
	public IniciaProcessamentoRota() {
	}

    public Object processItem(Object param) throws Exception {
        Properties processoParametros = new Properties();
        
        if (controle.jobAtivo(jobCtx.getExecutionId())){
        	processoParametros.put("idProcessoIniciado", util.parametroDoBatch("idProcessoIniciado"));
        	processoParametros.put("idRota", String.valueOf(param));
        	processoParametros.put("anoMesFaturamento" , util.parametroDoBatch("anoMesFaturamento"));
        	processoParametros.put("idGrupoFaturamento", util.parametroDoBatch("idGrupoFaturamento"));
        	processoParametros.put("parentExecutionId", String.valueOf(jobCtx.getExecutionId()));
        	
        	JobOperator jo = BatchRuntime.getJobOperator();
        	
        	Long executionRota = jo.start("job_processar_rota", processoParametros);
        	
        	logger.info(String.format("Rota [%s] marcada para processamento com executionId [%s]. ", param, executionRota));
        	
        	controle.iniciaProcessamentoRota();
        }
        
        
        return param;
    }
}