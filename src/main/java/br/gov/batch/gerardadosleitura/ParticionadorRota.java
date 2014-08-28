package br.gov.batch.gerardadosleitura;

import javax.inject.Inject;
import javax.inject.Named;

import br.gov.batch.Particao;
import br.gov.batch.util.BatchUtil;

@Named
public class ParticionadorRota extends Particao {

    @Inject
    private BatchUtil util;
	
    public int totalItens(){
    	int idRota = Integer.valueOf(util.parametroDoBatch("idRota"));
    	return (int) rotaBO.totalImoveisParaPreFaturamento(idRota);
    }
}