(ns main (:require [io.pedestal.http :as http]
                   [io.pedestal.http.route :as route]
                   [io.pedestal.test :as test]
                   [clojure.data.json :as json]))



(defn parse-json-body [context]
  (let [body (slurp (get-in context [:request :body]))] ;; Converte o corpo para string
    (json/read-str body :key-fn keyword))) ;; Decodifica o JSON

(defonce database (atom {}))

(def db-interceptor
  {
   :name :database-interceptor
   :enter (fn [context]
            (println "Entrando no db-interceptor")
            (update context :request assoc :database @database)
            )
   :leave (fn [context]
            (println "Saindo do db-interceptor")
            ;ta recuperando um valor do mapa e desestruturando nas varíaveis. Aqui no caso é a operacao + args
            (if-let [[op & args] (:tx-data context)]
              (do
                ;aqui atualiza o valor do atom aplicando a funcao op com os args passados
                ;aqui é importante notar que a funcao de atualizacao do atom tem que ser pura.
                (apply swap! database op args)
                (assoc-in context [:request :database] @database)
                )
              ;; o db interceptor não estava retornando nada quando não tinha operacao. Se o interceptor
              ;; nao retorna nada ele interrompe o fluxo.
              context
              )
            )
   }
  )

;;aqui pode ser um multimetodo
(defn respond-with-json [context payload]
    (http/respond-with context 200 {"Content-Type" "application/json"} payload)
  )


(def novo-autor {
                 :name :novo-autor
                 :enter (fn [context]
                          (let [payload (parse-json-body context)]
                            (respond-with-json context payload)
                            )
                          )

                 })

;; configuracoes

;eu quero descobrir pq o argumento de nome da rota não é um mapa
(def routes
  (route/expand-routes
    #{
      ["/autores" :post [db-interceptor novo-autor]]
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





