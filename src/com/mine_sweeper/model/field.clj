(ns com.mine-sweeper.model.field
  (:require [com.mine-sweeper.model.grid :as utils]))

(defn empty-cell [] {:cell/hidden?  true
                     :cell/flagged? false})

(defn mine-field [w h] {:mine-field/grid (utils/vectors-of empty-cell w h)})
(defn mined? [{:cell/keys [content]}] (= content :mine))
(def not-mined? (complement mined?))

(defn set-field-content [mine-field x y value] (assoc-in mine-field [:mine-field/grid x y :cell/content] value))
(defn get-field-content [mine-field x y] (get-in mine-field [:mine-field/grid x y :cell/content]))

(defn populate-mines
  "Returns an updated mine field with n mines in random locations. NOTE: The grid must be empty if you want to ensure n new mines."
  [{:mine-field/keys [width height] :as mf} n]
  (let [locations (utils/random-grid-locations width height n)]
    (reduce (fn [m [x y]] (set-field-content m x y :mine)) mf locations)))

(defn expand [{:keys [grid] :as mine-field} x y]
  (let [cell                     (get-in mine-field [x y])
        adjacent-not-mined-cells (into #{} (filter not-mined? (adjacent-cells mine-field x y)))]
    (cond
      (empty? (:cell/content cell))
      (mapv (fn [row] (mapv (fn [c]
                              (if (or (= c cell) (contains? c adjacent-not-mined-cells))
                                (assoc c :cell/hidden? false))) row)) grid)
      (mined? cell)
      (mapv (fn [row] (mapv (fn [cell] (assoc cell :cell/hidden? false)) row)) grid))))

(defn set-adjacent-content [c n] (assoc c :cell/content n))

(defn set-mine [c] (assoc c :cell/content :mine))

(defn set-flag [c] (assoc c :cell/flagged true))


(comment
  ;; 2 random number generators:
  ;;   * a bit of hardware is used to gather "entropy" and generate a number as a result. These are more "secure"
  ;;      * Used for cryptography...
  ;;   * Appearance of "randomness". Math sequence, based on an INITIAL seed, which generates new numbers BASED on the seed, which
  ;;     basically changes to the prior random number generated.
  ;;      * The probability of seeing a number is 1/n where n is the range
  ;;      * The probability of seeing a number twice in a row is 1/n + 1/n where n is the range
  (count (random-grid-locations 20 10 10))
  (mine-field 2 2)
  (let [r (java.util.Random. 1453361670)]
    (repeatedly 10 #(.nextInt r)))
  (get-cell-at-coordinate (mine-field 3 3) 2 1)
  (adjacent-cells (mine-field 6 6) 0 0)
  (build-grid 2 -1)
  )
