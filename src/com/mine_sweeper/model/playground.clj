(ns com.mine-sweeper.model.playground
  (:require [clojure.string :as str]
            [com.mine-sweeper.terminal :as terminal]
            [com.mine-sweeper.model.field :as field])
  (:import (jdk.internal.org.jline.terminal Terminal)))


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
                      (terminal/put-character t (:cell/x cell) (:cell/y cell) \~)
                      (cond
                        (= (:cell/content cell) :mine) (terminal/put-character t (:cell/x cell) (:cell/y cell) \*)
                        (number? (:cell/content cell)) (terminal/put-character t (:cell/x cell) (:cell/y cell) (char (+ 48 (:cell/content cell))))
                        (= (:cell/flagged? cell) true) (terminal/put-character t (:cell/x cell) (:cell/y cell) \?))))
              row)) grid)))


(def t (terminal/terminal))
(draw-grid t (setup 10 10 3))

(comment

  )






