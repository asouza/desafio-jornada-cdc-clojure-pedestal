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



(def payload {:nome "Larissa"
              :email "email@email"
              :descricao "descricao da pessoa autora"})

(def dados-com-email-duplicado #{[1 :autor/nome "Larissa"]
              [1 :autor/email "email2@email"]
              [1 :autor/descricao "alguma descricao"]})


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


    (api/defflow cria-novo-autor {:init (constantly (cria-contexto-com-payload-e-dados payload #{}))}
      [resposta (api/get-state (:enter novo-autor/handler))]
      (match? 1 1)
      (match? {:status 200} (:response resposta)))






