(ns datomic-schema-livro

  (:require
    [utilitarios])
  )

;preciso ver como crio um schema que suporta um localdatetime
(def schema [{:db/ident :livro/titulo
              :db/valueType :db.type/string
              :db/cardinality :db.cardinality/one
              :db/unique :db.unique/value
              }

             {:db/ident :livro/resumo
              :db/valueType :db.type/string
              :db/cardinality :db.cardinality/one
             }

             {:db/ident :livro/preco
              :db/valueType :db.type/bigdec
              :db/cardinality :db.cardinality/one
              }

             {:db/ident :livro/isbn
              :db/valueType :db.type/string
              :db/cardinality :db.cardinality/one
              }

             {:db/ident :livro/data-lancamento
              :db/valueType :db.type/instant
              :db/cardinality :db.cardinality/one
              }

             {:db/ident :livro/id-autor
              :db/valueType :db.type/ref
              :db/cardinality :db.cardinality/one
              }

             {:db/ident :livro/id-categoria
              :db/valueType :db.type/ref
              :db/cardinality :db.cardinality/one
              }
             ])

(defn to-schema
  "Recebe um mapa de categoria e retorna no schema definido para salvar"
  [livro]
  {
   :livro/titulo (:titulo livro)
   :livro/resumo (:resumo livro)
   :livro/preco (:preco livro)
   :livro/isbn (:isbn livro)
   :livro/data-lancamento (:data-lancamento livro)
   :livro/id-autor (:id-autor livro)
   :livro/id-categoria (:id-categoria livro)
   }
  )

(def handler
  {
   :name :datomic-registra-schema-livro
   :enter (fn [context]
            (utilitarios/executa-transacao context schema)
            (utilitarios/respond-with-status context 200)
            )
   }
  )


