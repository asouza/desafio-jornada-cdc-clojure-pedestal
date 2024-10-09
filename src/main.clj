(ns main
  (:require [io.pedestal.http :as http]
            [io.pedestal.http.route :as route]
            [io.pedestal.test :as test]
            [malli.core :as m]
            [malli.error :as me]
            [malli.experimental.time :as met]
            [malli.registry :as mr]
            [utilitarios :as utilitarios]
            [validacoes-conversoes :as validacoes-conversoes]
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
            (let [context-com-banco (update context :request assoc :database @database)
                  context-com-funcao-atualiza-banco (assoc context-com-banco
                                                      :funcao-altera-banco-dados
                                                      (fn [funcao-transacional]
                                                        (swap! database funcao-transacional)
                                                      ))
                  ]
                context-com-funcao-atualiza-banco
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
       ["/autores" :post [db-interceptor novo-autor/handler]]
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
       ["/datomic-registra-schema-autor" :post [datommic-schema-autor/handler]]
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





