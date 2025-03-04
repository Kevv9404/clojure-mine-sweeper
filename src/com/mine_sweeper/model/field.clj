(ns com.mine-sweeper.model.field
  (:require [com.mine-sweeper.model.cell :as cell]
            [com.mine-sweeper.model.grid :as utils]))

(defn mine-field [w h]
  {:mine-field/grid (utils/vectors-of cell/empty-cell w h)})

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

(defn expand [{:keys [grid] :as mine-field} x y]
  (let [cell (get-in mine-field [x y])]
    (cond
      (= 0 (:cell/content cell))
      (let [adjacent-cells (utils/adjacent-cells grid x y)]
        (reduce (fn [m [x y]] (expand m x y)) mine-field adjacent-cells))

      (cell/mined? cell) (expose-all-cells mine-field)

      :else mine-field)))
