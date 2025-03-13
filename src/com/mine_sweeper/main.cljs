(ns com.mine-sweeper.main
  (:require
    [com.fulcrologic.fulcro.dom :as dom]
    ["react-dom/client" :refer [createRoot]]
    [com.mine-sweeper.logic :as logic]))

(defonce root (atom nil))
(defonce game-state (atom (logic/setup 10 10 10)))

(defn render-mine-field [{:mine-field/keys [width height grid] :as mine-field}]
  (dom/div
    (mapv
      (fn [row]
        (dom/div {:style {:display :inline}}
          (mapv
            (fn [cell]
              (dom/div {:onClick (fn []
                                   (println "Clicked on " cell))
                        :style {:display "inline"
                                :width "40px"}} (str (:cell/content cell))))
            row)))
      grid)))

#_(defn draw! [^Terminal t {:mine-field/keys [width height grid] :as mine-field}]
    (terminal/clear! t)
    (when (logic/game-over? mine-field)
      (display-game-over-message t width height))
    (let [[cx cy] (:cursor-position mine-field)]
      (doseq [y (range height)]
        (doseq [x (range width)]
          (terminal/put-character t x y (first (cell/cell-character (get-in grid [x y]))))))
      (terminal/move-cursor t cx cy)
      (terminal/flush! t)))
(defn init []
  (println "Initializing app")
  (let [the-real-div (.getElementById js/document "app")]
    (reset! root (createRoot the-real-div))))

(defn refresh []
  (let [ui (render-mine-field @game-state)]
    (.render @root ui)))
