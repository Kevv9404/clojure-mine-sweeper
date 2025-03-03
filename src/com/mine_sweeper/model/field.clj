(ns com.mine-sweeper.model.field
  (:require [com.mine-sweeper.model.utils :as utils]))

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

(defn get-cell-at-coordinate [{:mine-field/keys [grid]} x y]
  (first (filter #(and (= (:cell/x %) x) (= (:cell/y %) y)) (mapcat identity grid))))

(defn adjacent-cells [mine-field x y]
  (let [coordinates [[x (dec y)] [x (inc y)] [(inc x) y] [(dec x) y]
                     [(dec x) (dec y)] [(inc x) (inc y)]
                     [(inc x) (dec y)] [(dec x) (inc y)]]]
    (remove nil? (mapv (fn [[x y]]
             (get-cell-at-coordinate mine-field x y)) coordinates))))

(defn mined? [{:cell/keys [content]}] (= content :mine))
(def not-mined? (complement mined?))

(defn mine-count [mine-field x y]
  (count (filter mined? (adjacent-cells mine-field x y))))

(defn expand [{:keys [grid] :as mine-field} x y]
  (let [cell (get-cell-at-coordinate mine-field x y)
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

(defn random-mind-locations [mine-field n]
  (loop [mines 0
         mine-field mine-field]
    (if (< mines n)
      (recur (inc mines)
             (assoc (get-cell-at-coordinate mine-field (rand-int (:mine-field/width mine-field)) (rand-int (:mine-field/height mine-field)))
               :cell/content :mine)))))

(comment
  (mine-field 2 2)
  (get-cell-at-coordinate (mine-field 3 3) 2 1)
  (adjacent-cells (mine-field 6 6) 0 0)
  (build-grid 2 -1)
  )
