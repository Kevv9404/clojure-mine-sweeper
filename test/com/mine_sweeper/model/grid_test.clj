(ns com.mine-sweeper.model.grid-test
  (:require
    [com.mine-sweeper.model.grid :as grid]
    [fulcro-spec.core :refer [=> assertions specification]]))

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
        actual => expected))))
