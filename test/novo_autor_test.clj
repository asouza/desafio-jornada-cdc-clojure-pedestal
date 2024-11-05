(ns novo-autor-test 
  (:require
   [clojure.test :refer :all]
   [state-flow.api :as api :refer [flow match?]]
   [datomic.api :as d]
   [clojure.data.json :as json]
   [novo-autor]
   [clojure.java.io :as io]
   )
   
  )


(defn executa-transacao-datomic [entidade]
  (println entidade)
  (java.util.UUID/randomUUID)
  )



(def payload {:nome "nome de teste"
              :email "email@email.com"
              :descricao "descricao da pessoa autora"})


(defn cria-contexto-com-payload-e-dados [payload dados]
  {:request {:funcao-transacao executa-transacao-datomic
             :json-params payload
             :db dados}}
  )

#_(
   - Cria o payload-reader e mantém aberto com o with-open
   - Cria um deflow
   - Precisa definir o estado inicial do flow
     - Contexto com os dados + funcao de transacao inserida + payload
   - usa o get-state para aplicar a função do handler em cima do estado
     - quando ele faz get-state :simbolo, está aplicando essa função também. 
   - tem que fazer o match agora para verificar 2 coisas na response:
     - o status é 200?
     - tem a chave id na resposta?
   - Acho que da para colocar mais um flow passando o handler como argume
     - acho que não, tem que pegar o estado. A não ser que eu possa modificar o estado para acrescentar coisas. 
   
)

(defn json-to-map [str-json]
  (println str-json)
  (json/read-str str-json :key-fn keyword)
  )

(def dados-com-email-duplicado #{[1 :autor/nome "teste"]
                                 [1 :autor/email (:email payload)]
                                 [1 :autor/descricao "alguma descricao"]})


(api/defflow cria-novo-autor {:init (constantly (cria-contexto-com-payload-e-dados payload #{}))}
  [resposta (api/get-state (:enter (novo-autor/handler {:datomic {:db #{} :funcao-transacao executa-transacao-datomic}})))
   :let [body (get-in resposta [:response :body])]] 
  (match? {:status 200} (:response resposta)) 
  (match? true (contains? body :id)))

(api/defflow nao-pode-criar-autor-com-email-duplicado {:init (constantly (cria-contexto-com-payload-e-dados payload dados-com-email-duplicado))}
  [resposta (api/get-state (:enter (novo-autor/handler {:datomic {:db dados-com-email-duplicado :funcao-transacao executa-transacao-datomic}})))
   :let [body (get-in resposta [:response :body])]]
  (match? {:status 400} (:response resposta))          
  (match? true (contains? body :global-erros))
  (match? ["Ja existe autor com email cadastrado"] (:global-erros body)))


    






