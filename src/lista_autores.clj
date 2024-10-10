(ns lista-autores
  (:require
    [utilitarios]
    [datomic.api :as d]
    )
  )

;chatgpt me disse que eu poderia ter explodido aqui nas tres variáveis que eu criei.
;Ao mesmo tempo disse que meu código está ok
(defn- converte-linha-autor-saida-lista [autor]
  {
   :nome (autor 0)
   :email (autor 1)
   :descricao (autor 2)
   }
  )

(defn- busca-autores [dados]
  (let [
        query '[:find ?nome ?email ?descricao
                :where
                  [?e :autor/email ?email]
                  [?e :autor/nome ?nome]
                  [?e :autor/descricao ?descricao]
                ]

        ]
    (d/q query dados)
    )
  )

(def handler
  {
   :name :lista-autores
   :enter (fn [context]
            (let [
                  dados (get-in context [:request :db])
                  autores (busca-autores dados)
                  ]
              (utilitarios/respond-with-json context (map converte-linha-autor-saida-lista autores))
              ))
   }
  )
