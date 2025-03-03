(ns com.mine-sweeper.model.grid)

(defn vec-of [sz element-factory]
  (mapv (fn [_] (element-factory)) (range sz)))

(defn vectors-of
  "creates a multi-dimensional vector in most-significant order."
  [element-factory & dims]
  (let [dim       (first dims)
        remainder (next dims)]
    (if remainder
      (vec-of dim #(apply vectors-of element-factory remainder))
      (vec-of dim element-factory))))

(defn width [cells] (count cells))
(defn height [cells] (count (first cells)))

(defn adjacent-cells
  "Returns a sequence of cells from `grid` (a 2-dim grid in least significant order) around the x,y coordinate, where
   x is the least significant (most deeply nested) dimension. If the element is a map, then this function will assoc
   :x and :y of the cell onto that map."
  [grid x y]
  (let [width  (width grid)
        height (height grid)]
    (for [cx (range (max 0 (dec x)) (min width (+ 2 x)))
          cy (range (max 0 (dec y)) (min height (+ 2 y)))
          :let [cell (get-in grid [cx cy])]]
      (cond-> cell
        (map? cell) (assoc :x cx :y cy)))))

(defn random-grid-locations
  "Return a SET of random locations [x y] within the given bounds 0..width, 0..height."
  [width height n]
  (loop [locations #{}]
    (if (= n (count locations))
      locations
      (let [x (rand-int width)
            y (rand-int height)]
        (recur (conj locations [x y]))))))

(defn adjacent-count
  "Given a 2-dim grid, and an x,y coordinate, returns the count of adjacent cells where (predicate cell) is true."
  [grid x y predicate]
  (count (filter predicate (adjacent-cells grid x y))))

(defn grid-locations
  "Returns a sequence of [x y] pairs for every location in the grid of the mine field."
  [width height]
  (for [x (range 0 width)
        y (range 0 height)]
    [x y]))

(defn populate-adjacent-counts
  "Returns a new grid of cells, where the number of adjacent cells that match is-counted? are set via (set-count cells x y cnt)."
  [cells is-counted? set-count]
  (let [cell-locations (grid-locations (width cells) (height cells))]
    (reduce
      (fn [cs [x y :as path]]
        (if (is-counted? (get-in cs path))
          cs
          (set-count cs x y (adjacent-count cs x y is-counted?))))
      cells
      cell-locations)))

