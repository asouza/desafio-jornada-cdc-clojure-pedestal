(ns exemplos-schema
  (:require
   [schema.core :as s]
   [schema.coerce :as coerce]
   [schema.utils :as u]
   [schema.spec.core :as spec]
            ;;   :include-macros true ;; cljs only
   )
(:import 
       [java.time LocalDate]
        [java.time.format DateTimeFormatter])  
  )



(s/defschema Data
  "A schema for a nested data type"
  {:a {:b s/Str
       :c s/Int}
   :d [{:e s/Keyword
        :f [s/Num]}]})

(def resultad-ok (s/check
                  Data
                  {:a {:b "abc"
                       :c 123}
                   :d [{:e :bc
                        :f [12.2 13 100]}
                       {:e :bc
                        :f [-1]}]}))

;validate solta exception e check retorna os erros(vou querer os erros normalmente)
(s/validate
 Data
 {:a {:b "123"
      :c 2}})

(def resultado-com-erro 
                         (s/check
                          Data
                          {:a {:b "123"
                               :c "2"}})
                         ) 

(defn formatar-erros [resultado]
  (for [[chave erro] resultado]
    (str "Erro em " (name chave) ": esperado "
         (s/explain (get Data chave)) ", mas recebeu " erro)))

(println (formatar-erros resultado-com-erro))

(defn not-empty-string? [min max]
  (fn [s]
    (and (string? s) (>= (count s) min) (<= (count s) max))))

(s/defschema NovoAutorRequest
  {
   :nome (s/constrained s/Str (not-empty-string? 1 20) "Nome não pode ser vazio e precisa ter no máximo 20 caracteres")
   :email (s/constrained (s/pred string?) (not-empty-string? 1 20) "Email é obrigatório")
   
})



(def resultado-validacao-autor (s/check
                                NovoAutorRequest
                                {:nome 1
                                 :email ""}))

(println resultado-validacao-autor)



(type (:nome resultado-validacao-autor))

(u/validation-error-explain (:nome resultado-validacao-autor))
@(.-expectation-delay (:nome resultado-validacao-autor))
(.-value (:nome resultado-validacao-autor))
(.-schema (:nome resultado-validacao-autor))
@(.-expectation-delay (:email resultado-validacao-autor))

(defn localdate-matcher [schema]
  (if (= schema LocalDate)
    (fn [value]
      (LocalDate/parse value (DateTimeFormatter/ofPattern "yyyy-MM-dd"))
      )
    
    nil
    )
)

(defn bigdecimal-matcher [schema]
  (if (= schema java.math.BigDecimal)
    (fn [value]
      (java.math.BigDecimal. value))

    nil))


(s/defschema CommentRequest
  {(s/optional-key :parent-comment-id) long
   :text s/Str
   :chave s/Keyword
   :valor java.math.BigDecimal
   :data java.time.LocalDate
   :share-services [(s/enum :twitter :facebook :google)]})

(println (coerce/json-coercion-matcher s/Int))

(def parse-comment-request
  ;; (coerce/coercer CommentRequest coerce/json-coercion-matcher)
  (coerce/coercer CommentRequest (fn [schema] 
                                   (or
                                     (coerce/json-coercion-matcher schema)
                                     (localdate-matcher schema)
                                     (bigdecimal-matcher schema)
                                    )
                                   ) 
                  ))

(def coerced (parse-comment-request
              {:parent-comment-id (int 2128123123)
               :valor 20.50
               :chave "bla"
               :text "This is awesome!"
               :data "2024-12-12"
               :share-services ["twitter" "facebook"]}))
(println coerced)

(= coerced
   
   {:parent-comment-id 2128123123
    :text "This is awesome!"
    :share-services [:twitter :facebook]}
   )




