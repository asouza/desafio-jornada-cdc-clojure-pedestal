(ns nova-categoria
  (:require
    [utilitarios]
    [malli.core :as m]
    [malli.error :as me]
    [datomic-schema-categoria]
    [datomic.api :as d])
  (:import (java.time LocalDateTime))
  )

(def schema-nova-categoria
  [:map
   [:nome [:string {:min 1 :max 20 :error/message "Nome é obrigatório"}]]
   [:descricao [:string {:min 1 :max 100 :error/message "Descricao obrigatoria"}]]
   ])

(defrecord Categoria [nome descricao instante-criacao])

(defn- ja-existe-nome-cadastrado [context categoria]
  (let [
        dados (get-in context [:request :db])
        nome-buscado (:nome categoria)
        query '[:find ?e
                :in $ ?nome-buscado
                ;pq eu preciso passar esse filtro antes? preciso entender melhor
                :where
                ;[?e :categoria/nome ?nome]
                [?e :categoria/nome ?nome-buscado]
                ]
        categorias-encontradas (d/q query dados nome-buscado)
        ]

    (seq categorias-encontradas)
    )
  )

(def handler {
                     :name  :nova-categoria
                     :enter (fn [context]
                              (let [
                                    payload (get-in context [:request :json-params])
                                    ;aqui eu estou validando duas vezes?
                                    valid? (m/validate schema-nova-categoria payload)
                                    errors (me/humanize (m/explain schema-nova-categoria payload))
                                    ]

                                (cond
                                  (not valid?) (utilitarios/respond-validation-error-with-json context errors)

                                  (ja-existe-nome-cadastrado context payload) (utilitarios/respond-validation-error-with-json context {:global-errors ["Ja existe categoria com o nome passado"]})

                                  :else (let [
                                               novo-id (utilitarios/executa-transacao context [(datomic-schema-categoria/categoria-to-schema payload)])
                                              ]

                                          (utilitarios/respond-with-json context {:id novo-id})
                                          )
                                  )
                                )
                              )

                     })


