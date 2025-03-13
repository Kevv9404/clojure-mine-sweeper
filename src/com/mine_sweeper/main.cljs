(ns com.mine-sweeper.main
  (:require
    [com.fulcrologic.fulcro.dom :as dom]
    ["react-dom/client" :refer [createRoot]]
    [com.mine-sweeper.logic :as logic]))

(defonce root (atom nil))

(declare render-mine-field game-state)

(defn render! []
  (let [ui (render-mine-field @game-state)]
    (.render @root ui)))

(defonce game-state (let [a (atom (logic/setup 10 10 10))]
                      (add-watch a :render render!)
                      a))

(defn render-mine-field [{:keys [cursor-position]
                          :mine-field/keys [width height grid] :as mine-field}]
  (dom/div {:tabIndex  0
            :onKeyDown (fn [evt]
                         (swap! game-state identity)
                         (println (.-key evt)))}
    (dom/div
      (mapv
        (fn [row]
          (dom/div {:style {:display :inline}}
            (mapv
              (fn [cell]
                (dom/div {:onClick (fn []
                                     (println "Clicked on " cell))
                          :style   {:display "inline"
                                    :width   "40px"}} (str (:cell/content cell))))
              row)))
        grid))))

(defn init []
  (println "Initializing app")
  (let [the-real-div (.getElementById js/document "app")]
    (reset! root (createRoot the-real-div))))

(defn refresh []
  (let [ui (render-mine-field @game-state)]
    (.render @root ui)))
