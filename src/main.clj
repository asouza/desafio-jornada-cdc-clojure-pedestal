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
           (utilitarios/respond-with-json context (map converte-linha-autor-saida-lista linhas-autores))
          ))
   }
  )


(def novo-autor {
                 :name :novo-autor
                 :enter (fn [context]
                          (let [
                                banco-dados (get-in context [:request :database])
                                payload (utilitarios/parse-json-body context)
                                ;aqui eu estou validando duas vezes?
                                valid? (m/validate schema-novo-autor payload)
                                errors (me/humanize (m/explain schema-novo-autor payload))
                                ;;deve ter outro jeito de criar uma funcao para atrasar a execução de um código
                                ja-existe-email-cadastrado (fn [] (utilitarios/verifica-campo-banco-dados banco-dados :autores :email (:email payload)))
                                ]

                            (cond
                              (not valid?) (utilitarios/respond-validation-error-with-json context errors)

                              (ja-existe-email-cadastrado) (utilitarios/respond-validation-error-with-json context {:global-erros ["Já existe autor com email cadastrado"]})

                              :else (let [{:keys [nome email descricao]} payload
                                          instance-criacao (LocalDateTime/now)
                                          id (gera-chave-primaira)
                                          autor-para-salvar (->Autor nome email descricao instance-criacao)]

                                      ;; curioso que o exemplo acha melhor acessar uma variável global do que acessar o atom via parametro
                                      (swap! database utilitarios/insere-tabela :autores id autor-para-salvar)
                                      (utilitarios/respond-with-json (assoc-in context [:request :database] @database) {:id id})

                                      )
                              )
                            )
                          )

                 })

(def schema-nova-categoria
  [:map
   [:nome [:string {:min 1 :max 20 :error/message "Nome é obrigatório"}]]
   [:descricao [:string {:min 1 :max 100 :error/message "Descricao obrigatoria"}]]
   ])

(defrecord Categoria [nome descricao instante-criacao])

(def nova-categoria {
                     :name  :nova-categoria
                     :enter (fn [context]
                              (let [
                                    banco-dados (get-in context [:request :database])
                                    payload (utilitarios/parse-json-body context)
                                    ;aqui eu estou validando duas vezes?
                                    valid? (m/validate schema-nova-categoria payload)
                                    errors (me/humanize (m/explain schema-nova-categoria payload))
                                    ;;deve ter outro jeito de criar uma funcao para atrasar a execução de um código
                                    ja-existe-nome-cadastrado (fn [] (utilitarios/verifica-campo-banco-dados banco-dados :categorias :nome (:nome payload)))
                                    ]

                                (cond
                                  (not valid?) (utilitarios/respond-validation-error-with-json context errors)

                                  (ja-existe-nome-cadastrado) (utilitarios/respond-validation-error-with-json context {:global-erros ["Já existe categoria com o nome passado"]})

                              :else (let [{:keys [nome descricao]} payload
                                          instance-criacao (LocalDateTime/now)
                                          id (gera-chave-primaira)
                                          categoria-para-salvar (->Categoria nome descricao instance-criacao)]

                                      ;; curioso que o exemplo acha melhor acessar uma variável global do que acessar o atom via parametro
                                      (swap! database utilitarios/insere-tabela :categorias id categoria-para-salvar)
                                      (utilitarios/respond-with-json (assoc-in context [:request :database] @database) {:id id})

                                      )
                              )
                            )
                          )

                 })






