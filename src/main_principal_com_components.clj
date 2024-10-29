(ns main-principal-com-components
  (:require 
   [com.stuartsierra.component :as component] 
   [com.stuartsierra.component.repl
      :refer [reset set-init start stop system]]   
   [io.pedestal.http :as http]
   [io.pedestal.http.route :as route]
   [io.pedestal.test :as test]
   [io.pedestal.http.body-params :refer [body-params]]
   [malli.core :as m]
   [malli.experimental.time :as met]
   [malli.registry :as mr]
   [lista-livros]
   [detalhe-livro]
   [novo-pais]
   [novo-estado]
   [lista-autores]
   [nova-categoria]
   [novo-livro]
   [novo-autor]
   [carrinho]
   [carrinhos]
   [datommic-schema-autor]
   [datomic-cria-banco]
   [datomic.api :as d]
   [datomic-schema-categoria]
   [datomic-schema-livro]
   [datomic-schema-pais]
   [datomic-schema-estado]
   [datomic-schema-carrinho])

  (:import (java.time LocalDateTime)))

;adicionando o schema de validação de datas
(mr/set-default-registry!
  (mr/composite-registry
    (m/default-schemas)
    (met/schemas)))


(defrecord Datomic []
  component/Lifecycle

  (start [this]
    (println "Subindo o Datomic")
    (let [db-uri "datomic:dev://localhost:4334/cdcv3"
          conexao (d/connect db-uri)
          versao-atual-banco (d/db conexao)
          funcao-transacao (fn [dados]
                                     ;dados pode ser um mapa ou um array
                             (let [resultado @(d/transact conexao dados)]
                                       ;retornando todos ids permanentes gerados na transacao
                                       ;a documentacao disse que esses temporarios estão mapeados para os permanentes
                               (vals (:tempids resultado))))]

      (assoc this :conexao conexao :db versao-atual-banco :funcao-transacao funcao-transacao)))

  (stop [this]
    (println "Parando o Datomic")
    this
    )
  )


(def db-interceptor
  {
   :name :database-interceptor
   :enter (fn [context]
            (println "Entrando no db-interceptor")
            (let [db-uri "datomic:dev://localhost:4334/cdcv3"
                  conexao (d/connect db-uri)
                  versao-atual-banco (d/db conexao)
                  funcao-transacao (fn [dados]
                                     ;dados pode ser um mapa ou um array
                                     (let [
                                           resultado @(d/transact conexao dados)
                                           ]
                                       ;retornando todos ids permanentes gerados na transacao
                                       ;a documentacao disse que esses temporarios estão mapeados para os permanentes
                                       (vals (:tempids resultado))
                                       )

                                     )
                  ;preciso mesmo colocar na request? eu vou gerar um novo contexto para uma request específica, acho que já resolvia
                  context-com-banco (update context :request assoc :conexao conexao :db versao-atual-banco :funcao-transacao funcao-transacao)

                  ]
              ;(d/transact conn schema-autor)
                context-com-banco
              )

            )
   :leave (fn [context]
            (println "Saindo do db-interceptor")
            ;(println (get-in context [:request :database]))
            context
            )
   }
  )

#_(
   - Copiar a parte básica da configuração do pedestal para rodar com components
   - fazer com que o handler vira uma função que recebe as dependencias e retorna uma 
     funcao parcial que falta receber o contexto. A ideia é controlar o que entra no 
     handler. 
)

;; configuracoes

;eu quero descobrir pq o argumento de nome da rota não é um mapa
(defn routes [dependencias]
  (route/expand-routes
    #{
       ["/autores" :post [(body-params) (novo-autor/handler dependencias)]]
       ["/autores" :get [http/json-body (lista-autores/handler dependencias)]]
       ["/categorias" :post [http/json-body (body-params) db-interceptor nova-categoria/handler]]
       ["/livros" :post [http/json-body (body-params) db-interceptor novo-livro/handler]]
       ["/livros" :get [db-interceptor lista-livros/handler]]
       ["/livros/:id" :get [db-interceptor detalhe-livro/handler]]
       ["/paises" :post [db-interceptor novo-pais/handler]]
       ["/paises/:id-pais/estados" :post [db-interceptor novo-estado/handler]]
       ["/carrinho/passo-1" :post [db-interceptor carrinho/handler-passo-1]]
       ["/carrinhos" :get [db-interceptor carrinhos/handler]]
       ["/datomic-cria-banco" :post [datomic-cria-banco/handler]]
       ["/datomic-registra-schema-autor" :post [db-interceptor datommic-schema-autor/handler]]
       ["/datomic-registra-schema-categoria" :post [db-interceptor datomic-schema-categoria/handler]]
       ["/datomic-registra-schema-livro" :post [db-interceptor datomic-schema-livro/handler]]
       ["/datomic-registra-schema-pais" :post [db-interceptor datomic-schema-pais/handler]]
       ["/datomic-registra-schema-estado" :post [db-interceptor datomic-schema-estado/handler]]
       ["/datomic-registra-schema-carrinno" :post [db-interceptor datomic-schema-carrinho/handler]]
      }
    )
  )

(defrecord Pedestal [service-map service]
  component/Lifecycle

  ;acho que é aqui que eu vou precisa configurar a rota usando os componentes gerenciados
  ; o serice-map tem as rotas e eu posso comportar essas rotas agora. 
  (start [this]
    (if service
      this
      ;isso aqui vira uma dependencia?
      (assoc this :service (http/start (http/create-server service-map)))))

  (stop [this]
    (http/stop service)
    (assoc this :service nil)))

(defrecord ServiceMap [env datomic]
  component/Lifecycle 
  (start [this]
    (println "Subbindo service map")  
    (assoc this  :env env
           ::http/routes (routes {:datomic datomic})
           ::http/type :jetty
           ::http/port 8890
           ::http/join? false))

  (stop [this]
    (println "Destruindo service map")
    this))  

(defn new-system
  [env]
  (component/system-map
   :datomic (map->Datomic {})
   :service-map
   (component/using
    (map->ServiceMap {:env env})
    [:datomic])

   :pedestal
   (component/using
    (map->Pedestal {})
    [:service-map])))

(set-init (fn [old-system] (new-system :prod)))





