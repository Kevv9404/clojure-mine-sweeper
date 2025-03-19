(ns com.example.example1
  (:require
    [clojure.core.async :as async]
    [com.fulcrologic.fulcro.algorithms.data-targeting :as target]
    [com.fulcrologic.fulcro.algorithms.merge :as merge]
    [com.fulcrologic.fulcro.algorithms.tempid :as tempid]
    [com.fulcrologic.fulcro.algorithms.tx-processing :as txn]
    [com.fulcrologic.fulcro.application :as app]
    [com.fulcrologic.fulcro.components :as comp :refer [defsc]]
    [com.fulcrologic.fulcro.data-fetch :as df]
    [com.fulcrologic.fulcro.dom :as dom]
    [com.fulcrologic.fulcro.mutations :as m :refer [defmutation]]
    [com.fulcrologic.fulcro.react.version18 :as v18]
    [edn-query-language.core :as eql]
    [fulcro.inspect.tool :refer [add-fulcro-inspect!]]))

(defonce next-id (atom 100))
(defonce buttons-on-server (atom [{:button/id 1 :button/label "A" :button/color "black"}
                                  {:button/id 2 :button/label "B" :button/color "black"}]))

(defn handle-remote-interaction [this {::txn/keys [ast result-handler] :as request}]
  (async/go
    (async/<! (async/timeout 200))
    (let [txn (eql/ast->query ast)
          {:keys [key params]} (-> ast :children first)]
      (cond
        (= :buttons key)
        (result-handler
          {:transaction txn
           :status-code 200
           :body        {:buttons @buttons-on-server}})

        (and (symbol? key) (= "add-button" (name key)))
        (let [new-button  (merge params {:button/color "black" :button/border "green"})
              incoming-id (:button/id params)
              real-id     (swap! next-id inc)
              new-button  (assoc new-button :button/id real-id)]
          (println new-button)
          (swap! buttons-on-server conj new-button)
          (result-handler
            {:transaction txn
             :status-code 200
             :body        {key (assoc new-button
                                 :tempids {incoming-id real-id})}}))

        :else
        (result-handler
          {:transaction txn
           :status-code 200
           :body        {}})))))

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
      :append [:buttons]))
  (remote [env]
    (-> env
      (m/returning CounterButton)
      (m/with-target (target/append-to [:buttons])))))

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

(defsc Root [this {:keys [buttons]}]
  {:query         [{:buttons (comp/get-query CounterButton)}]
   :initial-state {:buttons []}}
  (dom/div nil
    (dom/button
      {:onClick (fn [] (comp/transact! this [(add-button {:button/id    (tempid/tempid)
                                                          :button/color "green"
                                                          :button/label "Boo"})]))}
      "Add a button")
    (dom/h2 "My Buttons")
    (dom/ul nil
      (mapv ui-counter-button buttons))))

(defn refresh []
  (app/mount! app Root "app"))

(defn init []
  (refresh)
  (add-fulcro-inspect! app)
  (df/load! app :buttons CounterButton))
