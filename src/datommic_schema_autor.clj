(ns datommic-schema-autor
  (:require
    [utilitarios]
    [datomic.api :as d])
  )

;preciso ver como crio um schema que suporta um localdatetime
(def schema-autor [{:db/ident :autor/nome
                    :db/valueType :db.type/string
                    :db/cardinality :db.cardinality/one
                    :db/doc "Nome do autor"}

                   {:db/ident :autor/email
                    :db/valueType :db.type/string
                    :db/cardinality :db.cardinality/one
                    :db/doc "email do autor"}
                   ])

(def handler
  {
   :name :datomic-registra-schema-autor
   :enter (fn [context]
            (let [
                  db-uri "datomic:dev://localhost:4334/cdc"
                  conn (d/connect db-uri)
                  ]
              (d/transact conn schema-autor)
              (utilitarios/respond-with-status context 200)
              ))
   }
  )

