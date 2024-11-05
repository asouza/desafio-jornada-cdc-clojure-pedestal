(ns carrinho
  (:require
    [utilitarios]
    [malli.core :as m]
    [malli.error :as me]
    [datomic-lib]
    [datomic.api :as d]
    [datomic-schema-carrinho]
    [schema.core :as s]
    [common-schema]
    [schema-refined.core :as r]
   )
  (:import (java.time LocalDateTime)))
  

;validar que chegou um array de itens
;https://github.com/metosin/malli?tab=readme-ov-file#vector-schemas
  ;tem que ter no mínimo um item no array
  ;cada item do array precisa ter uma quantidade maior que zero
  ;cada id referenciado precisa de fato existir
  ;nao pode ter id livro repetido no array

;construção do mapa do carrinho em si
  ;para cada item do array, transforma para um item com o id do livro, preco original, titulo e valor calculado
  ;adiciona o item copiando as informacoes do livro e o montante


(defn- transforma-item-carrinho [banco-dados payload]
    (map (fn [item]
            (let [
                    livro (utilitarios/busca-item-por-campo banco-dados :livros :id (:id-livro item))]
                  
                {
                   :id-livro (:id livro)
                   :preco-original (:preco livro)
                   :titulo (:titulo livro)
                   :valor-calculado (* (:preco livro) (:quantidade item))}))
                 
              
           
         payload))
         


(def schema-novo-carrinho
  [:vector {:min 1}
   [:map
    [:id-livro [:int {:min 1 :error/message "Id do livro precisa ser um numero"}]]
    ;[:quantidade  [:and [:int {:error/message "Quantidade precisa ser um inteiro"}] [:> 0] ]]
    [:quantidade  [:int {:min 1 :error/message "Quantidade precisa ser um inteiro"}]]]])


;; (s/defschema NovoCarrinho 
;;   {
;;    :items (s/constrained 
;;            {
;;             :id-livro (r/refined s/Int (r/Greater  0))
;;             :quantidade (r/refined s/Int (r/Greater  0))
;;             }
;;            #(seq %))
;;    }
;;   )

(s/defschema NovoCarrinho
  {:items [         
           {:id-livro (r/refined s/Int (r/Greater  0))
            :quantidade (r/refined s/Int (r/Greater  0))
            }]
           })

(s/validate NovoCarrinho {:items [{:id-livro 17592186045459, :quantidade 3} {:id-livro 17592186045461, :quantidade 4}]})

(defn- mapeia-item-para-id-livro [item]
  (:id-livro item))
  

(defn- todos-ids-livros-existem [banco-dados payload]
  (let [
          ids-livros-payload (map mapeia-item-para-id-livro payload)
          ids-encontrados (d/pull-many banco-dados '[:db/id] ids-livros-payload)]
        
    (= (count ids-encontrados) (count ids-livros-payload))))
    

  


(defn- todos-ids-livros-diferentes? [payload]
  (let [ids (map (fn [item]
                   (:id-livro item))
                   
                 payload)]
    (= (count ids) (count (set ids)))))
  

(defn- carrega-livro-por-id [dados]
  ;vai retornar a funcao faltando o argumento do id
  (partial datomic-lib/busca-todos-atributos-entidade dados))

(s/defn ^:always-validate logica-novo-carrinho [context 
                            payload :- NovoCarrinho
                            db
                            executa-transacao] 
  {
   :pre [(seq (:items payload))]
  }
 (cond
  
  (not (todos-ids-livros-diferentes? payload)) (utilitarios/respond-validation-error-with-json context {:global-erros ["Tem livro igual"]})

  (not (todos-ids-livros-existem db payload)) (utilitarios/respond-validation-error-with-json context {:global-erros ["Tem livro referenciado que nao existe"]})

  :else (let [id-carrinho (java.util.UUID/randomUUID)
              carrinho (datomic-schema-carrinho/to-schema id-carrinho (:items payload) (carrega-livro-por-id db))
              id-carrinho-salvo (executa-transacao [carrinho])]


          (utilitarios/respond-with-json context {:id id-carrinho-salvo})))  
  )
  

(defn handler-passo-1 [{:keys [:datomic]}] {
                      :name :carrinho-passo-1

                                            ;aqui eu preciso wraper no handler de exception
                      :enter (fn [context]
                              (let [payload (get-in context [:request :json-params])
                                    coerced-payload (common-schema/coerce NovoCarrinho payload)]
                                
                                (println payload)
                                (println coerced-payload)
                                (logica-novo-carrinho context coerced-payload (:db datomic) (:funcao-transacao datomic))
                                ))})
                      
              

            
     


