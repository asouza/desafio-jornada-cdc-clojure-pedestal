(ns lista-livros
  (:require
    [utilitarios]
    [datomic.api :as d]
    )
  )

(defn- converte-livro-saida [livro]
  {
   :titulo (livro 1)
   :id (livro 0)
   }
  )

(defn- busca-livros [dados]
  (let [
        query '[:find ?e ?titulo
                :where
                [?e :livro/titulo ?titulo]
                ]

        ]
    (d/q query dados)
    )
  )

(def handler
  {
   :name :lista-livros
   :enter (fn [context]
            (let [db (get-in context [:request :db])
                  lista-livros (busca-livros db)
                  ]
              (utilitarios/respond-with-json context (map converte-livro-saida lista-livros))
              ))
   }
  )
