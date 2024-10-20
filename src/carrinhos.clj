(ns carrinhos

  (:require
    [utilitarios]
    [datomic.api :as d]
    )
  )

;chatgpt me disse que eu poderia ter explodido aqui nas tres variáveis que eu criei.
;Ao mesmo tempo disse que meu código está ok
(defn- converte-carrinho-saida-lista [carrinho]
    (carrinho 0)
  )

(defn- busca-carrinhos [dados]
  (let [
        query '[:find ?e
                :where
                [?e :carrinho/id]
                ]

        ]
    (d/q query dados)
    )
  )




(def handler
  {
   :name :lista-carrinhos
   :enter (fn [context]
            (let [db (get-in context [:request :db])]
              (utilitarios/respond-with-json context (map converte-carrinho-saida-lista (busca-carrinhos db)))
              ))
   }
  )
