(ns com.mine-sweeper.model.field-test
  (:require [clojure.test :refer :all]
            [com.mine-sweeper.model.field :as field]
            [fulcro-spec.core :refer [=> assertions component behavior specification when-mocking]]))


(specification "build-grid" :focus
               (assertions
                 "When number of rows is equal to height"
                 (count (field/build-grid 2 3)) => 3
                 "When number of cell is equal to width"
                 (into #{} (mapv count (field/build-grid 2 3))) => #{2}
                 "When given negative args, returns a empty vector"
                 (field/build-grid -1 -3) => []))
