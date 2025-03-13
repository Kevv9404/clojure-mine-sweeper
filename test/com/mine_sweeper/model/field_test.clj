(ns com.mine-sweeper.model.field-test
  (:require [com.mine-sweeper.model.cell :as cell]
            [com.mine-sweeper.model.field :as field]
            [com.mine-sweeper.model.utils-test :refer :all]
            [fulcro-spec.core :refer [=> assertions specification]]))


(specification "build-grid"

  (assertions
    "When number of rows is equal to height"
    (count (field/build-grid 2 3)) => 3
    "When number of cell is equal to width"
    (into #{} (mapv count (field/build-grid 2 3))) => #{2}
    "When given negative args, returns a empty vector"
    (field/build-grid -1 -3) => []))

(specification "initialize-mine-counts"
  (let [M        (cell/set-mine (field/empty-cell))
        c0       (cell/set-adjacent-content (field/empty-cell) 0)
        c1       (cell/set-adjacent-content (field/empty-cell) 1)
        c2       (cell/set-adjacent-content (field/empty-cell) 2)
        c3       (cell/set-adjacent-content (field/empty-cell) 3)
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




(specification "reveal-cell" :focus
  (let [M          (cell/set-mine (field/empty-cell))
        E          (field/empty-cell)
        C0         (cell/set-adjacent-content (field/empty-cell) 0)
        C1         (cell/set-adjacent-content (field/empty-cell) 1)
        C2         (cell/set-adjacent-content (field/empty-cell) 2)
        C3         (cell/set-adjacent-content (field/empty-cell) 3)
        grid       [[M E E M E E]
                    [E M E M E E]
                    [E E E E M E]
                    [E E E E E M]
                    [E E E E E E]
                    [E E E E E E]]
        mine-field (field/initialize-mine-counts
                     {:mine-field/width  6
                      :mine-field/height 6
                      :mine-field/grid   grid})
        expected   [[M C2 C3 M C2 C0]
                    [C2 M C3 M C3 C1]
                    [C1 C1 C2 C2 M C2]
                    [C0 C0 C0 C1 C2 M]
                    [C0 C0 C0 C0 C1 C1]
                    [C0 C0 C0 C0 C0 C0]]]
    (assertions

      "Reveals only the numbered cell when clicked"
      (get-in (field/reveal-cell mine-field [1 2]) [:mine-field/grid 1 2]) => C3

      "When revealing a revealed cell, returns the same field"
      (let [revealed-cell (field/reveal-cell mine-field [1 2])]
        (= revealed-cell (field/reveal-cell revealed-cell [1 2]))) => true

      "When revealing a flagged cell, returns the same field"
      (let [field-with-flagged-cell (update-in mine-field [:mine-field/grid 1 2] cell/set-flag)
            result                  (field/reveal-cell field-with-flagged-cell [1 2])]
        (and
          (cell/cell-flagged? (get-in result [:mine-field/grid 1 2]))
          (get-in result [:mine-field/grid 1 2 :cell/hidden?]))) => true
    )))



