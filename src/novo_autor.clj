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

(defn ja-existe-email-cadastrado [db autor]
  (let [
        email (:email autor)
        ;deve ter um jeito de passar um simbolo que represente que eu quero o valor da variável email
        ;quando eu passo a string direto funciona, acho que é isso.
        query '[:find ?autor-email
                :in $ ?email
                ;pq eu preciso passar esse filtro antes? preciso entender melhor
                ;não precisa, testei no cadastro de categoria.
                :where
                [?e :autor/email ?autor-email]
                [?e :autor/email ?email]
               ]
        emails (d/q query db email)
        ]

    ;Eu tinha feito assim => ((not (empty? emails)))
    ;a IDE mandou eu fazer assim
    (seq emails) 
    )
  )

(defn- logica-cria-autor [context db funcao-transacao]
  (println "=====")
  (println funcao-transacao)
  (let [payload (get-in context [:request :json-params])
                                  ;aqui eu estou validando duas vezes?
        valid? (m/validate schema-novo-autor payload)
        errors (me/humanize (m/explain schema-novo-autor payload))]
    
    (cond
      (not valid?) (utilitarios/respond-validation-error-with-json context errors)
      
      (ja-existe-email-cadastrado db payload) (utilitarios/respond-validation-error-with-json context {:global-erros ["Ja existe autor com email cadastrado"]})
      
      :else (let [novo-id (funcao-transacao [(datommic-schema-autor/autor-to-schema payload)])]
              
              (utilitarios/respond-with-json context {:id novo-id}))))
  )                
  

(defn handler [{:keys [:datomic]}]
  {
   :name :novo-autor
   :enter
     (fn [contexto]
                (logica-cria-autor contexto (:db datomic) (:funcao-transacao datomic))
                )
})