(def schema-basico-novo-livro
  [:map
   [:titulo [:string {:min 1 :max 20 :error/message "Titulo é obrigatório"}]]
   [:resumo  [:string {:min 1 :max 500 :error/message "Resumo é obrigatório"}]]
   [:preco  [:and
             [string? {:min 1 :error/message "Preço é obrigatório"}]
             [:fn {:error/message "Preco não está bem formatado"} #(validacoes-conversoes/decimal-string? %)]
             [:fn {:error/message "Preço precisa ser maior que zerp"} #(validacoes-conversoes/decimal-greater-than? 0 %)]
             ]]
   [:isbn  [:string {:min 1 :max 500 :error/message "ISBN é obrigatório"}]]
   ;pq eu não posso chamar a função que retorna o array
   [:data-lancamento  [:and
                       [:string {:min 1 :error/message "Data é obrigatória"}]
                       ;;na documentacao explica que quando usa funcao, as propriedades tem que ser passadas primeiro
                       [:fn {:error/message "Data mal formatada"} #(validacoes-conversoes/valid-date? "yyyy-MM-dd" %)]
                       [:fn {:error/message "Data não está no futuro"} #(validacoes-conversoes/future-date? "yyyy-MM-dd" %)]]]
   [:id-categoria  [:string {:min 1 :error/message "Categoria é obrigatória"}]]
   [:id-autor  [:string {:min 1 :error/message "Autor é obrigatório"}]]])



(defrecord Livro [titulo resumo preco isbn data-lancamento id-categoria id-autor instante-criacao])


(def novo-livro {
                     :name :novo-livro
                     :enter (fn [context]
                              (let [
                                    banco-dados (get-in context [:request :database])
                                    payload (utilitarios/parse-json-body context)
                                    ;aqui eu estou validando duas vezes?
                                    dados-basicos-estao-validos? (m/validate schema-basico-novo-livro payload)
                                    errors (me/humanize (m/explain schema-basico-novo-livro payload))
                                    ;;deve ter outro jeito de criar uma funcao para atrasar a execução de um código
                                    ja-existe-titulo-cadastrado (fn [] (utilitarios/verifica-campo-banco-dados banco-dados :livros :titulo (:titulo payload)))
                                    existe-categoria? (fn [] (utilitarios/verifica-campo-banco-dados banco-dados :categorias :id (:id-categoria payload)))
                                    existe-autor? (fn [] (utilitarios/verifica-campo-banco-dados banco-dados :autores :id (:id-autor payload)))
                                    ]

                                (cond
                                  (not dados-basicos-estao-validos?) (utilitarios/respond-validation-error-with-json context errors)
                                  ;
                                  (ja-existe-titulo-cadastrado) (utilitarios/respond-validation-error-with-json context {:global-erros ["Já existe um livro com o mesmo título"]})

                                  (not (existe-categoria?)) (utilitarios/respond-validation-error-with-json context {:global-erros ["Não existe a categoria referenciada"]})

                                  (not (existe-autor?)) (utilitarios/respond-validation-error-with-json context {:global-erros ["Não existe o autor referenciado"]})


                                  :else (let [{:keys [titulo resumo preco isbn data-lancamento id-categoria id-autor]} payload
                                              data-lancamento-convertida (validacoes-conversoes/valid-date? "yyyy-MM-dd" data-lancamento)
                                              preco-convertido (validacoes-conversoes/decimal-string? preco)
                                              instance-criacao (LocalDateTime/now)
                                              id (gera-chave-primaira)
                                              livro-para-salvar (->Livro titulo resumo preco-convertido isbn data-lancamento-convertida id-categoria id-autor instance-criacao)]

                                          ;; curioso que o exemplo acha melhor acessar uma variável global do que acessar o atom via parametro
                                          (swap! database utilitarios/insere-tabela :livros id livro-para-salvar)
                                          (utilitarios/respond-with-json (assoc-in context [:request :database] @database) {:id id})

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
       ["/categorias" :post [db-interceptor nova-categoria]]
       ["/livros" :post [db-interceptor novo-livro]]
       ["/livros" :get [db-interceptor lista-livros/handler]]
       ["/livros/:id" :get [db-interceptor detalhe-livro/handler]]
       ["/paises" :post [db-interceptor novo-pais/handler]]
       ["/paises/:id-pais/estados" :post [db-interceptor novo-estado/handler]]
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





