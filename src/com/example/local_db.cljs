(ns com.example.local-db
  (:require [com.fulcrologic.fulcro.algorithms.transit :as transit]))

(defonce server (atom {:buttons {1 {:button/id 1 :button/label "A" :button/color "black"}
                                 2 {:button/id 2 :button/label "B" :button/color "black"}}
                       :address {3 {:address/id 3 :address/street "initial-street"}}
                       :people  {1 {:person/id      1
                                    :person/address 3
                                    :person/name    "initial-name"}}}))

(defonce next-id (atom 100))

(defn save-db! []
  (.. js/window -localStorage (setItem "db" (transit/transit-clj->str {:next-id @next-id
                                                                       :server  @server}))))

(defn load-db! []
  (let [s (.. js/window -localStorage (getItem "db"))
        {:keys [next-id server]} (transit/transit-str->clj s)]
    (when (and next-id server)
      (reset! com.example.local-db/server server)
      (reset! com.example.local-db/next-id next-id))))
