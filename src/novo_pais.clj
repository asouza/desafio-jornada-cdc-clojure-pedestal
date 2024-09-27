(ns novo-pais
  (:require
    [utilitarios]
    [malli.core :as m]
    [malli.error :as me])
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
                             banco-dados (get-in context [:request :database])
                             payload (utilitarios/parse-json-body context)
                             ;aqui eu estou validando duas vezes?
                             valido? (m/validate schema-novo-pais payload)
                             errors (me/humanize (m/explain schema-novo-pais payload))
                             ;;deve ter outro jeito de criar uma funcao para atrasar a execução de um código
                             ja-existe-nome-cadastrado (fn [] (utilitarios/verifica-campo-banco-dados banco-dados :paises :nome (:nome payload)))
                             ]

                         (cond
                           (not valido?) (utilitarios/respond-validation-error-with-json context errors)

                           (ja-existe-nome-cadastrado) (utilitarios/respond-validation-error-with-json context {:global-erros ["Já existe um pais com o mesmo nome"]})

                           :else (let [{:keys [nome]} payload
                                       instance-criacao (LocalDateTime/now)
                                       id (utilitarios/gera-chave-primaira)
                                       pais-para-salvar (->Pais nome instance-criacao)
                                       nova-versao-banco-dados ((:funcao-altera-banco-dados context) (fn [ultima-versao-banco-dados]
                                                                                                       (utilitarios/insere-tabela ultima-versao-banco-dados :paises id pais-para-salvar))
                                                                )]





                                        (utilitarios/respond-with-json (assoc-in context [:request :database] nova-versao-banco-dados) {:id id})

                                   )
                           )
                         )
           )
              })
