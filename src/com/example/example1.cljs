(ns com.example.example1
  (:require
    [clojure.core.async :as async]
    [com.example.local-db :as local-db]
    [com.fulcrologic.fulcro.algorithms.merge :as merge]
    [com.fulcrologic.fulcro.algorithms.tempid :as tempid]
    [com.fulcrologic.fulcro.algorithms.tx-processing :as txn]
    [com.fulcrologic.fulcro.application :as app]
    [com.fulcrologic.fulcro.components :as comp :refer [defsc]]
    [com.fulcrologic.fulcro.data-fetch :as df]
    [com.example.parser :as parser]
    [taoensso.encore :as enc]
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

(declare PersonForm)

(defmutation add-button [{:button/keys [id label] :as button}]
  (action [{:keys [state]}]                                 ; OPTIMISTIC UPDATE
          (swap! state
                 merge/merge-component CounterButton button
                 :append [:component/id :top-container :buttons]))
  (remote [env] true))

(defmutation add-person [person]
  (action [{:keys [state]}]                                 ; OPTIMISTIC UPDATE
          (swap! state
                 merge/merge-component PersonForm person
                 :append [:person-form]))
  (remote [env] true))

(defmutation new-person-form [_]
  (action [{:keys [state]}]
          (merge/merge-component! app PersonForm {:person/address {:address/id (tempid/tempid)}
                                                  :person/id      (tempid/tempid)} :append [:person-form])))

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

(defn ui-field [{:keys [key top-class label-class input-class onChange value label] :as props}]
  (let [input-props (-> props
                        (assoc :className (or input-class "w-full border p-2 rounded"))
                        (dissoc :key :top-class :label-class :input-class :label))]
    (dom/div (cond-> {:className (or top-class "flex-1")}
                     key (assoc :key key))
             (when label
               (dom/label {:className (or label-class "block text-sm font-medium text-gray mb-1")} label))
             (dom/input input-props))))

(defn string-field-props [this field label]
  {:value    (str (get (comp/props this) field))
   :label    label
   :onChange #(m/set-string!! this field :event %)})

(defn ui-button-action [{:keys [disabled] :as props} & children]
  (apply dom/button
         (update props :classes (fnil conj [])
                 (if disabled "px-4 py-2 rounded text-black bg-grey" "px-4 py-2 rounded text-white bg-blue")) children))

(defsc AddressForm [this props]
  {:query [:address/id :address/street]
   :ident :address/id}
  (ui-field (string-field-props this :address/street "Street")))

(def ui-address-form (comp/factory AddressForm {:keyfn :address/id}))

(def name-pattern #"^[A-Za-z]+(?: [A-Za-z]+)*$")

(defn valid-age? [age]
  (contains? (set (range 18 99)) age))

(defn valid-name? [nm]
  (boolean (re-matches name-pattern nm)))

(defn valid-person-form? [{:person/keys [age name]}]
  (and (valid-age? age) (valid-name? name)))

(defsc PersonForm [this {:ui/keys     [original-name original-age]
                         :person/keys [id name age address] :as person}]
  {:query [:person/id :person/name :person/age {:person/address (comp/get-query AddressForm)}
           :ui/original-name :ui/original-age]
   :ident :person/id}
  (let [new? (tempid/tempid? id)
        dirty? (or new? (not= name original-name) (not= age original-age))]
    (dom/div {:className "flex items-center"}
             (ui-field (string-field-props this :person/name "Name"))
             (ui-field (string-field-props this :person/age "Age"))
             (ui-address-form address)
             (ui-button-action {:disabled (and (not dirty?) (valid-person-form? person))
                                :onClick  #(comp/transact! this [(add-person person)])} "Save"))))

(def ui-person-form (comp/factory PersonForm {:keyfn :person/id}))


(defsc TopContainer [this {:keys    [buttons]
                           :ui/keys [new-b-color new-b-label]}]
  {:query         [{:buttons (comp/get-query CounterButton)}
                   :ui/new-b-color :ui/new-b-label]
   :ident         (fn [] [:component/id :top-container])
   :initial-state {:buttons []}}
  (dom/div nil
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
                   (mapv ui-counter-button buttons))
           (ui-button-action {:onClick #(comp/transact! this [(new-person-form)])} "New person form")))

(def ui-top-container (comp/factory TopContainer))


(defsc Root [this {:keys [person-form top-container]}]
  {:query         [{:person-form (comp/get-query PersonForm)}
                   {:top-container (comp/get-query TopContainer)}]
   :initial-state {:top-container {}}}
  (dom/div
    (ui-top-container top-container)
    (mapv ui-person-form person-form)))

(defn refresh []
  (app/mount! app Root "app"))

(defn copy-original-person-data [person] (assoc person :ui/original-name (:person/name person)
                                                       :ui/original-age (:person/age person)))

(defn init []
  (local-db/load-db!)
  (refresh)
  (add-fulcro-inspect! app)
  (df/load! app :buttons CounterButton {:target [:component/id :top-container :buttons]})
  (df/load! app :people PersonForm {:target      [:person-form]
                                    :post-action (fn [{:keys [state] :as env}]
                                                   (swap! state update :person/id (fn [m] (enc/map-vals copy-original-person-data m))))}))

(comment

  )
