package br.gov.batch.util;

import javax.batch.api.listener.JobListener;
import javax.batch.runtime.BatchStatus;
import javax.batch.runtime.context.JobContext;
import javax.ejb.EJB;
import javax.inject.Inject;
import javax.inject.Named;

import br.gov.batch.BatchLogger;
import br.gov.batch.servicos.batch.ProcessoBatchBO;
import br.gov.model.batch.Processo;
import br.gov.model.batch.ProcessoSituacao;
import br.gov.servicos.batch.ProcessoRepositorio;

@Named
public class ErrorJobListener implements JobListener{
    @EJB
    protected BatchLogger logger;

    @Inject
    protected BatchUtil util;

    @EJB
    private ProcessoRepositorio repositorio;
    
    @EJB
    private ProcessoBatchBO processoBO;
    
    @Inject
    protected JobContext jobCtx;

    public void beforeJob() throws Exception {
        
    }

    public void afterJob() throws Exception {
        if (jobCtx.getBatchStatus() == BatchStatus.FAILED){
            
            Integer idProcessoIniciado = Integer.valueOf(util.parametroDoJob("idProcessoIniciado"));
            
            Processo processo = repositorio.obterProcessoPeloIniciado(idProcessoIniciado);
            
            repositorio.terminaExecucaoProcesso(idProcessoIniciado, ProcessoSituacao.CONCLUIDO_COM_ERRO);
            
            if (util.parametroDoJob("idControleAtividade") != null){
                Integer idControleAtividade = Integer.valueOf(util.parametroDoJob("idControleAtividade"));
                
                processoBO.finalizaAtividade(idControleAtividade, ProcessoSituacao.CONCLUIDO_COM_ERRO);
            }

            logger.error(util.parametroDoJob("idProcessoIniciado"), "Erro ao concluir processo: " + processo.getDescricao());
        }
    }
}
