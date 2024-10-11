(ns detalhe-livro
  (:require
    [utilitarios]
    [validacoes-conversoes]
    [datomic.api :as d]
    ))


;;como saber quais são as propriedades do livro? Preciso organizar os records.
(defn converte-livro-saida [livro]
    {
     :titulo (:livro/titulo livro)
     :preco (:livro/preco livro)
     :resumo (:livro/resumo livro)
     :autor {
              :nome (get-in livro [:livro/id-autor :autor/nome])
             }
     :categoria {
             :nome (get-in livro [:livro/id-categoria :categoria/nome])
             }
     }

  )

(defn- busca-por-id [context id]
    (let [
            dados (get-in context [:request :db])
          ]
        (d/pull dados '[:livro/titulo :livro/resumo :livro/preco :livro/isbn :livro/data-lancamento
                   {:livro/id-autor [:autor/nome]}
                   {:livro/id-categoria [:categoria/nome]}]
              id)
      )
  )

(def handler
        {
          :name :detalhe-livro
          :enter (fn [context]
                    (let [id-livro (Long/parseLong (get-in context [:request :path-params :id]))
                          livro (busca-por-id context id-livro)
                          ]
                      (if livro
                          (utilitarios/respond-with-json context (converte-livro-saida livro))

                          (utilitarios/respond-with-status context 404)
                        )
                      )
                   )

          }
  )
