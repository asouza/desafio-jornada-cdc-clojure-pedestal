(ns valida-email
  (:require
    [schema-refined.core :as r]
    [schema.core :as s]
   )
  )

(defn- match-regex?
  "Check if the string matches the regex"
  [v regex]
  (boolean (re-matches regex v)))

(defn- is-email?
  "Check if input is a valid email address"
  [input]
  (if (nil? input)
    false
    (match-regex? input #"(?i)[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*@(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?")))

(def Email (r/refined s/Str (r/->FunctionPredicate is-email?)))
