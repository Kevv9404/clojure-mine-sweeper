(ns field
  (:require [clojure.test :refer :all]
            [com.mine-sweeper.model.field :as field]))


(deftest cells-are-hidden
  (let [field field/field]
    (doseq [row  field
            cell row]
      (is (:hidden? cell)))))

(deftest there-are-10-mines
  (let [field            field/field
        random-cell      (rand-int 10)
        ;TODO: fix how we add mines to the field
        field-with-mines (repeat 10 (assoc-in field [random-cell random-cell :value] (:mine field/cells-status)))]
    [(println "mine --> " field-with-mines)]))
