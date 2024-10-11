(ns datomic-cria-banco
  (:require
    [utilitarios]
    [datomic.api :as d])
  )

(def handler
  {
   :name :datomic-cria-banco
   :enter (fn [context]
            (let [
                  db-uri "datomic:dev://localhost:4334/cdcv3"
                  ]
              (d/create-database db-uri)
              (utilitarios/respond-with-status context 200)
              ))
   }
  )

