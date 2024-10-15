(ns novo-pais
  (:require
    [utilitarios]
    [malli.core :as m]
    [malli.error :as me]
    [datomic-lib]
    [datomic-schema-pais]
    )
  (:import (java.time LocalDateTime)))

(defrecord Pais [nome instante-criacao])

(def schema-novo-pais
  [:map
   [:nome [:string {:min 1 :max 20 :error/message "Nome é obrigatório"}]]
   ])

(def handler {
  :name :novo-pais
  :enter (fn [context]
                       (let [
                             payload (utilitarios/parse-json-body context)
                             ;aqui eu estou validando duas vezes?
                             valido? (m/validate schema-novo-pais payload)
                             errors (me/humanize (m/explain schema-novo-pais payload))
                             dados (get-in context [:request :db])
                             ;;deve ter outro jeito de criar uma funcao para atrasar a execução de um código
                             ]

                         (cond
                           (not valido?) (utilitarios/respond-validation-error-with-json context errors)

                           (datomic-lib/busca-entidades-por-unico-atributo dados :pais/nome (:nome payload)) (utilitarios/respond-validation-error-with-json context {:global-erros ["Já existe um pais com o mesmo nome"]})

                           :else (let [novo-id (utilitarios/executa-transacao context [(datomic-schema-pais/to-schema payload)])]
                                        (utilitarios/respond-with-json context {:id novo-id})

                                   )
                           )
                         )
           )
              })
