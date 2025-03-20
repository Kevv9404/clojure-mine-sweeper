(ns com.example.button
  (:require
    [com.wsscode.pathom.connect :as pc]
    [com.fulcrologic.fulcro.algorithms.transit :as transit]))

(defonce buttons-on-server (atom {1 {:button/id 1 :button/label "A" :button/color "black"}
                                  2 {:button/id 2 :button/label "B" :button/color "black"}}))

(defonce next-id (atom 100))
(defn save-db! []
  (.. js/window -localStorage (setItem "db" (transit/transit-clj->str {:next-id @next-id
                                                                       :buttons @buttons-on-server}))))

(defn load-db! []
  (let [s (.. js/window -localStorage (getItem "db"))
        {:keys [next-id buttons]} (transit/transit-str->clj s)]
    (when (and next-id buttons)
      (reset! buttons-on-server buttons)
      (reset! com.example.button/next-id next-id))))

(comment
  (save-db!))
(pc/defresolver button-resolver [env {:button/keys [id]}]
  {::pc/input  #{:button/id}
   ::pc/output [:button/color :button/label]}
  (get @buttons-on-server id))

(pc/defresolver buttons-resolver [env _]
  {::pc/output [{:buttons [:button/id]}]}
  {:buttons (mapv (fn [id] {:button/id id}) (keys @buttons-on-server))})

(pc/defmutation add-button [env {:button/keys [id color label] :as button}]
  {::pc/sym    'com.example.example1/add-button
   ::pc/output [:button/id :button/label :button/color]}
  (let [real-id (swap! next-id inc)
        new-button (assoc button :button/id real-id)]
    (swap! buttons-on-server assoc real-id new-button)
    (save-db!)
    (assoc new-button :tempids {id real-id})))

(def resolvers [button-resolver buttons-resolver add-button])
