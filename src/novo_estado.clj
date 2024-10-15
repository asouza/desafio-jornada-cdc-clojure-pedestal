(ns novo-estado
  (:require
    [utilitarios]
    [validacoes-conversoes]
    [malli.core :as m]
    [malli.error :as me]
    [datomic-lib]
    [datomic-schema-estado]
    [datomic.api :as d]
    )
  )

(def schema-novo-estado
  [:map
   [:nome [:string {:min 1 :max 20 :error/message "Nome é obrigatório"}]]
   ]
)



(defn- ja-tem-estado-com-mesmo-nome? [dados id-pais nome-estado]
    (let [
          query '[:find ?e
                  :in $ ?id-pais ?nome-estado
                  :where
                  [?e :estado/pais ?id-pais]
                  [?e :estado/nome ?nome-estado]
                  ]
          entidades (d/q query dados id-pais nome-estado)
          ]
        (seq entidades)
      )

  )

(def handler {
                 :name :novo-estado
                 :enter (fn [context]
                          (let [
                                id-pais (Long/parseLong (get-in context [:request :path-params :id-pais]))
                                payload (utilitarios/parse-json-body context)
                                ;aqui eu estou validando duas vezes?
                                valido? (m/validate schema-novo-estado payload)
                                errors (me/humanize (m/explain schema-novo-estado payload))
                                dados (get-in context [:request :db])
                                ]

                            (cond
                              (not valido?) (utilitarios/respond-validation-error-with-json context errors)
                              ;
                              (not (datomic-lib/busca-entidade dados :pais/nome id-pais)) (utilitarios/respond-validation-error-with-json context {:global-erros ["O pais não existe"]})

                              (ja-tem-estado-com-mesmo-nome? dados id-pais (:nome payload)) (utilitarios/respond-validation-error-with-json context {:global-erros ["Já tem estado com o mesmo nome para este país"]})


                              :else (let [
                                          novo-id (utilitarios/executa-transacao context [(datomic-schema-estado/to-schema (assoc payload :id-pais id-pais))])
                                          ]

                                      (utilitarios/respond-with-json context {:id novo-id})

                                      )
                              )
                            )
                          )

                 })
