(ns com.example.resolvers.button
  (:require
    [com.example.local-db :as local-db]
    [com.wsscode.pathom.connect :as pc]))

(pc/defresolver button-resolver [env {:button/keys [id]}]
  {::pc/input  #{:button/id}
   ::pc/output [:button/color :button/label]}
  (get-in @local-db/server [:buttons id]))

(pc/defresolver buttons-resolver [env _]
  {::pc/output [{:buttons [:button/id]}]}
  {:buttons (mapv (fn [id] {:button/id id}) (keys (:buttons @local-db/server)))})

(pc/defmutation add-button [env {:button/keys [id] :as button}]
  {::pc/sym    'com.example.example1/add-button
   ::pc/output [:button/id :button/label :button/color]}
  (let [real-id    (swap! local-db/next-id inc)
        new-button (assoc button :button/id real-id)]
    (swap! local-db/server update-in [:buttons] assoc real-id new-button)
    (local-db/save-db!)
    (assoc new-button :tempids {id real-id})))

(def resolvers [button-resolver buttons-resolver add-button])

(comment
  (local-db/save-db!))