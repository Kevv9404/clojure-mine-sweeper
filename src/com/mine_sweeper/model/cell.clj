(ns com.mine-sweeper.model.cell)


(defn mined? [{:cell/keys [content]}] (= content :mine))

(def not-mined? (complement mined?))

(defn set-adjacent-content [c n] (assoc c :cell/content n))

(defn set-mine [c] (assoc c :cell/content :mine))

(defn set-flag [c] (assoc c :cell/flagged true))

(defn exposed? [cell] (not (:cell/hidden? cell)))

(defn exposed-mine? [cell] (and (exposed? cell) (mined? cell)))

