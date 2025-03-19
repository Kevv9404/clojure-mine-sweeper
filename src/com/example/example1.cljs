(ns com.example.example1
  (:require
    [com.fulcrologic.fulcro.algorithms.tx-processing :as txn]
    [com.fulcrologic.fulcro.application :as app]
    [com.fulcrologic.fulcro.components :as comp :refer [defsc]]
    [com.fulcrologic.fulcro.dom :as dom]
    [com.fulcrologic.fulcro.mutations :refer [defmutation]]
    [edn-query-language.core :as eql]
    [fulcro.inspect.tool :refer [add-fulcro-inspect!]]))

(defn handle-remote-interaction [this request]
  )

(defonce app (app/fulcro-app {:remotes
                              {:remote
                               {:transmit!
                                (fn [{:keys [active-requests] :as this} {::txn/keys [ast result-handler update-handler] :as request}]
                                  (handle-remote-interaction this request))}}}))

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
                   :ui/buttons      [{:id 1 :start-at 8}
                                     {:id 2 :start-at 2}
                                     {:id 3 :start-at 9}]}}
  (dom/div nil
    (dom/h2 "My Buttons")
    (ui-counter-button other-button)
    (dom/ul nil
      (mapv ui-counter-button buttons))))

(defn refresh []
  (app/mount! app Root "app"))

(defn init []
  (refresh)
  (add-fulcro-inspect! app))

;; 1. Does I/O ONE at a time by default (override)
;;    * IN THE order of submission
;;    * UNLESS there are writes. Writes are pulled to the FRONT (on queue processing).
;;       * A single interaction with the UI results in 1+ reads, 1+ writes-> writes pulled to the front
;;           * Compose together a bunch of functions, and in VARIOUS functions there are loads/mutations.
;;       * Multiple interactions with the UI WILL NOT reorder prior submissions
