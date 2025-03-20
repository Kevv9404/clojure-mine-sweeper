(ns com.example.example1
  (:require
    [clojure.core.async :as async]
    [com.example.button :as button]
    [com.fulcrologic.fulcro.algorithms.data-targeting :as target]
    [com.fulcrologic.fulcro.algorithms.merge :as merge]
    [com.fulcrologic.fulcro.algorithms.tempid :as tempid]
    [com.fulcrologic.fulcro.algorithms.tx-processing :as txn]
    [com.fulcrologic.fulcro.application :as app]
    [com.fulcrologic.fulcro.components :as comp :refer [defsc]]
    [com.fulcrologic.fulcro.data-fetch :as df]
    [com.example.parser :as parser]
    [com.fulcrologic.fulcro.dom :as dom]
    [com.fulcrologic.fulcro.dom.events :as evt]
    [com.fulcrologic.fulcro.mutations :as m :refer [defmutation]]
    [com.fulcrologic.fulcro.react.version18 :as v18]
    [edn-query-language.core :as eql]
    [fulcro.inspect.tool :refer [add-fulcro-inspect!]]))


(defn handle-remote-interaction [this {::txn/keys [ast result-handler] :as request}]
  (async/go
    (async/<! (async/timeout 200))
    (let [txn (eql/ast->query ast)
          response (parser/parser {} txn)
          {:keys [key]} (-> ast :children first)
          response-status-fail (keyword? (get response key))]
      (result-handler
        (if response-status-fail
          {:transaction txn
           :status-code 400
           :body        response}
          {:transaction txn
           :status-code 200
           :body        response})))))

(defonce app (v18/with-react18
               (app/fulcro-app
                 {:remotes
                  {:remote
                   {:transmit!
                    (fn [{:keys [active-requests] :as this} request]
                      (handle-remote-interaction this request))}}})))

(defn counter-button-click [b] (update b :ui/clicks inc))

(defmutation increment-button [_]
  (action [{:keys [state ref]}]
          (swap! state update-in ref counter-button-click)))

(declare CounterButton)

(defmutation add-button [{:button/keys [id label] :as button}]
  (action [{:keys [state]}]                                 ; OPTIMISTIC UPDATE
          (swap! state
                 merge/merge-component CounterButton button
                 :append [:component/id :top-container :buttons]))
  (remote [env] true #_(-> env
                           (m/returning CounterButton)
                           (m/with-target (target/append-to [:component/id :top-container :buttons])))))

(defsc CounterButton [this {:ui/keys     [clicks error-message]
                            :button/keys [id label color] :as props}]
  {:query         [:ui/clicks :button/id :button/color
                   :ui/error-message
                   :button/border :button/label]
   :ident         :button/id
   :initial-state {:button/id    (or :param/id (random-uuid))
                   :button/color "blue"
                   :ui/clicks    (or :param/start-at 0)}}
  (dom/li nil
          (if error-message
            (dom/div error-message)
            (dom/button {:style   {:backgroundColor color
                                   :color           "white"}
                         :onClick (fn [] (comp/transact!! this [(increment-button)]))}
                        (str label " w/clicks " clicks)))))

(def ui-counter-button (comp/factory CounterButton {:keyfn :button/id}))

(defsc TopContainer [this {:keys    [buttons]
                           :ui/keys [new-b-color new-b-label]}]
  {:query         [{:buttons (comp/get-query CounterButton)}
                   :ui/new-b-color :ui/new-b-label]
   :ident         (fn [] [:component/id :top-container])
   :initial-state {:buttons []}}
  (dom/div nil
           (dom/input {:value       (str new-b-color)
                       :placeholder "Insert color"
                       :onChange    #(m/set-value! this :ui/new-b-color (evt/target-value %))})
           (dom/input {:value       (str new-b-label)
                       :onChange    #(m/set-string! this :ui/new-b-label :event %)
                       :placeholder "Insert label"})
           (dom/button
             {:onClick (fn [] (comp/transact! this [(add-button {:button/id    (tempid/tempid)
                                                                 :button/color (or new-b-color "black")
                                                                 :button/label (or new-b-label "default")})]))}
             "Add a button")
           (dom/h2 "My Buttons")
           (dom/ul nil
                   (mapv ui-counter-button buttons))))
(def ui-top-container (comp/factory TopContainer))

(defsc Root [this {:keys [top-container]}]
  {:query         [{:top-container (comp/get-query TopContainer)}]
   :initial-state {:top-container {}}}
  (ui-top-container top-container))

(defn refresh []
  (app/mount! app Root "app"))

(defn init []
  (button/load-db!)
  (refresh)
  (add-fulcro-inspect! app)
  (df/load! app :buttons CounterButton {:target [:component/id :top-container :buttons]}))
