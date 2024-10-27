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
    (println entidade)
    (let [
         id (fn-id-generator)
    ]
      [id]
      ) 
    )
  )


(defn cria-contexto-com-payload-e-dados [payload dados atom-id]
  {:request {:funcao-transacao (executa-transacao-datomic  #((default-id-generator atom-id)))
             :json-params payload
             :db dados}})