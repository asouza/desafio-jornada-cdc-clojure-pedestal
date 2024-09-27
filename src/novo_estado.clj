(ns novo-estado
  (:require
    [utilitarios]
    [validacoes-conversoes]
    [malli.core :as m]
    [malli.error :as me]
    )
  (:import (java.time LocalDateTime)))

(def schema-novo-estado
  [:map
   [:nome [:string {:min 1 :max 20 :error/message "Nome é obrigatório"}]]
   ]
)



(defrecord Estado [nome id-pais instante-criacao])

(defn- ja-tem-estado-com-mesmo-nome? [banco-dados id-pais nome-estado]
    (let [estados-do-pais (utilitarios/busca-todos-itens-por-campo banco-dados :estados :id-pais id-pais)
          ]
        (some #(= nome-estado (:nome %)) estados-do-pais)
      )

  )

(defn- pais-existe? [banco-dados id-pais]
  (utilitarios/busca-item-por-campo banco-dados :paises :id id-pais)
  )

(def handler {
                 :name :novo-estado
                 :enter (fn [context]
                          (let [
                                id-pais (get-in context [:request :path-params :id-pais])
                                banco-dados (get-in context [:request :database])
                                payload (utilitarios/parse-json-body context)
                                ;aqui eu estou validando duas vezes?
                                valido? (m/validate schema-novo-estado payload)
                                errors (me/humanize (m/explain schema-novo-estado payload))
                                ]

                            (cond
                              (not valido?) (utilitarios/respond-validation-error-with-json context errors)
                              ;
                              (not (pais-existe? banco-dados id-pais)) (utilitarios/respond-validation-error-with-json context {:global-erros ["O pais não existe"]})

                              (ja-tem-estado-com-mesmo-nome? banco-dados id-pais (:nome payload)) (utilitarios/respond-validation-error-with-json context {:global-erros ["Já tem estado com o mesmo nome para este país"]})


                              :else (let [{:keys [nome]} payload
                                          instance-criacao (LocalDateTime/now)
                                          id (utilitarios/gera-chave-primaira)
                                          estado-para-salvar (->Estado nome id-pais instance-criacao)
                                          nova-versao-banco-dados ((:funcao-altera-banco-dados context)
                                                                   (fn [ultima-versao-banco-dados]
                                                                        (utilitarios/insere-tabela ultima-versao-banco-dados :estados id estado-para-salvar))
                                                                   )                                          ]

                                      (utilitarios/respond-with-json context {:id id})

                                      )
                              )
                            )
                          )

                 })
