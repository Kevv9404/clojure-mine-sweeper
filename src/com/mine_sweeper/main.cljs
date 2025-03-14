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
      (dom/div {:id "Game over" :className "flex flex-col items-center justify-center w-full mt-4 text-red"}
        (when (logic/game-over? mine-field)
          (dom/div {:className "text-center"}
            (dom/h1 {:className "text-2xl font-bold"} "Game over!!")
            (dom/button {:className "mt-2 px-4 py-2 bg-blue text-white rounded"
                         :onClick   #(swap! game-state logic/game-step \y)}
              "Restart")))))))

(defn init []
  (println "Initializing app!!")
  (let [the-real-div (.getElementById js/document "app")]
    (reset! root (createRoot the-real-div))))

(defn refresh []
  (let [ui (render-mine-field @game-state)]
    (.render @root ui)))

;; Problems with global state:
;; 1. Gets large, and without organization, HARD to comprehend/navigate
;; 2. As you evolve the application OVER TIME, with random devs
;;    * people stick things in random locations
;;    * information gets duplicated
;;    * information LOADING/SAVING is complected with the operation of rendering
;;        . componentDidMount used to LOAD data!!! Ugh. AVOID THIS!

;; Database Normalization is how we deal with large complex data...have been doing this for decades
;;   Definition:
;;     * Deduplicate information (don't save the same things in multiple places)
;;        * Efficient update
;;        * Save storage (sharing data)
;;     * Scheme for doing that.  SQL Research.
;; Take-away information:
;;   1. Divide the data based on conceptual "entities"
;;   2. If there is nested data within such an entity that might need to be shared by more than one item, then divide that out
;;   3. Use *references* to point things to each other

; INVOICE: invoice-id, date, amount, [item1-id, item2-id, item3-id]
; ITEM:    item1-id product-id number total
; PRODUCT: product-id "SHIRT" 11.0

; HOW WELL DOES THIS (practice/concept/technique) WORK AS SOFTWARE GROWS????
;   * Code composition? Can I build things as "localized units" where composing those new units does not BREAK existing code?
;       * Able to Refactor?
;       * Can I Reuse that thing?
;       * Does adding some new thing, break some old thing?
;   * Local Reasoning?  Can I (mostly) thing about the things I'm working on, without worrying about breaking other things?
;       * Can I think about some "thing" all by itself?
;       * Abstraction
;           * Can other people thing about my thing without having to read the implementation?
;   * Clarity?
;      * Are the concepts simple?
;      * Easy to reason about?
;      * Small number of things to "know" that can easily lead to understanding?
;      * AVOID INCIDENTAL COMPLEXITY:
;         . Usually an accident where not enough thought was used to find the core concepts/operations/etc. needed.
;         . Not ALWAYS bad. Sometimes you're doing it because of some other concern (Type systems)
;         . Bad when it COSTS something, but brings no benefit.


; Swim different sizes of pools: Olympic size, smaller pools at rec centers.
;    ALl of the swim meets (competitions): Keep track of how fast people are.
;       20yd pool swimming 40yd breast stroke... (go out, turn around, come back)
;          Tommy 23.45s
;          Susie 22.45s
;       20m pool swimming 40m breast stroke...
;          Tommy 26.45s
;          Susie 24.45s
;       50m pool swimming 50m breast stroke <---- time you really care about (swim OUT only)
; ; Qualify for Olympics, OR compare times between teams (rankings)
; * REAL complexity: Ton of rules for CONVERTING times to compare swimmers (HAD A LOT of incidental complexity because of how people think)
;
; Loop: swimmers
;    * Find all of their times (READ)
;    * Convert some of those times -> target pool size (MAP)
;    * Remove times that are not allowed to be used (FILTER)
;    * compare (SORT)
