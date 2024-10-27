(ns nova-categoria-test 
  (:require
   [clojure.test :refer :all]
   [state-flow.api :as api :refer [match?]]  
   [nova-categoria] 
   )
   
  )


(defn executa-transacao-datomic [entidade]
  (println entidade)
  [1]
  )



(def payload {:nome "nome da categoria"
              :descricao "descricao"})


(defn cria-contexto-com-payload-e-dados [payload dados]
  {:request {:funcao-transacao executa-transacao-datomic
             :json-params payload
             :db dados}}
  )



(def categorias-com-nome-duplicado #{[1 :categoria/nome (:nome payload)]
                                 [1 :categoria/descricao (:descricao payload)]
                                 })

(defn executa-handler [estado]
  (:response ((:enter nova-categoria/handler) estado))
  )

(api/defflow deveria-criar-nova-categoria {:init (constantly (cria-contexto-com-payload-e-dados payload #{}))}
  [resposta (api/get-state executa-handler)] 
  (match? 200 (:status resposta))
  (match? {:id [1]} (:body resposta))
  )

(api/defflow nao-deveria-criar-categoria-duplicada {:init (constantly (cria-contexto-com-payload-e-dados payload categorias-com-nome-duplicado))}
  [resposta (api/get-state executa-handler)]
  (match? 400 (:status resposta)) 
  (match? {:global-errors ["Ja existe categoria com o nome passado"]} (:body resposta)))


    






