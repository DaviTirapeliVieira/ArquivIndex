package controller;

/**
 * Os critérios pelos quais a listagem de arquivos pode ser ordenada.
 * Fica no Controller (não no DAO) porque ordenação é regra de
 * apresentação, não de acesso a dados — ver §7.2 da especificação.
 */
public enum CriterioOrdenacao {
    NOME,
    TIPO,
    DATA
}
