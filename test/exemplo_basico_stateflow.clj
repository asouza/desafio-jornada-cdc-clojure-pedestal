(ns exemplo-basico-stateflow
  (:require
   [clojure.test :refer :all]
   [state-flow.api :as api :refer [flow match?]]))

(def get-value (api/get-state :value))
(api/run get-value {:value 4})

(def inc-value (api/swap-state #(update % :value inc)))
(api/run inc-value {:value 4})

(def my-first-flow
  (flow "my first flow"
        (flow "bla" inc-value)))

(api/run my-first-flow {:value 4})

(def inc-two
  (flow "inc 2 times"
        inc-value
        inc-value))

(api/run inc-two {:value 4})

(def with-bindings
  (flow "get double value"
        inc-value
        [value get-value
         :let [value2 (* 2 value)]]
        (api/return value2)))

(api/run with-bindings {:value 4})

(def with-assertions
  (flow "with assertions"
        inc-value
        [value get-value]
        (match? 5 value)

        inc-value
        [world (api/get-state identity)]
        (match? 7 get-value)))

(api/run with-assertions {:value 4})