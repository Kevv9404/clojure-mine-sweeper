(ns com.mine-sweeper.model.field
  (:require [com.mine-sweeper.model.utils :as utils]
            [com.mine-sweeper.model.cell :as cell]))

(defn empty-cell [] {:cell/hidden?  true
                     :cell/flagged? false})

(defn build-grid
  "Returns a list of maps as cells with coordinates given a width and height"
  [width height]
  (let [grid (utils/vectors-of empty-cell height width)]
    (into [] (map-indexed
               (fn [y row]
                 (into []
                   (map-indexed
                     (fn [x cell]
                       (assoc cell :cell/x x :cell/y y))
                     row)))
               grid))))

(defn mine-field [w h] {:mine-field/width  w
                        :mine-field/height h
                        :mine-field/grid   (build-grid w h)})

(defn adjacent-cells [{:mine-field/keys [width height grid] :as mine-field} x y]
  (for [cx (range (max 0 (dec x)) (min width (+ 2 x)))
        cy (range (max 0 (dec y)) (min height (+ 2 y)))]
    (-> grid
      (get-in [cx cy])
      (assoc :x cx :y cy))))


;; Library???
(defn random-grid-locations
  "Return a SET of random locations (vectors of [x y]) within the given bounds."
  [width height n]
  (loop [locations #{}]
    (if (= n (count locations))
      locations
      (let [x (rand-int width)
            y (rand-int height)]
        (recur (conj locations [x y]))))))

(defn set-field-content [mine-field x y value] (assoc-in mine-field [:mine-field/grid x y :cell/content] value))

(defn get-field-content [mine-field x y] (get-in mine-field [:mine-field/grid x y :cell/content]))

;; Generative testing???
;;   * In tests you state general *properties* that should be true about a function
;;      * Resulting grid contains exactly N mines
;;      * The mines seem to be in random locations (might be challenging to write)
;;   * run some number of iterations, where you use randomly generated data, and assert that the PROPERTIES are true
;;   * When using "random" data, you often take control of the "seed"

(defn populate-mines
  "Returns an updated mine field with n mines in random locations. NOTE: The grid must be empty if you want to ensure n new mines."
  [{:mine-field/keys [width height] :as mf} n]
  (let [locations (random-grid-locations width height n)]
    (reduce (fn [m [x y]] (set-field-content m x y :mine)) mf locations)))

(defn mine-count [mine-field x y]
  (count (filter cell/mined? (adjacent-cells mine-field x y))))

(defn grid-locations
  "Returns a sequence of [x y] pairs for every location in the grid of the mine field."
  [width height]
  (for [x (range 0 width)
        y (range 0 height)]
    [x y]))

(defn initialize-mine-counts
  "Returns a new mine field, where the adjacent numbers for mines are set."
  [{:mine-field/keys [width height] :as mf}]
  (let [cell-locations (grid-locations width height)]
    (reduce
      (fn [field [x y]]
        (if (= :mine (get-field-content field x y))
          field
          (set-field-content field x y (mine-count field x y))))
      mf
      cell-locations)))

(defn expand [{:keys [grid] :as mine-field} x y]
  (let [cell                     (get-in mine-field [x y])
        adjacent-not-mined-cells (into #{} (filter cell/not-mined? (adjacent-cells mine-field x y)))]
    (cond
      (empty? (:cell/content cell))
      (mapv (fn [row] (mapv (fn [c]
                              (if (or (= c cell) (contains? c adjacent-not-mined-cells))
                                (assoc c :cell/hidden? false))) row)) grid)
      (cell/mined? cell)
      (mapv (fn [row] (mapv (fn [cell] (assoc cell :cell/hidden? false)) row)) grid))))


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
  (build-grid 3 2)
  (empty-cell)
  )
