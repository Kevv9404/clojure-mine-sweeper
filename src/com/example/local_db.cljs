(ns com.example.local-db
  (:require [com.fulcrologic.fulcro.algorithms.transit :as transit]))

(defonce server (atom {:buttons {1 {:button/id 1 :button/label "A" :button/color "black"}
                                 2 {:button/id 2 :button/label "B" :button/color "black"}}
                       :address {3 {:address/id 3 :address/street "Street from db"}}
                       :people  {1 {:person/id      1
                                    :person/address 3
                                    :person/name    "Robert"}}}))

(defn next-id [db-key]
  (inc (last (keys (db-key @server)))))

(defn save-db! []
  (.. js/window -localStorage (setItem "db" (transit/transit-clj->str {:server  @server}))))

(defn load-db! []
  (let [s (.. js/window -localStorage (getItem "db"))
        {:keys [ server]} (transit/transit-str->clj s)]
    (when server
      (reset! com.example.local-db/server server))))

(comment
  @server )
