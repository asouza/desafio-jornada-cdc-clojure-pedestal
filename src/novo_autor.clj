(ns novo-autor
  (:require
    [utilitarios]
    [malli.core :as m]
    [malli.error :as me]
    [datommic-schema-autor]
    [datomic.api :as d]
    [schema.core :as s]
    [schema-refined.core :as r]
    [valida-email]
   )
  (:import (java.time LocalDateTime)))




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

(s/defschema NovoAutorHandlerRequest
  {:nome r/NonEmptyStr
   :email (r/refined s/Str (r/And r/NonEmptyStr valida-email/Email))
   :descricao r/NonEmptyStr})

(s/defn ^:always-validate logica-cria-autor [
                           context 
                           payload :- NovoAutorHandlerRequest
                           db 
                           funcao-transacao
                           ]
  (let [] 
    (cond 
      
      (ja-existe-email-cadastrado db payload) (utilitarios/respond-validation-error-with-json context {:global-erros ["Ja existe autor com email cadastrado"]})
      
      :else (let [novo-id (funcao-transacao [(datommic-schema-autor/autor-to-schema payload)])]
              
              (utilitarios/respond-with-json context {:id novo-id}))
      )
    )
  )  


(def schema-http-request-payload
  [:map
   [:nome [:string {:min 1 :max 20 :error/message "Nome é obrigatório"}]]
   [:email [:string {:min 1 :error/message "Email inválido"}]]
   [:descricao  [:string {:min 1 :max 100 :error/message "Descricao obrigatoria"}]]])
  

(defn handler [{:keys [:datomic]}]
  {:name :novo-autor
   :enter
   (fn [context]
       ;poderia usar uma (s/fn-validation) aqui para forçar a checagem de tipo na chamada
     #_(
        - valida com o mali o payload externo
        - converte para o schema definido
        - e aí passar o que foi convertido para a lógica
        - depois analisar como isolar isso.  

     ) 
    ;;  (try
       (let [payload (get-in context [:request :json-params])
             valid? (m/validate schema-http-request-payload payload)
             errors (me/humanize (m/explain schema-http-request-payload payload))]
         

         (cond
           (not valid?) (utilitarios/respond-validation-error-with-json context errors)
           
           :else (logica-cria-autor context payload (:db datomic) (:funcao-transacao datomic))
           )         
      ;;  (catch clojure.lang.ExceptionInfo e
      ;;    (let [erros (:error (ex-data e))]
      ;;      (println erros)
      ;;      context)))
         ))})
