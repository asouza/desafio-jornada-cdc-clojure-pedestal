(ns utilitarios
  (:require
    [clojure.data.json :as json]
    [io.pedestal.http :as http]
    )
  (:import [java.util Date]
          [java.time LocalDate ZoneId Instant])
  )

(defn local-date->date [local-date]
  (Date/from (.toInstant (.atStartOfDay local-date (ZoneId/systemDefault)))))

(defn gera-chave-primaira []
  (str (gensym "i"))
  )

;;##paraTreinar aqui eu posso usar o lance das specs, para definir bem a entrada. Brincar de pre e pos condicoes
(defn parse-json-body [context]
  (let [body (slurp (get-in context [:request :body]))] ;; Converte o corpo para string
    ;:key-fn é por onde passamos a funcao que transforma a propriedade que está como string para uma keyword a ser adicionada no mapa
    (json/read-str body :key-fn keyword))) ;; Decodifica o JSON

;;aqui pode ser um multimetodo
(defn respond-with-json [context payload]
  (http/respond-with context 200 (json/write-str payload))
  )

(defn respond-validation-error-with-json [context errors]
  (http/respond-with context 400 {"Content-Type" "application/json"} (json/write-str errors))
  )

(defn respond-with-status [context status]
  (http/respond-with context status)
  )

;; {:autores [{:id 1 :dados {}} {:id 2 :dados {}}]}

;; (find #(= id-buscado (:id autor) (get-in banco-dados :autores))

;;(apply swap! database op args)
(defn insere-tabela [banco-dados nome-tabela chave-primaria dados]
  ;;fnil retorna uma funcao multi-method. Quando ela for chamada, vai chamar a funcao conj passando o valor recebido no mapa(ou [] em caso de nil + o segundo argumento)
  ;;aqui pode ser assoc usando a chave primaria como chave mesmo
  ;;(assoc-in banco-dados [nome-tabela chave-primaria] (assoc dados :id chave-primaria))
  (update banco-dados nome-tabela (fnil conj []) (assoc dados :id chave-primaria))
  )

(defn verifica-campo-banco-dados [banco-dados tabela campo valor-buscado]
  ;precisa pegar todos os valores do mapa autores
  ;dentro dos valores buscar se tem algum com email igual
  (println "verifica-campo-banco-dados")
  ;(println banco-dados)
  (let [linhas (get banco-dados tabela)]
    (some #(= valor-buscado (get % campo)) linhas)
    )
  )

(defn busca-item-por-campo [banco-dados tabela campo-para-igualdade valor]
  (let [linhas (get banco-dados tabela)]
    ;;aqui precisa de pos condicao para achar um so
    (first (filter #(= valor (get % campo-para-igualdade)) linhas))
    )
  )

(defn busca-todos-itens-por-campo [banco-dados tabela campo-para-igualdade valor]
  (let [linhas (get banco-dados tabela)]
    ;;aqui precisa de pos condicao para achar um so
    (filter #(= valor (get % campo-para-igualdade)) linhas)
    )
  )


(defn executa-transacao
  "Executa uma transacao supondo que o context passado tem uma chave [:request :funcao-transacao]
  que retorna uma função que recebe um mapa como argumento."
  [context mapa]

  (let [
        funcao-transacao (get-in context [:request :funcao-transacao])
        ]
      (funcao-transacao mapa)
    )
  )
