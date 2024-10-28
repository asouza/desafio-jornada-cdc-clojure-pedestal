(ns nova-categoria-test 
  (:require
   [clojure.test :refer :all]
   [state-flow.api :as api :refer [match?]]  
   [nova-categoria] 
   [utilitarios-test]
   )
   
  )



(def payload {:nome "nome da categoria"
              :descricao "descricao"})



(def categorias-com-nome-duplicado #{[1 :categoria/nome (:nome payload)]
                                 [1 :categoria/descricao (:descricao payload)]
                                 })

(defn atom-id [] (atom 0))


(api/defflow deveria-criar-nova-categoria {:init (constantly (utilitarios-test/cria-contexto-com-payload-e-dados payload #{} (atom-id)))}
  [resposta (api/get-state utilitarios-test/executa-handler nova-categoria/handler) ]
  
  (match? {:status 200} resposta)
  (match? 200 (:status resposta))
  (match? {:id [1]} (:body resposta)) 
  )

(api/defflow nao-deveria-criar-categoria-duplicada {:init (constantly (utilitarios-test/cria-contexto-com-payload-e-dados payload categorias-com-nome-duplicado (atom-id)))}
  [resposta (api/get-state utilitarios-test/executa-handler nova-categoria/handler)]
  (match? 400 (:status resposta)) 
  (match? {:global-errors ["Ja existe categoria com o nome passado"]} (:body resposta)))


    






