(ns carrinho
  (:require
    [utilitarios]
    [malli.core :as m]
    [malli.error :as me]
    [datomic-lib]
    [datomic.api :as d]
    [datomic-schema-carrinho])
  (:import (java.time LocalDateTime))
  )

;validar que chegou um array de itens
;https://github.com/metosin/malli?tab=readme-ov-file#vector-schemas
  ;tem que ter no mínimo um item no array
  ;cada item do array precisa ter uma quantidade maior que zero
  ;cada id referenciado precisa de fato existir
  ;nao pode ter id livro repetido no array

;construção do mapa do carrinho em si
  ;para cada item do array, transforma para um item com o id do livro, preco original, titulo e valor calculado
  ;adiciona o item copiando as informacoes do livro e o montante


(defn- transforma-item-carrinho [banco-dados payload]
    (map (fn [item]
            (let [
                    livro (utilitarios/busca-item-por-campo banco-dados :livros :id (:id-livro item))
                  ]
                {
                   :id-livro (:id livro)
                   :preco-original (:preco livro)
                   :titulo (:titulo livro)
                   :valor-calculado (* (:preco livro) (:quantidade item))
                 }
              )
           )
         payload
         ))


(def schema-novo-carrinho
  [:vector {:min 1}
   [:map
    [:id-livro [:int {:min 1 :error/message "Id do livro precisa ser um numero"}]]
    ;[:quantidade  [:and [:int {:error/message "Quantidade precisa ser um inteiro"}] [:> 0] ]]
    [:quantidade  [:int {:min 1 :error/message "Quantidade precisa ser um inteiro"}]]
    ]
   ]
  )

(defrecord Carrinho [itens instante-criacao])

(defn- mapeia-item-para-id-livro [item]
  (:id-livro item)
  )

(defn- todos-ids-livros-existem [banco-dados payload]
  (let [
          ids-livros-payload (map mapeia-item-para-id-livro payload)
          ids-encontrados (d/pull-many banco-dados '[:db/id] ids-livros-payload)
        ]
    (= (count ids-encontrados) (count ids-livros-payload))
    )

  )


(defn- todos-ids-livros-diferentes? [payload]
  (let [ids (map (fn [item]
                   (:id-livro item)
                   )
                 payload)]
    (= (count ids) (count (set ids))))
  )

(defn- carrega-livro-por-id [dados]
  ;vai retornar a funcao faltando o argumento do id
  (partial datomic-lib/busca-todos-atributos-entidade dados)
  )

(def handler-passo-1 {
    :name :carrinho-passo-1

    :enter (fn [context]
          (let [
                banco-dados (get-in context [:request :db])
                payload (utilitarios/parse-json-body context)
                valido? (m/validate schema-novo-carrinho payload)
                errors (me/humanize (m/explain schema-novo-carrinho payload))
                ]
            (cond
              (not valido?) (utilitarios/respond-validation-error-with-json context errors)

              (not (todos-ids-livros-diferentes? payload)) (utilitarios/respond-validation-error-with-json context {:global-erros ["Tem livro igual"]})

              (not (todos-ids-livros-existem banco-dados payload)) (utilitarios/respond-validation-error-with-json context {:global-erros ["Tem livro referenciado que nao existe"]})

              :else (let [
                          id-carrinho (java.util.UUID/randomUUID)
                          carrinho (datomic-schema-carrinho/to-schema id-carrinho payload (carrega-livro-por-id banco-dados))
                          id-carrinho-salvo (utilitarios/executa-transacao context [carrinho])
                          ]

                      (utilitarios/respond-with-json context {:id id-carrinho-salvo})
                      )
              )

            )
     )

})
