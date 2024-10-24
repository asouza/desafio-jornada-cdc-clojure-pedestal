(ns exemplo-basico-stateflow
  (:require
    [clojure.test :refer :all]
    [state-flow.api :as api :refer [flow match?]]
  )
  )

  (def get-value (api/get-state :value))
  (api/run get-value {:value 4})
