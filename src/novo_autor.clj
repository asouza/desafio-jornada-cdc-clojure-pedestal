(ns novo-autor
  (:require
    [utilitarios]
    [malli.core :as m]
    [malli.error :as me]
    [datommic-schema-autor])
  (:import (java.time LocalDateTime)))

(def schema-novo-autor
  [:map
   [:nome [:string {:min 1 :max 20 :error/message "Nome é obrigatório"}]]
   [:email [:string {:min 1 :error/message "Email inválido"}]]
   [:descricao  [:string {:min 1 :max 100 :error/message "Descricao obrigatoria"}]]
   ])

(defrecord Autor [nome email descricao instante-criacao])

(def handler {
                 :name :novo-autor
                 :enter (fn [context]
                          (let [
                                payload (utilitarios/parse-json-body context)
                                ;aqui eu estou validando duas vezes?
                                valid? (m/validate schema-novo-autor payload)
                                errors (me/humanize (m/explain schema-novo-autor payload))
                                ;;deve ter outro jeito de criar uma funcao para atrasar a execução de um código
                                ;ja-existe-email-cadastrado (fn [] (utilitarios/verifica-campo-banco-dados banco-dados :autores :email (:email payload)))
                                ja-existe-email-cadastrado (fn [] false)
                                ]

                            (cond
                              (not valid?) (utilitarios/respond-validation-error-with-json context errors)

                              (ja-existe-email-cadastrado) (utilitarios/respond-validation-error-with-json context {:global-erros ["Já existe autor com email cadastrado"]})

                              :else (let [{:keys [nome email descricao]} payload
                                          instance-criacao (LocalDateTime/now)
                                          autor-para-salvar (->Autor nome email descricao instance-criacao)
                                          ]

                                      (utilitarios/executa-transacao context (datommic-schema-autor/autor-to-schema autor-para-salvar))
                                      (utilitarios/respond-with-status context 200)

                                      )
                              )
                            )
                          )

                 })
