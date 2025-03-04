(ns com.mine-sweeper.model.cell)

(defn empty-cell []
  {:cell/hidden?  true
   :cell/content  0
   :cell/flagged? false})

(defn mined? [{:cell/keys [content]}] (= content :mine))
(def not-mined? (complement mined?))
(defn set-adjacent-content [c n] (assoc c :cell/content n))
(defn set-mine [c] (assoc c :cell/content :mine))
(defn set-flag [c] (assoc c :cell/flagged true))
(defn content [c] (:cell/content c))
