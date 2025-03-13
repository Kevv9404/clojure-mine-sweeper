(ns com.mine-sweeper.logic
  (:require [com.mine-sweeper.model.cell :as cell]
            [com.mine-sweeper.model.field :as field]))

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


(defn reveal-cell
  "Reveals the cell at the given coordinates (x, y) in the mine field.
   If the cell is already revealed or flagged, returns the mine field unchanged.
   If the cell contains a mine or a number, reveals only that cell.
   If the cell is empty (content = 0), recursively reveals all connected empty cells
   and their adjacent numbered cells.
   Returns an updated (immutable) version of the mine field with appropriate cells revealed."
  [mine-field x y]
  (assoc-in mine-field [:mine-field/grid x y :cell/hidden?] false))

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

(defn user-input->command
  "Takes a keyboard input character and turns it into the desired game command"
  [k]
  (case k
    \h :left
    \j :down
    \k :up
    \l :right
    \e :expand
    \  :expand
    \f :flag
    \y :restart
    \x :exit
    nil))

(defn flagged [{:keys [cursor-position] :as state}]
  (let [[cx cy] cursor-position]
    (update-in state [:mine-field/grid cx cy :cell/flagged?] not)))

(defn get-command [state key]
  (let [[x y] (:cursor-position state)]
    (when key
      (fn [state]
        (let [cmd (user-input->command key)]
          (case cmd
            (:up :down :left :right) (move-cursor* state cmd)
            :exit (System/exit 0)
            :expand (expand state x y)
            :flag (flagged state)
            :restart state
            state))))))

(defn game-over? [state]
  (or (some cell/exposed-mine? (field/all-cells state))
    (= (:mine-count state) (count (filter #(and (cell/mined? %) (cell/flagged? %)) (field/all-cells state))))))

(defn game-step
  "Update the game for a single step of user input"
  [current-state user-input]
  (if-let [cmd (get-command current-state user-input)]
    (cmd current-state)
    current-state))
