(ns common-schema
  (:require
   [schema.core :as s]
   [schema.coerce :as coerce]
)  
(:import
  [java.time LocalDate]
  [java.time.format DateTimeFormatter])
  )  
  

(defn- localdate-matcher [schema]
  (if (= schema LocalDate)
    (fn [value]
      (LocalDate/parse value (DateTimeFormatter/ofPattern "yyyy-MM-dd")))
    nil))

(defn- bigdecimal-matcher [schema]
  (if (= schema java.math.BigDecimal)
    (fn [value]
      (java.math.BigDecimal. value))

    nil))

(defn coerce [schema dados]
  (let [
    ;aqui poderia cachear  a funcao de coercao    
    coercer  (coerce/coercer schema (fn [schema]
                                     (or
                                      (coerce/json-coercion-matcher schema)
                                      (localdate-matcher schema)
                                      (bigdecimal-matcher schema))))    
  ]
    (coercer dados)
    )
  )

