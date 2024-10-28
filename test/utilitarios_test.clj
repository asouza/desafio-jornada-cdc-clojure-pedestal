(ns utilitarios-test)

(defn executa-handler 
  "Executa o handler passado já extraindo a response"
  [estado def-handler]
  (:response ((:enter def-handler) estado)))

(defn default-id-generator [atom-id]
  (swap! atom-id inc)
  )

(defn executa-transacao-datomic [fn-id-generator]

  (fn [entidade] 
      [(fn-id-generator)] 
    )
  )


(defn cria-contexto-com-payload-e-dados [payload dados atom-id]
  {:request {:funcao-transacao (executa-transacao-datomic  (fn [] (default-id-generator atom-id)))
             :json-params payload
             :db dados}})