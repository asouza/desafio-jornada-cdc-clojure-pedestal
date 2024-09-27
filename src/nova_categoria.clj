(ns nova-categoria
  (:require
    [utilitarios]
    [malli.core :as m]
    [malli.error :as me])
  (:import (java.time LocalDateTime))
  )

(def schema-nova-categoria
  [:map
   [:nome [:string {:min 1 :max 20 :error/message "Nome é obrigatório"}]]
   [:descricao [:string {:min 1 :max 100 :error/message "Descricao obrigatoria"}]]
   ])

(defrecord Categoria [nome descricao instante-criacao])

(def handler {
                     :name  :nova-categoria
                     :enter (fn [context]
                              (let [
                                    banco-dados (get-in context [:request :database])
                                    payload (utilitarios/parse-json-body context)
                                    ;aqui eu estou validando duas vezes?
                                    valid? (m/validate schema-nova-categoria payload)
                                    errors (me/humanize (m/explain schema-nova-categoria payload))
                                    ;;deve ter outro jeito de criar uma funcao para atrasar a execução de um código
                                    ja-existe-nome-cadastrado (fn [] (utilitarios/verifica-campo-banco-dados banco-dados :categorias :nome (:nome payload)))
                                    ]

                                (cond
                                  (not valid?) (utilitarios/respond-validation-error-with-json context errors)

                                  (ja-existe-nome-cadastrado) (utilitarios/respond-validation-error-with-json context {:global-erros ["Já existe categoria com o nome passado"]})

                                  :else (let [{:keys [nome descricao]} payload
                                              instance-criacao (LocalDateTime/now)
                                              id (utilitarios/gera-chave-primaira)
                                              categoria-para-salvar (->Categoria nome descricao instance-criacao)
                                              nova-versao-banco-dados ((:funcao-altera-banco-dados context) (fn [ultima-versao-banco-dados]
                                                                                                              (utilitarios/insere-tabela ultima-versao-banco-dados :categorias id categoria-para-salvar))
                                                                       )
                                              ]

                                          (utilitarios/respond-with-json (assoc-in context [:request :database] nova-versao-banco-dados) {:id id})

                                          )
                                  )
                                )
                              )

                     })


