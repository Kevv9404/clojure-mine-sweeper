(ns com.mine-sweeper.main
  (:require [com.mine-sweeper.model.cell :as cell]
            [com.mine-sweeper.model.field :as field]
            [com.mine-sweeper.terminal :as terminal])
  (:import (jdk.internal.org.jline.terminal Terminal)))


(defn setup [width height n]
  (merge {:cursor-position [0 0]
          :mine-count      n}
    (-> (field/mine-field width height)
      (field/populate-mines n)
      (field/initialize-mine-counts))))

(defn move-cursor* [initial-state direction]
  (let [{:mine-field/keys [width height]} initial-state]
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


(defn reveal-cell [mine-field x y]
  (assoc-in mine-field [:mine-field/grid x y :cell/hidden?] false))



"Reveals the cell at the given coordinates (x, y) in the mine field.
 If the cell is already revealed or flagged, returns the mine field unchanged.
 If the cell contains a mine or a number, reveals only that cell.
 If the cell is empty (content = 0), recursively reveals all connected empty cells
 and their adjacent numbered cells.
 Returns an updated (immutable) version of the mine field with appropriate cells revealed."

(defn expand
  [{:mine-field/keys [width height grid] :as mine-field} x y]
  {:pre [(< -1 x width) (< -1 y height)]}
  (let [{:cell/keys [hidden? flagged? content]} (get-in grid [x y])]
    (cond
      ;; If the cell is already revealed or flagged, do nothing
      (or (not hidden?) flagged?)
      mine-field

      ;; If the cell is empty (content = 0), reveal it and recursively expand adjacent cells
      (= content 0)
      (let [revealed-field  (reveal-cell mine-field x y)
            adjacent-coords (sequence (map (juxt :x :y)) (field/adjacent-cells mine-field x y))]
        (reduce (fn [field [ax ay]]
                  (expand field ax ay))
          revealed-field
          adjacent-coords))

      ;; Otherwise (mine or number), just reveal this cell
      :else (reveal-cell mine-field x y))))

(defn movement-key->direction [k]
  (case k
    \h :left
    \j :down
    \k :up
    \l :right
    "Unknown command"))

(defn flagged [{:keys [cursor-position] :as state}]
  (let [[cx cy] cursor-position]
    (update-in state [:mine-field/grid cx cy :cell/flagged?] not)))

(defn get-command [^Terminal t state]
  (let [key (terminal/get-next-key-press t)
        [x y] (:cursor-position state)]
    (when key
      (fn [state]
        (cond
          (= key \e) (expand state x y)
          (= key \f) (flagged state)
          :else (move-cursor* state (movement-key->direction key)))))))

(defn game-over? [state]
  (or (some cell/exposed-mine? (field/all-cells state))
    (= (:mine-count state) (count (filter #(and (cell/mined? %) (cell/flagged? %)) (field/all-cells state))))))

(defn draw! [^Terminal t {:mine-field/keys [width height grid] :as initial-state}]
  (let [[cx cy] (:cursor-position initial-state)]
    (doseq [y (range height)]
      (doseq [x (range width)]
        (terminal/put-character t x y (cell/cell-character (get-in grid [x y])))))
    (terminal/move-cursor t cx cy)
    (terminal/flush! t)))

;(doseq [y (range 10)]
;  (doseq [x (range 10)]
;    (prn x y)))

(declare play!)

(defn display-game-over-message [^Terminal t w h]
  (terminal/put-string t w h "Game Over! Press y to start again or x to exit"))

(defn game-loop [^Terminal t app-state]
  (while (not (game-over? @app-state))
    (when-let [cmd (get-command t @app-state)]
      (swap! app-state cmd)
      (draw! t @app-state))))

(defn handle-game-over-input [^Terminal t k]
  (cond
    (= k \y) :restart
    (= k \x) :exit
    :else nil))

(defn handle-post-game-input [^Terminal t w h]
  (display-game-over-message t w h)
  (loop []
    (when-let [k (terminal/get-next-key-press t)]
      (if-let [result (handle-game-over-input t k)]
        result
        (recur)))))

(defn initialize-game [w h m]
  (let [app-state (atom (setup w h m))
        t         (terminal/terminal)]
    (terminal/init-terminal t)
    [t app-state]))

(defn play! [w h m]
  (loop []
    (let [[t app-state] (initialize-game w h m)]
      (draw! t @app-state)
      (game-loop t app-state)
      (let [decision (handle-post-game-input t w h)]
        (terminal/exit-terminal t)
        (when (= decision :restart)
          (recur))))))


;(defn play! [w h m]
;  (let [app-state (atom (setup w h m))
;        t         (terminal/terminal)]
;    (terminal/init-terminal t)
;    (draw! t @app-state)
;    (while (not (game-over? @app-state))
;      (when-let [cmd (get-command t @app-state)]
;        (swap! app-state cmd)
;        (draw! t @app-state)))
;    (game-over-dialog t w h m)))

(comment
  (play! 20 10 20))
