(ns novo-autor
  (:require
    [utilitarios]
    [malli.core :as m]
    [malli.error :as me]
    [datommic-schema-autor]
    [datomic.api :as d])
  (:import (java.time LocalDateTime)))

(def schema-novo-autor
  [:map
   [:nome [:string {:min 1 :max 20 :error/message "Nome é obrigatório"}]]
   [:email [:string {:min 1 :error/message "Email inválido"}]]
   [:descricao  [:string {:min 1 :max 100 :error/message "Descricao obrigatoria"}]]
   ])

(defrecord Autor [nome email descricao instante-criacao])

(defn ja-existe-email-cadastrado [context autor]
  (let [
        dados (get-in context [:request :db])
        email (:email autor)
        ;deve ter um jeito de passar um simbolo que represente que eu quero o valor da variável email
        ;quando eu passo a string direto funciona, acho que é isso.
        query '[:find ?autor-email
                :in $ ?email
                ;pq eu preciso passar esse filtro antes? preciso entender melhor
                :where
                [?e :autor/email ?autor-email]
                [?e :autor/email ?email]
               ]
        emails (d/q query dados email)
        ]

    ;Eu tinha feito assim => ((not (empty? emails)))
    ;a IDE mandou eu fazer assim
    (seq emails)
    )
  )

(def handler {
                 :name :novo-autor
                 :enter (fn [context]
                          (let [
                                payload (utilitarios/parse-json-body context)
                                ;aqui eu estou validando duas vezes?
                                valid? (m/validate schema-novo-autor payload)
                                errors (me/humanize (m/explain schema-novo-autor payload))
                                ]

                            (cond
                              (not valid?) (utilitarios/respond-validation-error-with-json context errors)

                              (ja-existe-email-cadastrado context payload) (utilitarios/respond-validation-error-with-json context {:global-erros ["Já existe autor com email cadastrado"]})

                              :else (let [{:keys [nome email descricao]} payload
                                          instance-criacao (LocalDateTime/now)
                                          autor-para-salvar (->Autor nome email descricao instance-criacao)
                                          ]

                                      (utilitarios/executa-transacao context [(datommic-schema-autor/autor-to-schema autor-para-salvar)])
                                      (utilitarios/respond-with-status context 200)

                                      )
                              )
                            )
                          )

                 })
