(ns datomic-schema-carrinho

  (:require
    [utilitarios]
    )
  )

;preciso ver como crio um schema que suporta um localdatetime
(def schema [{:db/ident :carrinho/id
              :db/valueType :db.type/uuid
              :db/cardinality :db.cardinality/one
              :db/unique :db.unique/value
              }

             {:db/ident :carrinho/itens
              :db/valueType :db.type/ref
              :db/cardinality :db.cardinality/many
              :db/isComponent true
              }

             {:db/ident :item-carrinho/livro
              :db/valueType :db.type/ref
              :db/cardinality :db.cardinality/one
              }


             {:db/ident :item-carrinho/quantidade
              :db/valueType :db.type/long
              :db/cardinality :db.cardinality/one
              }

             {:db/ident :item-carrinho/preco-original
              :db/valueType :db.type/bigdec
              :db/cardinality :db.cardinality/one
              }

             {:db/ident :item-carrinho/titulo-original
              :db/valueType :db.type/string
              :db/cardinality :db.cardinality/one
              }

             ;aqui era melhor ter chamado de autor mesmo.
             {:db/ident :item-carrinho/preco-final
              :db/valueType :db.type/bigdec
              :db/cardinality :db.cardinality/one
              }
             ])

(defn- mapeia-item-carrinho-to-schema-item [funcao-mapeia-id-livro-para-livro]

  (fn [item]
    (let [
          livro-original (funcao-mapeia-id-livro-para-livro (:id-livro item))
          ]
      {
       :item-carrinho/quantidade (:quantidade item)
       :item-carrinho/livro (:id-livro item)
       :item-carrinho/preco-original (:livro/preco livro-original)
       :item-carrinho/titulo-original (:livro/titulo livro-original)
       :item-carrinho/preco-final (* (:livro/preco livro-original) (:quantidade item))
       }
      )
    )
  )

(defn to-schema
  "Recebe um array de itens e precisa retornar um carrinho"
  [id-carrinho itens-carrinho funcao-mapeia-id-livro-para-livro]
  {
   :carrinho/id id-carrinho
   :carrinho/itens (map (mapeia-item-carrinho-to-schema-item funcao-mapeia-id-livro-para-livro) itens-carrinho)
   }
  )

(def handler
  {
   :name :datomic-registra-schema-carrinho
   :enter (fn [context]
            (utilitarios/executa-transacao context schema)
            (utilitarios/respond-with-status context 200)
            )
   }
  )
