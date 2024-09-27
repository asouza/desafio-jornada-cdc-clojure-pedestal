(ns detalhe-livro
  (:require
    [utilitarios]
    [validacoes-conversoes]
    ))


;;como saber quais são as propriedades do livro? Preciso organizar os records.
(defn converte-livro-saida [livro]
    (assoc (select-keys livro [:titulo :resumo :isbn :preco]) :data-lancamento (validacoes-conversoes/string-date "yyyy-MM-dd" (:data-lancamento livro)))
  )

(def handler
        {
          :name :detalhe-livro
          :enter (fn [context]
                    (let [id-livro (get-in context [:request :path-params :id])
                          banco-dados (get-in context [:request :database])
                         livro (utilitarios/busca-item-por-campo banco-dados :livros :id id-livro)
                          ]
                      (if livro
                        (do
                          (utilitarios/respond-with-json context (converte-livro-saida livro))
                          )
                          (utilitarios/respond-with-status context 404)
                        )
                      )
                   )

          }
  )
