(ns com.mine-sweeper.model.utils)

(defn vec-of [sz element-factory]
  (mapv (fn [_] (element-factory)) (range sz)))

(defn vectors-of [element-factory & dims]
  (let [dim (last dims)
        remainder (butlast dims)]
    (if remainder
      (vec-of dim #(apply vectors-of element-factory remainder))
      (vec-of dim element-factory))))
