(ns com.mine-sweeper.model.cell)


(defn mined? [{:cell/keys [content]}] (= content :mine))

(defn number-cell? [{:cell/keys [content]}] (pos-int? content))

(defn empty-cell? [{:cell/keys [content]}] (= 0 content))
(def not-mined? (complement mined?))

(defn flagged? [{:cell/keys [flagged?]}] flagged?)
(defn set-adjacent-content [c n] (assoc c :cell/content n))

(defn set-mine [c] (assoc c :cell/content :mine))

(defn set-flag [c] (assoc c :cell/flagged true))

(defn exposed? [cell] (not (:cell/hidden? cell)))
(defn hidden? [cell] (:cell/hidden? cell))

(defn exposed-mine? [cell] (and (exposed? cell) (mined? cell)))

(defn cell-character ^Character [cell]
  (cond
    (and (exposed? cell) (mined? cell)) \*
    (flagged? cell) \?
    (not (exposed? cell)) \~
    (empty-cell? cell) \_
    (number-cell? cell) (char (+ 48 (:cell/content cell)))))