(ns datomic-schema-categoria

  (:require
    [utilitarios])
  )

;preciso ver como crio um schema que suporta um localdatetime
(def schema [{:db/ident :categoria/nome
                    :db/valueType :db.type/string
                    :db/cardinality :db.cardinality/one
                    :db/unique :db.unique/value
                    :db/doc "Nome da categoria"}

                   {:db/ident :categoria/descricao
                    :db/valueType :db.type/string
                    :db/cardinality :db.cardinality/one
                    :db/doc "descricao da categoria"}

                   ])

(defn categoria-to-schema
  "Recebe um mapa de categoria e retorna no schema definido para salvar"
  [categoria]
  {
   :categoria/nome (:nome categoria)
   :categoria/descricao (:descricao categoria)
   }
  )

(def handler
  {
   :name :datomic-registra-schema-categoria
   :enter (fn [context]
            (utilitarios/executa-transacao context schema)
            (utilitarios/respond-with-status context 200)
            )
   }
  )


