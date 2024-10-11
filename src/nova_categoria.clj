(ns nova-categoria
  (:require
    [utilitarios]
    [malli.core :as m]
    [malli.error :as me]
    [datomic-schema-categoria])
  (:import (java.time LocalDateTime))
  )

(def schema-nova-categoria
  [:map
   [:nome [:string {:min 1 :max 20 :error/message "Nome é obrigatório"}]]
   [:descricao [:string {:min 1 :max 100 :error/message "Descricao obrigatoria"}]]
   ])

(defrecord Categoria [nome descricao instante-criacao])

(defn- ja-existe-nome-cadastrado [dados]
  false
  )

(def handler {
                     :name  :nova-categoria
                     :enter (fn [context]
                              (let [
                                    payload (utilitarios/parse-json-body context)
                                    ;aqui eu estou validando duas vezes?
                                    valid? (m/validate schema-nova-categoria payload)
                                    errors (me/humanize (m/explain schema-nova-categoria payload))
                                    ]

                                (cond
                                  (not valid?) (utilitarios/respond-validation-error-with-json context errors)

                                  (ja-existe-nome-cadastrado context) (utilitarios/respond-validation-error-with-json context {:global-erros ["Já existe categoria com o nome passado"]})

                                  :else (do
                                          (utilitarios/executa-transacao context [(datomic-schema-categoria/categoria-to-schema payload)])
                                          (utilitarios/respond-with-status context 200)
                                          )
                                  )
                                )
                              )

                     })


