(ns datomic-schema-pais
  (:require
    [utilitarios])
  )

;preciso ver como crio um schema que suporta um localdatetime
(def schema [{:db/ident :pais/nome
              :db/valueType :db.type/string
              :db/cardinality :db.cardinality/one
              :db/unique :db.unique/value
              }
             ])

(defn to-schema
  "Recebe um mapa representando um pais e retorna o mapa com o schema do datomic"
  [pais]
  {
   :pais/nome (:nome pais)
   }
  )

(def handler
  {
   :name :datomic-registra-schema-pais
   :enter (fn [context]
            (utilitarios/executa-transacao context schema)
            (utilitarios/respond-with-status context 200)
            )
   }
  )

