(ns carrinho
  (:require
    [utilitarios]
    [malli.core :as m]
    [malli.error :as me])
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
         )
  )

(def schema-novo-carrinho
  [:vector {:min 1}
   [:map
    [:id-livro [:string {:min 1 :max 20 :error/message "Id do livro é obrigatório"}]]
    ;[:quantidade  [:and [:int {:error/message "Quantidade precisa ser um inteiro"}] [:> 0] ]]
    [:quantidade  [:int {:min 1 :error/message "Quantidade precisa ser um inteiro"}]]
    ]
   ]
  )

(defrecord Carrinho [itens instante-criacao])

(defn- todos-ids-livros-existem [banco-dados payload]
  (every? (fn [item]
            (utilitarios/busca-item-por-campo banco-dados :livros :id (:id-livro item))
            )
          payload)
  )

(defn- todos-ids-livros-diferentes? [payload]
  (let [ids (map (fn [item]
                   (:id-livro item)
                   )
                 payload)]
    (= (count ids) (count (set ids))))
  )

(def handler-passo-1 {
    :name :carrinho-passo-1

    :enter (fn [context]
          (let [
                banco-dados (get-in context [:request :database])
                payload (utilitarios/parse-json-body context)
                valido? (m/validate schema-novo-carrinho payload)
                errors (me/humanize (m/explain schema-novo-carrinho payload))
                ]
            (cond
              (not valido?) (utilitarios/respond-validation-error-with-json context errors)

              (not (todos-ids-livros-diferentes? payload)) (utilitarios/respond-validation-error-with-json context {:global-erros ["Tem livro igual"]})

              (not (todos-ids-livros-existem banco-dados payload)) (utilitarios/respond-validation-error-with-json context {:global-erros ["Tem livro referenciado que nao existe"]})

              :else (let [
                          itens-mapeados (transforma-item-carrinho banco-dados payload)
                          instance-criacao (LocalDateTime/now)
                          id (utilitarios/gera-chave-primaira)
                          carrinho-para-salvar (->Carrinho itens-mapeados  instance-criacao)
                          ]
                      ((:funcao-altera-banco-dados context) (fn [ultima-versao-banco-dados]
                                                              (utilitarios/insere-tabela ultima-versao-banco-dados :carrinhos id carrinho-para-salvar))
                       )


                      (utilitarios/respond-with-json context {:id id})
                      )
              )

            )
     )

})
