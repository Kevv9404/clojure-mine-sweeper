(ns com.mine-sweeper.model.field
  (:require [com.mine-sweeper.model.cell :as cell]
            [com.mine-sweeper.model.grid :as utils]))

(defn mine-field [w h]
  {:mine-field/width  w
   :mine-field/height h
   :mine-field/grid   (utils/vectors-of cell/empty-cell w h)})

(defn set-field-content [mine-field x y value]
  (assoc-in mine-field [:mine-field/grid x y :cell/content] value))

(defn get-field-content [mine-field x y]
  (get-in mine-field [:mine-field/grid x y :cell/content]))

(defn populate-mines
  "Returns an updated mine field with n mines in random locations. NOTE: The grid must be empty if you want to ensure n new mines."
  [{:mine-field/keys [width height] :as mf} n]
  (let [locations (utils/random-grid-locations width height n)]
    (reduce (fn [m [x y]] (set-field-content m x y :mine)) mf locations)))

(defn expose-all-cells
  "Returns an updated mine field with all cells exposed."
  [{:mine-field/keys [width height] :as mine-field}]
  (let [cells (utils/grid-locations width height)]
    (reduce (fn [m [x y]] (assoc-in m [:mine-field/grid x y :cell/hidden?] false)) mine-field cells)))

(defn reveal-cell
  "Reveals a single cell at the given coordinates."
  [mine-field x y]
  (assoc-in mine-field [:mine-field/grid x y :cell/hidden?] false))

(defn get-cell
  "Gets the cell at the given coordinates."
  [mine-field x y]
  (get-in mine-field [:mine-field/grid x y]))

(defn expand
  "Reveals the cell at the given coordinates (x, y) in the mine field.

   If the cell is already revealed or flagged, returns the mine field unchanged.
   If the cell contains a mine or a number, reveals only that cell.
   If the cell is empty (content = 0), recursively reveals all connected empty cells
   and their adjacent numbered cells.

   Returns an updated (immutable) version of the mine field with appropriate cells revealed."
  [{:mine-field/keys [width height grid] :as mine-field} x y]
  {:pre [(< -1 x width) (< -1 y height)]}
  (let [{:cell/keys [hidden? flagged? content]} (get-cell mine-field x y)]
    (cond
      ;; If the cell is already revealed or flagged, do nothing
      (or (not hidden?) flagged?)
      mine-field

      ;; If the cell is empty (content = 0), reveal it and recursively expand adjacent cells
      (= content 0)
      (let [revealed-field  (reveal-cell mine-field x y)
            adjacent-coords (sequence (map (juxt :x :y)) (utils/adjacent-cells grid x y))]
        (reduce (fn [field [ax ay]]
                  (expand field ax ay))
          revealed-field
          adjacent-coords))

      ;; Otherwise (mine or number), just reveal this cell
      :else
      (reveal-cell mine-field x y))))

(defn draw! [{:keys [width height grid] :as mine-field}]
  (doseq [x (range width)
          y (range height)
          :let [cell    (get-field-content mine-field x y)
                to-draw (cell/text-representation cell)]]

    ))
