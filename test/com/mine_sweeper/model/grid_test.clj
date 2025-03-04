(ns com.mine-sweeper.model.grid-test
  (:require
    [com.mine-sweeper.model.grid :as grid]
    [fulcro-spec.core :refer [=> =fn=> assertions behavior specification]]))

(specification "Grid Operations"
  (behavior "vec-of"
    (assertions
      "Creates vector of specified size with factory elements"
      (grid/vec-of 3 #(identity 0)) => [0 0 0]
      "Handles empty size"
      (grid/vec-of 0 #(identity 1)) => []
      "Factory function is called for each element"
      (let [counter (atom 0)]
        (grid/vec-of 2 #(swap! counter inc))) => [1 2]))

  (behavior "vectors-of"
    (assertions
      "Creates 2D vector with specified dimensions"
      (grid/vectors-of #(identity 0) 2 3) => [[0 0 0] [0 0 0]]
      "Creates 1D vector when single dimension"
      (grid/vectors-of #(identity 1) 2) => [1 1]
      "Handles empty dimensions"
      (grid/vectors-of #(identity 0) 0 0) => []))

  (behavior "adjacent-cells"
    (let [test-grid [[{:val 1} {:val 2} {:val 3}]
                     [{:val 4} {:val 5} {:val 6}]
                     [{:val 7} {:val 8} {:val 9}]]]
      (assertions
        "Returns all surrounding cells for center position"
        (count (grid/adjacent-cells test-grid 1 1)) => 9

        "Returns 4 cells for corner position"
        (count (grid/adjacent-cells test-grid 0 0)) => 4

        "Adds x,y coordinates to returned cells"
        (:x (first (grid/adjacent-cells test-grid 1 1))) => 0
        (:y (first (grid/adjacent-cells test-grid 1 1))) => 0

        "Handles edge cases"
        (count (grid/adjacent-cells test-grid 2 2)) => 4)))

  (behavior "random-grid-locations"
    (assertions
      "Generates requested number of unique locations"
      (count (grid/random-grid-locations 10 10 5)) => 5

      "All locations are within bounds"
      (let [locs (grid/random-grid-locations 3 3 4)]
        (every? #(and (< (first %) 3) (< (second %) 3)) locs)) => true

      "The locations appear to be random (two consecutive generations differ)"
      (let [locs1 (grid/random-grid-locations 10 10 5)
            locs2 (grid/random-grid-locations 10 10 5)]
        (not= locs1 locs2)) => true))

  (behavior "grid-locations"
    (assertions
      "Returns all coordinates for given dimensions"
      (count (grid/grid-locations 2 2)) => 4

      "Returns coordinates in correct sequence"
      (grid/grid-locations 1 2) => [[0 0] [0 1]]

      "Returns empty sequence for zero dimensions"
      (grid/grid-locations 0 0) =fn=> empty?)))

(specification "populate-adjacent-counts"
  (let [before    '[[M E E M]
                    [E E E E]
                    [E M E E]]
        mine?     #(= 'M %)
        set-count (fn [cells x y n] (assoc-in cells [x y] n))
        expected  '[[M 1 1 M]
                    [2 2 2 1]
                    [1 M 1 0]]]

    (let [actual (grid/populate-adjacent-counts before mine? set-count)]

      (assertions
        "Populates the correct numbers around the target cells"
        actual => expected))))
