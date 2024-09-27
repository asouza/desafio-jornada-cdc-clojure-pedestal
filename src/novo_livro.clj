(ns novo-livro
  (:require
    [utilitarios]
    [malli.core :as m]
    [malli.error :as me])
  (:import (java.time LocalDateTime))
  )


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


(def handler {
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
                                          id (utilitarios/gera-chave-primaira)
                                          livro-para-salvar (->Livro titulo resumo preco-convertido isbn data-lancamento-convertida id-categoria id-autor instance-criacao)
                                          nova-versao-banco-dados ((:funcao-altera-banco-dados context) (fn [ultima-versao-banco-dados]
                                                                                                          (utilitarios/insere-tabela ultima-versao-banco-dados :livros id livro-para-salvar))
                                                                   )
                                          ]

                                      (utilitarios/respond-with-json (assoc-in context [:request :database] nova-versao-banco-dados) {:id id})

                                      )
                              )
                            )
                          )

                 })
