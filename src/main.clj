(ns main
  (:require [io.pedestal.http :as http]
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
            [carrinho]
            [carrinhos]
            [datommic-schema-autor]
            [datomic-cria-banco]
            [datomic.api :as d]
            [datomic-schema-categoria]
            [datomic-schema-livro]
            [datomic-schema-pais]
            [datomic-schema-estado]
            [datomic-schema-carrinho]
            )

  (:import (java.time LocalDateTime)))

;adicionando o schema de validação de datas
(mr/set-default-registry!
  (mr/composite-registry
    (m/default-schemas)
    (met/schemas)))




(defonce database (atom {}))

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

;; configuracoes

;eu quero descobrir pq o argumento de nome da rota não é um mapa
(def routes
  (route/expand-routes
    #{
       ["/autores" :post [(body-params) db-interceptor novo-autor/handler]]
       ["/autores" :get [db-interceptor lista-autores/handler]]
       ["/categorias" :post [db-interceptor nova-categoria/handler]]
       ["/livros" :post [db-interceptor novo-livro/handler]]
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

(def service-map
  {::http/routes routes
   ::http/type :jetty
   ::http/port 8890
   }
  )

(defonce server (atom nil))

(defn test-request [verb url]
  (io.pedestal.test/response-for (::http/service-fn @server) verb url))

(defn start-dev []
  (reset! server (http/start (http/create-server (assoc service-map ::http/join? false))))
  )

(defn stop-server []
  (http/stop @server)
  )

(defn restart []
  (stop-server)
  (start-dev)
  )





