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

(specification "expand"
  (let [;; Helper function to create cells with specific content
        cell-with-content (fn [content]
                            (assoc (cell/empty-cell) :cell/content content))

        ;; Create a revealed cell
        revealed-cell     (fn [content]
                            (assoc (cell-with-content content) :cell/hidden? false))

        ;; Create a flagged cell
        flagged-cell      (fn [content]
                            (assoc (cell-with-content content) :cell/flagged? true))

        ;; Test grid with various cell types:
        ;; M = Mine, 1/2 = numbered cells, E = Empty cell (content 0)
        ;; R = Already revealed cell, F = Flagged cell
        M                 (cell-with-content :mine)
        E                 (cell-with-content 0)
        N1                (cell-with-content 1)
        N2                (cell-with-content 2)
        R                 (revealed-cell 1)
        F                 (flagged-cell 0)

        ;; Create a test grid with different scenarios
        grid              [[M N1 E E]
                           [N2 N1 E E]
                           [E E E F]
                           [R E E E]]

        mine-field        {:mine-field/width  4
                           :mine-field/height 4
                           :mine-field/grid   grid}
                           
        ;; Pre-compute results for different scenarios
        mine-result       (field/expand mine-field 0 0)
        mine-result-grid  (:mine-field/grid mine-result)
        
        number-result     (field/expand mine-field 0 1)
        number-result-grid (:mine-field/grid number-result)
        
        empty-result      (field/expand mine-field 2 0)
        empty-result-grid (:mine-field/grid empty-result)
        
        middle-result     (field/expand mine-field 2 2)
        middle-result-grid (:mine-field/grid middle-result)
        
        revealed-result   (field/expand mine-field 3 0)
        revealed-result-grid (:mine-field/grid revealed-result)
        
        flagged-result    (field/expand mine-field 2 3)
        flagged-result-grid (:mine-field/grid flagged-result)
        
        original-grid     (:mine-field/grid mine-field)]

    (assertions
      "Revealing a mine cell should only reveal that cell"
      (:cell/hidden? (get-in mine-result-grid [0 0])) => false
      (:cell/hidden? (get-in mine-result-grid [0 1])) => true

      "Revealing a numbered cell should only reveal that cell"
      (:cell/hidden? (get-in number-result-grid [0 1])) => false
      (:cell/hidden? (get-in number-result-grid [1 1])) => true

      "Revealing an empty cell should trigger recursive expansion"
      (:cell/hidden? (get-in empty-result-grid [2 0])) => false
      (:cell/hidden? (get-in empty-result-grid [2 1])) => false
      (:cell/hidden? (get-in empty-result-grid [1 0])) => false
      (:cell/hidden? (get-in empty-result-grid [0 0])) => true

      "Revealing a cell in the middle of empty region should reveal all connected empty cells"
      (:cell/hidden? (get-in middle-result-grid [2 2])) => false
      (:cell/hidden? (get-in middle-result-grid [3 2])) => false
      (:cell/hidden? (get-in middle-result-grid [3 3])) => false
      (:cell/hidden? (get-in middle-result-grid [2 3])) => true

      "Revealing an already revealed cell should not change anything"
      (= original-grid revealed-result-grid) => true

      "Revealing a flagged cell should not change anything"
      (= original-grid flagged-result-grid) => true)))
