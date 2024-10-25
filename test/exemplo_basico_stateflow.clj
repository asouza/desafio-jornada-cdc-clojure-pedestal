(ns exemplo-basico-stateflow
  (:require
   [clojure.test :refer :all]
   [state-flow.api :as api :refer [flow match?]]
   ))

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
        get-value
        inc-value
        get-value))

(api/run inc-two {:value 4})

(def with-bindings-teste
  (flow "get double value"
        ;; inc-value
        [value get-value
         :let [value2 (* 3 value)]]
        get-value
        (api/return value2) 
        ))

(api/run with-bindings-teste {:value 4 :outro 8})


(def with-assertions
  (flow "with assertions"
        inc-value
        [value get-value]
        (match? 5 value)

        inc-value
        [world (api/get-state identity)]
        (match? 7 get-value)
        (match? 1 (count world))))

(api/run* {:fail-fast? false :init (constantly {:value 4})} with-assertions)

(def for-flow
  (flow "even? returns true for even numbers"
        (api/for [x (filter even? (range 10))]
          (match? even? x))))

(api/run for-flow)    

(deftest fruits-and-veggies
  (clojure.test/testing "bla"
    (flow "surprise! Tomatoes are fruits!"
          (match? #{:tomato} #{:tomato :potato}))))


(api/defflow teste-flow
  (match? 1 1)
  )

(deftest teste
  (clojure.test/is (= 5 5) "mensagem"))