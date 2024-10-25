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

(defn string-to-reader [s]
  (io/reader (char-array s))
  )

(defn executa-transacao-datomic [entidade]
  (println entidade)
  (java.util.UUID/randomUUID)
  )



(def payload (json/write-str {
                                  :nome "Larissa"
                                  :email "email@email"
                                  :descricao "descricao da pessoa autora"
                                  }))




(def contexto {
   :request {
             :funcao-transacao executa-transacao-datomic
             :body {
             } 
             }           
})

;preciso passar o reader aberto para que o slurp lá dentro funcione
;
(with-open [payloadReader (string-to-reader payload)]  
  ((:enter novo-autor/handler contexto) (assoc-in contexto [:request :body] payloadReader))
  )




