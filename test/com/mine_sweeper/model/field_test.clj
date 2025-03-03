(ns com.mine-sweeper.model.field-test
  (:require [clojure.test :refer :all]
            [com.mine-sweeper.model.field :as field]
            [fulcro-spec.core :refer [=> assertions specification]]))


(specification "build-grid" :focus
  (assertions
    "When number of rows is equal to height"
    (count (field/build-grid 2 3)) => 3
    "When number of cell is equal to width"
    (into #{} (mapv count (field/build-grid 2 3))) => #{2}
    "When given negative args, returns a empty vector"
    (field/build-grid -1 -3) => []))

(specification "initialize-mine-counts" :focus
  (let [M        (field/set-mine (field/empty-cell))
        c0       (field/set-adjacent-content (field/empty-cell) 0)
        c1       (field/set-adjacent-content (field/empty-cell) 1)
        c2       (field/set-adjacent-content (field/empty-cell) 2)
        c3       (field/set-adjacent-content (field/empty-cell) 3)
        E        (field/empty-cell)
        grid     [[M E E M]
                  [E M E M]
                  [E E E E]
                  [E E M E]]
        expected [[M c2 c3 M]
                  [c2 M c3 M]
                  [c1 c2 c3 c2]
                  [c0 c1 M c1]]]
    (assertions
      (:mine-field/grid
        (field/initialize-mine-counts {:mine-field/width  4
                                       :mine-field/height 4
                                       :mine-field/grid   grid})) => expected)))
