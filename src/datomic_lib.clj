(ns datomic-lib
  (:require
    [datomic.api :as d]
  )
  )

(defn busca-entidade
  "Retorna nil, caso não encontre"
  [dados atributo id]
  (d/pull dados [atributo] id)
  )

(defn busca-entidades-por-unico-atributo [dados atributo-match valor-buscado]
  (let [
        query '[:find ?e
                :in $ ?atributo-match ?valor-buscado
                :where
                [?e ?atributo-match ?valor-buscado]
                ]
        entidades (d/q query dados atributo-match valor-buscado)
        ]
    ;nao da para simplesmente retornar os livros, pq ele um hashset. Por mais que esteja vazio, não é nil.
    ;se não é nil, é true
    (seq entidades)

    )
  )
