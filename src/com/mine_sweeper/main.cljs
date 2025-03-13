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

(defn render-mine-field [{:keys            [cursor-position]
                          :mine-field/keys [width height grid] :as mine-field}]
  (dom/div {:tabIndex  0
            :className "flex justify-center items-center h-screen "
            :onKeyDown (fn [evt]
                         (let [key (.-key evt)]
                           (when key
                             (swap! game-state logic/game-step key))))}
    (dom/div {:className "grid gap-1 p-4 bg-white shadow-lg rounded-lg"}
      (for [y (range height)]
        (dom/div {:id (str "row-" y) :className "flex"}
          (for [x (range width)]
            (let [{:cell/keys [content hidden? flagged?]} (get-in grid [x y])
                  is-cursor? (= [x y] cursor-position)]
              (dom/div {:id        (str "cell-" x "-" y)
                        :className "w-10 h-10 flex items-center justify-center border border-gray-300 font-bold border-grey text-white"
                        :style     {:backgroundColor (cond
                                                        is-cursor? "#3cdfff"
                                                        (not hidden?) "#636363"
                                                        hidden? "#E8E8E8")}}
                (cond
                  flagged? (str "🇨🇴")
                  hidden? ""
                  (= content :mine) (str "💣")
                  :else (str content)))))))
      (dom/div {:id "Game over" :className "flex"}
        (when (logic/game-over? mine-field)
          (dom/div
            (dom/h1 "Game over!!")
            (dom/button {:onClick #(swap! game-state logic/game-step \y)} "Restart")))))))


(defn init []
  (println "Initializing app!!!")
  (let [the-real-div (.getElementById js/document "app")]
    (reset! root (createRoot the-real-div))))

(defn refresh []
  (let [ui (render-mine-field @game-state)]
    (.render @root ui)))
