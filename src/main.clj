(ns main (:require [io.pedestal.http :as http]
                   [io.pedestal.http.route :as route]
                   [io.pedestal.test :as test]
                   [clojure.data.json :as json]
                   [malli.core :as m]
                   [malli.error :as me]
                   )
  (:import [java.time LocalDateTime])
  )


;;##paraTreinar aqui eu posso usar o lance das specs, para definir bem a entrada. Brincar de pre e pos condicoes
(defn parse-json-body [context]
  (let [body (slurp (get-in context [:request :body]))] ;; Converte o corpo para string
    ;:key-fn é por onde passamos a funcao que transforma a propriedade que está como string para uma keyword a ser adicionada no mapa
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
            context
            )
   }
  )

;;aqui pode ser um multimetodo
(defn respond-with-json [context payload]
    (http/respond-with context 200 (json/write-str payload))
  )

(defn respond-validation-error-with-json [context errors]
  (http/respond-with context 400 {"Content-Type" "application/json"} (json/write-str errors))
  )


;; {:autores {:chave-primaria dados}}

;; (get-in banco-dados [:autores :chave-primaria])

;;(apply swap! database op args)
(defn insere-tabela [banco-dados nome-tabela chave-primaria dados]
  (assoc-in banco-dados [nome-tabela chave-primaria] dados)
  )

(def schema-novo-autor
  [:map
   [:nome [:string {:min 1 :max 20 :error/message "Nome é obrigatório"}]]
   [:email [:string {:min 1 :error/message "Email inválido"}]]
   [:descricao  [:string {:min 1 :max 100 :error/message "Descricao obrigatoria"}]]
   ])

(defrecord Autor [nome email descricao instante-criacao])

(defn gera-chave-primaira []
  (str (gensym "i"))
  )

(defn converte-linha-autor-saida-lista [autor]
  {
    :nome (:nome autor)
    :email (:email autor)
    :descricao (:descricao autor)
   }
  )

(def lista-autores
  {
    :name :lista-autores
    :enter (fn [context]
        (let [linhas-autores (vals (get-in context [:request :database :autores]))]
           (respond-with-json context (map converte-linha-autor-saida-lista linhas-autores))
          ))
   }
  )

(defn verifica-campo-banco-dados [banco-dados tabela campo valor-buscado]
  ;precisa pegar todos os valores do mapa autores
  ;dentro dos valores buscar se tem algum com email igual

  (let [linhas (vals (get banco-dados tabela))]
    (some #(= valor-buscado (get % campo)) linhas)
    )
  )

(def novo-autor {
                 :name :novo-autor
                 :enter (fn [context]
                          (let [
                                banco-dados (get-in context [:request :database])
                                payload (parse-json-body context)
                                ;aqui eu estou validando duas vezes?
                                valid? (m/validate schema-novo-autor payload)
                                errors (me/humanize (m/explain schema-novo-autor payload))
                                ;;deve ter outro jeito de criar uma funcao para atrasar a execução de um código
                                ja-existe-email-cadastrado (fn [] (verifica-campo-banco-dados banco-dados :autores :email (:email payload)))
                                ]

                            (cond
                              (not valid?) (respond-validation-error-with-json context errors)

                              (ja-existe-email-cadastrado) (respond-validation-error-with-json context {:global-erros ["Já existe autor com email cadastrado"]})

                              :else (let [{:keys [nome email descricao]} payload
                                          instance-criacao (LocalDateTime/now)
                                          id (gera-chave-primaira)
                                          autor-para-salvar (->Autor nome email descricao instance-criacao)]

                                      ;; curioso que o exemplo acha melhor acessar uma variável global do que acessar o atom via parametro
                                      (swap! database insere-tabela :autores id autor-para-salvar)
                                      (respond-with-json (assoc-in context [:request :database] @database) {:id id})

                                      )
                              )
                            )
                          )

                 })



;; configuracoes

;eu quero descobrir pq o argumento de nome da rota não é um mapa
(def routes
  (route/expand-routes
    #{
       ["/autores" :post [db-interceptor novo-autor]]
       ["/autores" :get [db-interceptor lista-autores]]
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





