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
            (mapv (fn [{:cell/keys [hidden? content flagged? x y]}]
                    (if (= hidden? true)
                      (terminal/put-character t x y \~)
                      (cond
                        (= content :mine) (terminal/put-character t x y \*)
                        (number? content) (terminal/put-character t x y (char (+ 48 content)))
                        (= flagged? true) (terminal/put-character t x y \?))))
              row)) grid)))


(def t (terminal/terminal))

(defn movement []
  (let [cursor-position (atom [0 0])]
    (loop []
      (let [key (terminal/get-next-key-press t)]
        (when key
          (swap! cursor-position
            (fn [[x y]]
              (condp = key
                \h [(dec x) y]
                \j [x (inc y)]
                \k [x (dec y)]
                \l [(inc x) y]
                :else [x y]))))
        (let [[new-x new-y] @cursor-position]
          (terminal/move-cursor t new-x new-y)
          ))
      (recur))))

(comment
  (draw-grid t (setup 10 10 3))
  (movement)
  )






