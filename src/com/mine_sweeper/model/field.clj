(ns com.mine-sweeper.model.field)

(def cells-status
  {:mine   "mine"
   :number "number"
   :flag   "flag"
   :empty  "empty"})
;1, Should be a function
;2. could take width/height
;3. We need a function to create a new cell.
;4. Functional programming general.
;5. things need goo names. (field? is very general)
;6. Minefield =  vector of list of vectors.

(defn mine-field [width heigth])

(defn vec-of [number-of-elements element-factory]
  (mapv element-factory (range number-of-elements)))

(defn vectors-of [element-factory & dims]
  (mapv (vec-of (apply vectors-of (rest dims)) dims) dims))

(defn empty-cell [_] {:hidden? true})

(defn mine-field [w h] {:width  w
                        :height h
                        :cells  (vectors-of empty-cell w h)})

(comment
  (vec-of 5 empty-cell)
  (vectors-of empty-cell 5 5))