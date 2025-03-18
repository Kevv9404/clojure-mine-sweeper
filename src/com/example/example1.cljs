(ns com.example.example1
  (:require
    [com.fulcrologic.fulcro.application :as app]
    [com.fulcrologic.fulcro.components :as comp :refer [defsc]]
    [com.fulcrologic.fulcro.dom :as dom]
    [com.fulcrologic.fulcro.mutations :refer [defmutation]]
    [fulcro.inspect.tool :refer [add-fulcro-inspect!]]))

;; Tree of data represents how we want the application to start out (static statement)
;;    Normalizing into an "initial database"
;;    Mounting:
;;      LOOP:
;;        * Query the database using the UI query -> Tree
;;        * Render that entire Tree from Root (React makes this fast)
;;        * TRANSACT -> change db -> recur (SYNCHRONOUS)
;;
;; Properties of this application:
;;    Change happens at TRANSACT
;;    Goes from one immutable state to new version of that state
;;    Tracked in an atom
;;
;;    Can REASON IN TIME
;;    * Normalization : makes large (or any) application tractable to use this way
;;    * Composition : Components let us "take apart" the parts of the application to get LOCAL reasoning
;;       * Co-located queries/idents lets us also compose the normalization
;;
;; NEXT: Consider side-effects that happen outside of pure data
;;   * CANNOT reason about these IN TIME (unless you figure out a way to add that back)
;;   * Async query over a network
;;   * Read from disk
;;   * Request to process something on web worker
;;
(defonce app (app/fulcro-app))

(defn counter-button-click [b] (update b :button/clicks inc))

(defmutation increment-button [{:button/keys [id]}]
  (action [{:keys [state]}]
    (swap! state update-in [:button/id id] counter-button-click)))

(defsc CounterButton [this {:button/keys [clicks color] :as props}]
  {:query         [:button/id :button/clicks :button/color]
   :ident         :button/id
   :initial-state {:button/id     (or :param/id (random-uuid))
                   :button/color  "blue"
                   :button/clicks (or :param/start-at 0)}}
  (dom/li nil
    (dom/button {:style   {:backgroundColor color
                           :color           "white"}
                 :onClick (fn []
                            (comp/transact! this [(increment-button props)]))}
      (str "Clicks " clicks))))

(comment
  (js/console.log CounterButton)
  (comp/get-initial-state CounterButton {:id 5 :start-at 99}))

(def ui-counter-button (comp/factory CounterButton))

(defsc Root [this {:ui/keys [other-button buttons]}]
  {:query         [{:ui/other-button (comp/get-query CounterButton)}
                   {:ui/buttons (comp/get-query CounterButton)}]
   :initial-state {:ui/other-button {:id 1 :start-at 3}
                   :ui/buttons     [{:id 1 :start-at 8}
                                    {:id 2 :start-at 2}
                                    {:id 3 :start-at 9}]}}
  (dom/div nil
    (dom/h2 "My Buttons")
    (ui-counter-button other-button)
    (dom/ul nil
      (mapv ui-counter-button buttons))))
(comment
  (comp/get-initial-state Root {}))

(defn refresh []
  (app/mount! app Root "app"))

(defn init []
  (refresh)
  (add-fulcro-inspect! app))
