(ns com.mine-sweeper.main
  (:require
    [com.fulcrologic.fulcro.application :as app]
    [com.fulcrologic.fulcro.components :as comp :refer [defsc]]
    [com.fulcrologic.fulcro.dom :as dom]
    [com.fulcrologic.fulcro.mutations :refer [defmutation]]
    [com.fulcrologic.fulcro.react.version18 :refer [with-react18]]
    [com.mine-sweeper.logic :as logic]
    [fulcro.inspect.tool :refer [add-fulcro-inspect!]]))

(defonce app (let [app (with-react18 (app/fulcro-app))]
               (add-fulcro-inspect! app)
               app))

(defmutation process-user-input [{:keys [key]}]
  ;; Section
  (action [{:keys [state]}]                                 ; OPTIMISTIC ACTION (before doing network stuff)
          (when key
            (swap! state logic/game-step key))))

(defmutation restart [{:keys [key]}]
  (action [{:keys [state]}]
          (reset! state (logic/setup 10 10 10))))

(defsc Cell [this {:cell/keys [x y content hidden? flagged?]} {:keys [cursor-position]}]
  {:query [:cell/x
           :cell/y
           :cell/content
           :cell/hidden?
           :cell/flagged?]}
  (let [is-cursor? (= [x y] cursor-position)]
    (dom/div {:id        (str "cell-" x "-" y)
              :key       (str "cell-" x "-" y)
              :className "w-10 h-10 flex items-center justify-center border border-gray-300 font-bold border-grey text-white"
              :style     {:backgroundColor (cond
                                             is-cursor? "#3cdfff"
                                             (not hidden?) "#636363"
                                             hidden? "#E8E8E8")}}
             (cond
               flagged? (str "🇨🇴")
               hidden? ""
               (= content :mine) (str "💣")
               :else (str content)))))

(def ui-cell (comp/computed-factory Cell))

(defsc Row [this {:row/keys [cells] :as props}]
  {:query [{:row/cells (comp/get-query Cell)}]}
  (mapv ui-cell cells))

; EQL:   [:prop]

(def ui-row (comp/computed-factory Row))

(defsc MineField [this {:mine-field/keys [width height grid] :as mine-field} {:keys [cursor-position]}]
  {:query [:mine-field/height :mine-field/width :mine-field/grid {:mine-field/grid (comp/get-query Row)}]}
  (doall
    (for [y (range height)]
      (dom/div {:key (str "row-" y)
                :id  (str "row-" y) :className "flex"}
               (for [x (range width)]
                 (ui-cell (get-in grid [x y]) {:cursor-position cursor-position
                                               :x               x
                                               :y               y}))))))

(def ui-mine-field (comp/computed-factory MineField))

(defsc Game [this {:keys            [cursor-position]
                   :mine-field/keys [width height grid] :as mine-field}]
  {:initial-state (fn [& _] (logic/setup 10 10 10))}
  (dom/div {:tabIndex  0
            :className "flex justify-center items-center h-screen "
            :onKeyDown (fn [evt]
                         (let [key (.-key evt)]
                           (comp/transact! this [(process-user-input {:key key})])))}
           (dom/div {:className "grid gap-1 p-4 bg-white shadow-lg rounded-lg"}
                    (ui-mine-field mine-field {:cursor-position cursor-position})
                    (dom/div {:id "Game over" :className "flex flex-col items-center justify-center w-full mt-4 text-red"}
                             (when (logic/game-over? mine-field)
                               (dom/div {:className "text-center"}
                                        (dom/h1 {:className "text-2xl font-bold"} "Game over!!")
                                        (dom/button {:className "mt-2 px-4 py-2 bg-blue text-white rounded"
                                                     :onClick   (fn [] (comp/transact! this [(restart)]))}
                                                    "Restart")))))))

(defn init []
  (println "Initializing app!!")
  (app/mount! app Game "app")
  )

(defn refresh []
  (app/mount! app Game "app")
  )

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
