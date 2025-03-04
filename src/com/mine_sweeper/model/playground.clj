(ns com.mine-sweeper.model.playground
  (:require [com.mine-sweeper.model.field :as field]
            [com.mine-sweeper.terminal :as terminal])
  (:import (jdk.internal.org.jline.terminal Terminal)))


(defn setup [width height n]
  (-> (field/mine-field width height)
    (field/populate-mines n)
    (field/initialize-mine-counts)))

(def app-state (atom {:cursor-position [0 0]
                      :mine-field      (setup 10 10 3)}))


(defn move-cursor* [initial-state direction]
  (let [{:mine-field/keys [width height]} (:mine-field initial-state)]
    (update initial-state :cursor-position
      (fn [[x y]]
        (let [[new-x new-y] (case direction
                              :up [x (dec y)]
                              :down [x (inc y)]
                              :left [(dec x) y]
                              :right [(inc x) y]
                              [x y])]
          [(max 0 (min (dec width) new-x))
           (max 0 (min (dec height) new-y))])))))

(defn draw-grid [^Terminal t mine-field]
  (let [grid (-> mine-field :mine-field :mine-field/grid)]
    (mapv (fn [row]
            (mapv (fn [{:cell/keys [hidden? content flagged? x y]}]
                    (if (= hidden? true)
                      (terminal/put-character t x y \~)
                      (cond
                        (= content :mine) (terminal/put-character t x y \*)
                        (number? content) (terminal/put-character t x y (char (+ 48 content)))
                        (= flagged? true) (terminal/put-character t x y \?))))
              row))
      grid)))

#_(draw! @app-state)
#_(while (not (game-over @app-state))
    (let [cmd (get-command t)]                              ; returns a (fn [app-state] (move-cursor* app-state :up))
      (if cmd
        (do
          (swap! app-state cmd)
          (draw! @app-state))
        (Thread/sleep 100))))


(def t (terminal/terminal))

(defn user-movement []
  (let [cursor-position (:cursor-position @app-state)]
    (loop []
      (let [key (terminal/get-next-key-press t)]
        (when key
          (swap! app-state update :cursor-position
            (fn [[x y]]
              (condp = key
                \h [(dec x) y]
                \j [x (inc y)]
                \k [x (dec y)]
                \l [(inc x) y]
                :else [x y]))))
        (let [[new-x new-y] cursor-position]
          (terminal/move-cursor t new-x new-y)))
      (recur))))

(comment
  (draw-grid t @app-state)
  (user-movement))






