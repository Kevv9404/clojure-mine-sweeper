(ns com.mine-sweeper.model.field-test
  (:require [clojure.test :refer :all]
            [com.mine-sweeper.model.cell :as cell]
            [com.mine-sweeper.model.field :as field]
            [fulcro-spec.core :refer [=> assertions specification]]))

(specification "expose-all-cells"
  (let [M             (cell/set-mine (cell/empty-cell))
        E             (cell/empty-cell)
        grid          [[M E E]
                       [E M E]
                       [E E M]]
        mine-field    {:mine-field/width  3
                       :mine-field/height 3
                       :mine-field/grid   grid}
        exposed-field (field/expose-all-cells mine-field)]

    (assertions
      "All cells should be exposed (hidden? = false)"
      (every? (fn [row]
                (every? #(false? (:cell/hidden? %)) row))
        (:mine-field/grid exposed-field)) => true

      "Grid structure and content should remain unchanged"
      (= (mapv #(mapv :cell/content %) (:mine-field/grid exposed-field))
        (mapv #(mapv :cell/content %) grid)) => true)))
