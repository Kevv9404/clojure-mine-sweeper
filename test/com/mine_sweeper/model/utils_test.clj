(ns com.mine-sweeper.model.utils-test
  (:require [com.mine-sweeper.model.utils :as utils]
            [fulcro-spec.core :refer [=> assertions component behavior specification when-mocking]]))

(specification "vec-of"
               (assertions
                 (utils/vec-of 5 (constantly "X")) => ["X" "X" "X" "X" "X"]))

(specification "vectors-of"
               (assertions
                 (utils/vectors-of (fn [] {:hidden true}) 10 2) => (into [] (repeat 10 [{:hidden true} {:hidden true}]))))
