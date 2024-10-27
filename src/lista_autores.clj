(ns lista-autores
  (:require
    [utilitarios]
    [datomic.api :as d]
   [io.pedestal.http :as http]
    )
  )

;chatgpt me disse que eu poderia ter explodido aqui nas tres variáveis que eu criei.
;Ao mesmo tempo disse que meu código está ok
(defn- converte-linha-autor-saida-lista [autor]
  {
   :id (autor 0)
   :nome (autor 1)
   :email (autor 2)
   :descricao (autor 3)
   }
  )

(defn- busca-autores [dados]
  (let [
        query '[:find ?e ?nome ?email ?descricao
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
              (http/respond-with context 200 (map converte-linha-autor-saida-lista autores)) 
              ))
   }
  )
