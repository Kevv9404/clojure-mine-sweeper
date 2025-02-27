(ns com.mine-sweeper.model.field)

(def cells-status
  {:mine   "mine"
   :number "number"
   :flag   "flag"
   :empty  "empty"})

(def field
  (vec (for [x (range 10)]
         (vec (for [y (range 10)]
                {:x       x
                 :y       y
                 :hidden? true})))))
