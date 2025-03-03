(ns com.mine-sweeper.model.playground
  (:require [com.mine-sweeper.terminal :as terminal]
            [com.mine-sweeper.model.field :as field])
  (:import (jdk.internal.org.jline.terminal Terminal)))


(defn draw-cell [^Terminal t grid cell]
  )

(defn setup [width height n]
  (-> (field/mine-field width height)
      (field/populate-mines n)
      (field/initialize-mine-counts))
  )

(defn draw-grid [^Terminal t mine-field]
  (let [grid (:mine-field/grid mine-field)]
    (mapv (fn [row]
            (mapv (fn [cell]
                    (if (= (:cell/hidden? cell) true)
                      (terminal/put-character t (:cell/x cell) (:cell/y cell) \X)))
                  row))
          grid)
    (doseq [y (range (count grid))
            x (range (count (first grid)))]
      (draw-cell t grid (get-in grid [x y])))))



(comment
  (def t (terminal/terminal))
  (def mine-field (field/mine-field 10 10))
  (draw-grid t mine-field)
  )






