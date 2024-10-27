(ns lista-autores-test 
  (:require
   
   [state-flow.api :as api :refer [match?]]  
   [lista-autores] 
   )
   
  )


(defn executa-transacao-datomic [entidade]
  (println entidade)
  (java.util.UUID/randomUUID)
  )



(def payload {:nome "nome de teste"
              :email "email@email"
              :descricao "descricao da pessoa autora"})


(defn cria-contexto-com-payload-e-dados [payload dados]
  {:request {:funcao-transacao executa-transacao-datomic
             :json-params payload
             :db dados}}
  )

#_(
   - Cria um deflow
   - Precisa definir o estado inicial do flow
     - precisa existir uma pessoa autora cadastrada no banco de dados
     - prepara o context com as chaves padrões
   - usa o get-state para aplicar a função do handler em cima do estado 
   - tem que fazer o match agora para verificar 3 coisas na response:
     - o status é 200?
     - o vetor com autores de saída é igual o vetor esperado 
   
)


(def autores-salvos #{[1 :autor/nome (:nome payload)]
                                 [1 :autor/email (:email payload)]
                                 [1 :autor/descricao (:descricao payload)]})


(api/defflow deveria-listar-autores {:init (constantly (cria-contexto-com-payload-e-dados {} autores-salvos))}
  ;aqui eu passo uma funcao que recebe um parametro para o get-state. 
  ;essa funcao chama o handler passando o estado recebido. 
  ;depois pego a chave :response. 
  ;poderia ter usado o let embaixo, mas aí fica algo (:response resposta)
  [resposta (api/get-state #(:response ((:enter lista-autores/handler) %1)))] 
  
  (match? 200 (:status resposta))
  (match? [payload] (:body resposta))
)


    






