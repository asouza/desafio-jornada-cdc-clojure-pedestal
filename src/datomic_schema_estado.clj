(ns datomic-schema-estado

  (:require
    [utilitarios])
  )

;preciso ver como crio um schema que suporta um localdatetime
(def schema [{:db/ident :estado/nome
              :db/valueType :db.type/string
              :db/cardinality :db.cardinality/one
              }

             ;aqui era melhor ter chamado de autor mesmo.
             {:db/ident :estado/pais
              :db/valueType :db.type/ref
              :db/cardinality :db.cardinality/one
              }
             ])

(defn to-schema
  "Recebe um mapa que representa um estado e retorna o schema"
  [estado]
  {
   :estado/nome (:nome estado)
   :estado/pais (:id-pais estado)
   }
  )

(def handler
  {
   :name :datomic-registra-schema-estado
   :enter (fn [context]
            (utilitarios/executa-transacao context schema)
            (utilitarios/respond-with-status context 200)
            )
   }
  )


