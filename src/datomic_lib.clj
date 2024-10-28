(ns datomic-lib
  (:require
    [datomic.api :as d]
  )
  )

(defn busca-entidade
  "Retorna nil, caso não encontre"

  ;aqui eu vou trocar para usar a api de query. Aí vou ter que explorar mais o datomic.Database
  [dados id]

  ;pelo que entendi, aqui busca uma entidade que tem exatamente este id
  (let [
    query '[:find ?id-entidade
            :in $ ?id-entidade
            :where [?id-entidade]
            ] 
     resultado (d/q query dados id)
     ]
    (seq resultado)
    )
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

(defn busca-todos-atributos-entidade [dados id] 
  (d/pull dados '[*] id)
  )
