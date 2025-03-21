(ns com.example.example1
  (:require
    [clojure.core.async :as async]
    [com.example.local-db :as local-db]
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
    (let [txn                  (eql/ast->query ast)
          response             (parser/parser {} txn)
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
(declare Person)

(defmutation add-button [{:button/keys [id label] :as button}]
  (action [{:keys [state]}]                                 ; OPTIMISTIC UPDATE
    (swap! state
      merge/merge-component CounterButton button
      :append [:component/id :top-container :buttons]))
  (remote [env] true))

(defmutation add-person [person]
  (action [{:keys [state]}]
    (swap! state merge/merge-component Person person
      :append [:component/id :top-container :people]))
  (remote [_] true))

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
        (str label "w/clicks" clicks)))))

(def ui-counter-button (comp/factory CounterButton {:keyfn :button/id}))

(defsc Address [_ _]
  {:query [:address/id :address/street]
   :ident :address/id})

(def ui-address (comp/factory Address {:keyfn :address/id}))

(defsc Person [_ {:person/keys [id name address]}]
  {:query [:person/id :person/name {:person/address (comp/get-query Address)}]
   :ident :person/id}
  (dom/div {:key id :className " border-b pb-2 mb-2"}
    (dom/p {:className " font-medium "} (str " Name: " name))
    (dom/p {:className " text-gray "} (str " Address: " (:address/street address)))))

(def ui-person (comp/factory Person {:keyfn :person/id}))

(defn ui-add-person-data-input [this value key label]
  (dom/div {:className "flex-1"}
    (dom/label {:className "block text-sm font-medium text-gray mb-1"} label)
    (dom/input {:className   "w-full border p-2 rounded"
                :value       value
                :placeholder (str "Enter person " label)
                :onChange    #(m/set-string! this key :event %)})))

(defn ui-add-person-button [this new-person-name new-person-address]
  (let [can-add? (and (seq new-person-name) (seq new-person-address))]
    (dom/div {:className "ml-4 self-end"}
      (dom/button {:className (if can-add? "px-4 py-2 rounded text-white bg-blue" "px-4 py-2 rounded text-black bg-grey")
                   :disabled  (not can-add?)
                   :onClick   #(comp/transact! this [(add-person {:person/id      (tempid/tempid)
                                                                  :person/name    new-person-name
                                                                  :person/address {:address/id     (tempid/tempid)
                                                                                   :address/street new-person-address}})])}
        " Add Person "))))

(defsc TopContainer [this {:keys    [buttons people]
                           :ui/keys [new-person-name new-person-address]}]
  {:query         [{:buttons (comp/get-query CounterButton)}
                   {:people (comp/get-query Person)}
                   :ui/new-person-name
                   :ui/new-person-address]
   :ident         (fn [] [:component/id :top-container])
   :initial-state {:people                []
                   :buttons               []
                   :ui/new-person-name    ""
                   :ui/new-person-address ""}}
  (dom/div {:className "flex flex-col items-center space-y-4 p-4"}

    (dom/h2 {:className "text-xl font-bold mb-4"} "People Management")

    (dom/div {:className "flex w-full mb-6 items-center"}
      (dom/div {:className "flex-grow flex space-x-4"}
        (ui-add-person-data-input this new-person-name :ui/new-person-name "Name")
        (ui-add-person-data-input this new-person-address :ui/new-person-address "Address"))

      (ui-add-person-button this new-person-name new-person-address))

    (dom/div {:className "w-full"}
      (dom/h3 {:className "text-lg font-medium mb-2"} " People List ")
      (dom/div {:className " grid gap-4 p-4 bg-white shadow-lg rounded-lg "}
        (mapv ui-person people)))))



#_(defsc TopContainer [this {:keys    [buttons people]
                             :ui/keys [new-b-color new-b-label]}]
    {:query         [{:buttons (comp/get-query CounterButton)}
                     {:people (comp/get-query Person)}
                     :ui/new-b-color :ui/new-b-label]
     :ident         (fn [] [:component/id :top-container])
     :initial-state {:people  []
                     :buttons []}}
    (dom/div
      #_(dom/div nil
          (dom/input {:value       (str new-b-color)
                      :placeholder " Insert color "
                      :onChange    #(m/set-value! this :ui/new-b-color (evt/target-value %))})
          (dom/input {:value       (str new-b-label)
                      :onChange    #(m/set-string! this :ui/new-b-label :event %)
                      :placeholder " Insert label "})
          (dom/button
            {:onClick (fn [] (comp/transact! this [(add-button {:button/id    (tempid/tempid)
                                                                :button/color (or new-b-color " black ")
                                                                :button/label (or new-b-label " default ")})]))}
            " Add a button ")
          (dom/h2 " My Buttons ")
          (dom/ul nil
            (mapv ui-counter-button buttons)))))

(def ui-top-container (comp/factory TopContainer))

(defsc Root [this {:keys [top-container]}]
  {:query         [{:top-container (comp/get-query TopContainer)}]
   :initial-state {:top-container {}}}
  (ui-top-container top-container))

(defn refresh []
  (app/mount! app Root "app"))

(defn init []
  (local-db/load-db!)
  (refresh)
  (add-fulcro-inspect! app)
  (df/load! app :buttons CounterButton {:target [:component/id :top-container :buttons]})
  (df/load! app :people Person {:target [:component/id :top-container :people]}))
