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

                   {:db/ident :autor/descricao
                    :db/valueType :db.type/string
                    :db/cardinality :db.cardinality/one
                    :db/doc "descricao do autor"}

                   ])

;deixando perto o que faz sentido estar perto
;acho que podia ter um arquivo como tudo-sobre-autor (record e schema do banco)
(defn autor-to-schema
  "Recebe um mapa de autor e retorna no schema definido para salvar"
  [autor]
  {
    :autor/nome (:nome autor)
    :autor/email (:email autor)
    :autor/descricao (:descricao autor)
   }
  )

(def handler
  {
   :name :datomic-registra-schema-autor
   :enter (fn [context]
              (utilitarios/executa-transacao context schema-autor)
              (utilitarios/respond-with-status context 200)
              )
   }
  )

