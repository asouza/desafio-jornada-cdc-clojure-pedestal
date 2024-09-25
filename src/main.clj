(ns main (:require [io.pedestal.http :as http]
                   [io.pedestal.http.route :as route]
                   [io.pedestal.test :as test]
                   [clojure.data.json :as json]
                   [malli.core :as m]
                   [malli.error :as me]
                   [malli.experimental.time :as met]
                   [malli.registry :as mr]
                   )
  (:import [java.time LocalDate LocalDateTime]
           (java.time.format DateTimeFormatter))
  )

;adicionando o schema de validação de datas
(mr/set-default-registry!
  (mr/composite-registry
    (m/default-schemas)
    (met/schemas)))


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

(defn verifica-campo-banco-dados [banco-dados tabela campo valor-buscado]
  ;precisa pegar todos os valores do mapa autores
  ;dentro dos valores buscar se tem algum com email igual

  (let [linhas (vals (get banco-dados tabela))]
    (some #(= valor-buscado (get % campo)) linhas)
    )
  )

(defn verifica-existencia-pk [banco-dados tabela campo-pk valor-buscado]
  ;precisa pegar todos os valores do mapa autores
  ;dentro dos valores buscar se tem algum com email igual
  (= valor-buscado (get-in banco-dados [(keyword tabela) (keyword campo-pk)]))
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

;chatgpt me disse que eu poderia ter explodido aqui nas tres variáveis que eu criei.
;Ao mesmo tempo disse que meu código está ok
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

(def schema-nova-categoria
  [:map
   [:nome [:string {:min 1 :max 20 :error/message "Nome é obrigatório"}]]
   [:descricao  [:string {:min 1 :max 100 :error/message "Descricao obrigatoria"}]]
   ])

(defrecord Categoria [nome descricao instante-criacao])

(def nova-categoria {
                 :name :nova-categoria
                 :enter (fn [context]
                          (let [
                                banco-dados (get-in context [:request :database])
                                payload (parse-json-body context)
                                ;aqui eu estou validando duas vezes?
                                valid? (m/validate schema-nova-categoria payload)
                                errors (me/humanize (m/explain schema-nova-categoria payload))
                                ;;deve ter outro jeito de criar uma funcao para atrasar a execução de um código
                                ja-existe-nome-cadastrado (fn [] (verifica-campo-banco-dados banco-dados :categorias :nome (:nome payload)))
                                ]

                            (cond
                              (not valid?) (respond-validation-error-with-json context errors)

                              (ja-existe-nome-cadastrado) (respond-validation-error-with-json context {:global-erros ["Já existe categoria com o nome passado"]})

                              :else (let [{:keys [nome descricao]} payload
                                          instance-criacao (LocalDateTime/now)
                                          id (gera-chave-primaira)
                                          categoria-para-salvar (->Categoria nome descricao instance-criacao)]

                                      ;; curioso que o exemplo acha melhor acessar uma variável global do que acessar o atom via parametro
                                      (swap! database insere-tabela :categorias id categoria-para-salvar)
                                      (respond-with-json (assoc-in context [:request :database] @database) {:id id})

                                      )
                              )
                            )
                          )

                 })




(defn valid-date? [pattern data-string]
  (try
    (let [formatter (DateTimeFormatter/ofPattern pattern)]
      (LocalDate/parse data-string formatter)
      )
    (catch Exception _ ;; Se der erro ao converter, retorna nulo
      nil)))

(defn future-date? [pattern data-string]
  (try
    (let [formatter (DateTimeFormatter/ofPattern pattern)
          parsed-date (LocalDate/parse data-string formatter)]
      (.isAfter parsed-date (LocalDate/now))) ;; Verifica se a data é depois da data atual
    (catch Exception _ ;; Se der erro ao converter retorna true, seguindo a ideia da bean validation
      true)))

(defn decimal-string? [valor-string]
  (try
    (BigDecimal. valor-string)
    true
    (catch Exception _ ;; Captura qualquer exceção lançada pela tentativa de conversão
      false)))

(defn decimal-greater-than? [min valor-string]

    (if (and (decimal-string? min) (decimal-string? valor-string))
      (let [decimal-value (BigDecimal. valor-string)]
        (> (.compareTo decimal-value (BigDecimal. min)) 0)
        );; Compara os decimais

      ;se tiver algum erro de conversao, ignora. Alguém deveria ter tratado.
      true
      )
  )

(def schema-basico-novo-livro
  [:map
   [:titulo [:string {:min 1 :max 20 :error/message "Titulo é obrigatório"}]]
   [:resumo  [:string {:min 1 :max 500 :error/message "Resumo é obrigatório"}]]
   [:preco  [:and
             [string? {:min 1 :error/message "Preço é obrigatório"}]
             [:fn {:error/message "Preco não está bem formatado"} #(decimal-string? %)]
             [:fn {:error/message "Preço precisa ser maior que zerp"} #(decimal-greater-than? 0 %)]
             ]]
   [:isbn  [:string {:min 1 :max 500 :error/message "ISBN é obrigatório"}]]
   ;pq eu não posso chamar a função que retorna o array
   [:data-lancamento  [:and
                       [:string {:min 1 :error/message "Data é obrigatória"}]
                       ;;na documentacao explica que quando usa funcao, as propriedades tem que ser passadas primeiro
                       [:fn {:error/message "Data mal formatada"} #(valid-date? "yyyy-MM-dd" %)]
                       [:fn {:error/message "Data não está no futuro"} #(future-date? "yyyy-MM-dd" %)]]]
   [:id-categoria  [:string {:error/message "Categoria é obrigatória"}]]
   [:id-autor  [:string {:error/message "Autor é obrigatório"}]]])



(defrecord Livro [titulo resumo preco isbn data-lancamento id-categoria id-autor instante-criacao])


(def novo-livro {
                     :name :novo-livro
                     :enter (fn [context]
                              (let [
                                    banco-dados (get-in context [:request :database])
                                    payload (parse-json-body context)
                                    ;aqui eu estou validando duas vezes?
                                    validacao-basica? (m/validate schema-basico-novo-livro payload)
                                    errors (me/humanize (m/explain schema-basico-novo-livro payload))
                                    ;;deve ter outro jeito de criar uma funcao para atrasar a execução de um código
                                    ja-existe-titulo-cadastrado (fn [] (verifica-campo-banco-dados banco-dados :livros :titulo (:titulo payload)))
                                    existe-categoria (fn [] (verifica-existencia-pk banco-dados :categorias :id (:id-categoria payload)))
                                    existe-autor (fn [] (verifica-existencia-pk banco-dados :autores :id (:id-autor payload)))
                                    ]

                                (cond
                                  (not validacao-basica?) (respond-validation-error-with-json context errors)

                                  :else (respond-with-json (assoc-in context [:request :database] @database) {:id 1})

                                  ;(ja-existe-nome-cadastrado) (respond-validation-error-with-json context {:global-erros ["Já existe categoria com o nome passado"]})

                                  ;:else (let [{:keys [nome descricao]} payload
                                  ;            instance-criacao (LocalDateTime/now)
                                  ;            id (gera-chave-primaira)
                                  ;            categoria-para-salvar (->Categoria nome descricao instance-criacao)]
                                  ;
                                  ;        ;; curioso que o exemplo acha melhor acessar uma variável global do que acessar o atom via parametro
                                  ;        (swap! database insere-tabela :categorias id categoria-para-salvar)
                                  ;        (respond-with-json (assoc-in context [:request :database] @database) {:id id})
                                  ;
                                  ;        )
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
       ["/categorias" :post [db-interceptor nova-categoria]]
       ["/livros" :post [db-interceptor novo-livro]]
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





