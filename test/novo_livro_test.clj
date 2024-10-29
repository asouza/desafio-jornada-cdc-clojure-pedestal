(ns novo-livro-test 
  (:require
   [clojure.test :refer :all]
   [state-flow.api :as api :refer [match?]]  
   [novo-livro] 
   [utilitarios-test]
   )
   
  )


#_(- Cria o payload do novo-livro
     - Prepara uma versão do banco com autor e categoria cadastradas
     - Prepara uma versão do banco sem autor e sem categoria cadastrados
     - Prepara uma versão do banco om um livro de mesmo título

     - Faz um teste para o caso de sucesso - ok
     - Faz um teste para o caso de referencia inválida para categoria - ok
     - Faz um teste para o caso de referencia inválida para autor - ok
     - Faz um teste para livro com titulo duplicado
     )

(def payload-livro-com-categoria-autor-validos {:titulo "titulo do livro"
              :resumo "resumo do livro"
              :preco "20.00"
              :isbn "7856745"
              :data-lancamento "2100-10-10"
              :id-categoria 1
              :id-autor 1})

(def payload-livro-com-categoria-invalida {:titulo "titulo do livro"
                                                :resumo "resumo do livro"
                                                :preco "20.00"
                                                :isbn "7856745"
                                                :data-lancamento "2100-10-10"
                                                :id-categoria 2
                                                :id-autor 1})

(def payload-livro-com-autor-invalido {:titulo "titulo do livro"
                                           :resumo "resumo do livro"
                                           :preco "20.00"
                                           :isbn "7856745"
                                           :data-lancamento "2100-10-10"
                                           :id-categoria 1
                                           :id-autor 2})

(def payload-livro-com-titulo-duplicado {:titulo "titulo duplicado"
                                       :resumo "resumo do livro"
                                       :preco "20.00"
                                       :isbn "7856745"
                                       :data-lancamento "2100-10-10"
                                       :id-categoria 1
                                       :id-autor 2})



(def versao-banco #{
                    [1 :categoria/nome "categoria teste"]
                    [1 :autor/nome "autor teste"]
                    [1 :livro/titulo "titulo duplicado"]
                    })

(defn atom-id [] (atom 0))


(api/defflow deveria-cadastrar-um-livro {:init (constantly (utilitarios-test/cria-contexto-com-payload-e-dados payload-livro-com-categoria-autor-validos versao-banco (atom-id)))}
  [resposta (api/get-state utilitarios-test/executa-handler novo-livro/handler) ]
  
  (match? {:status 200} resposta)
  (match? 200 (:status resposta))
  (match? {:id [1]} (:body resposta)) 
  )

(api/defflow nao-deve-cadastrar-com-categoria-invalida {:init (constantly (utilitarios-test/cria-contexto-com-payload-e-dados payload-livro-com-categoria-invalida versao-banco (atom-id)))}
  [resposta (api/get-state utilitarios-test/executa-handler novo-livro/handler)]
  (match? 400 (:status resposta)) 
  (match? {:global-errors ["Nao existe a categoria referenciada"]} (:body resposta)))

(api/defflow nao-deve-cadastrar-com-autor-invalido {:init (constantly (utilitarios-test/cria-contexto-com-payload-e-dados payload-livro-com-autor-invalido versao-banco (atom-id)))}
  [resposta (api/get-state utilitarios-test/executa-handler novo-livro/handler)]
  (match? 400 (:status resposta))
  (match? {:global-errors ["Nao existe o autor referenciado"]} (:body resposta)))

(api/defflow nao-deve-cadastrar-com-titulo-duplicado {:init (constantly (utilitarios-test/cria-contexto-com-payload-e-dados payload-livro-com-titulo-duplicado versao-banco (atom-id)))}
  [resposta (api/get-state utilitarios-test/executa-handler novo-livro/handler)]
  (match? 400 (:status resposta))
  (match? {:global-errors ["Ja existe um livro com o mesmo titulo"]} (:body resposta)))


    






