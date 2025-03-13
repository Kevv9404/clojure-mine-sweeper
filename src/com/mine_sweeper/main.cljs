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
                         (let [key  (.-key evt)
                               move (logic/user-input->command key)]
                           (when move
                             (swap! game-state logic/game-step key))))}
    (dom/div {:className "grid gap-1 p-4 bg-white shadow-lg rounded-lg"}
      (map-indexed
        (fn [y row]
          (dom/div {:id (str "row-" y) :className "flex"}
            (map-indexed
              (fn [x cell]
                (let [content    (:cell/content cell)
                      hidden?    (:cell/hidden? cell)
                      is-cursor? (= [x y] cursor-position)]
                  (dom/div {:id (str "cell-" x "-" y)
                            :onClick   (fn []
                                         (println "Clicked on " x y cell))
                            :className (str "w-10 h-10 flex items-center justify-center border border-gray-300"
                                         (if is-cursor?
                                           " bg-blue"
                                           " bg-gray"
                                           ))}
                    (cond
                      hidden? ""
                      (= content :mine) (str "💣")
                      :else (str content))
                    )))
              row)))
        grid))))


(defn init []
  (println "Initializing app!!!")
  (let [the-real-div (.getElementById js/document "app")]
    (reset! root (createRoot the-real-div))))

(defn refresh []
  (let [ui (render-mine-field @game-state)]
    (.render @root ui)))
