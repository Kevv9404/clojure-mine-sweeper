(ns com.example.resolvers.person
  (:require [com.wsscode.pathom.connect :as pc]
            [com.example.local-db :as local-db]))

(pc/defresolver address-resolver [_ {:address/keys [id]}]
  {::pc/input  #{:address/id}
   ::pc/output [:address/id :address/street]}
  (get-in @local-db/server [:address id]))

(pc/defresolver person-resolver [_ {:person/keys [id]}]
  {::pc/input  #{:person/id}
   ::pc/output [:person/id :person/name {:person/address [:address/id]}]}
  (let [person     (get-in @local-db/server [:people id])
        address-id (:person/address person)
        address    (get-in @local-db/server [:address address-id])]
    (assoc person :person/address (or address {:address/id address-id}))))

(pc/defresolver all-people-resolver [env _]
  {::pc/output [{:people [:person/id]}]}
  {:people (mapv (fn [id] {:person/id id}) (keys (:people @local-db/server)))})

(pc/defmutation add-person [env {:person/keys [id] :as person}]
  {::pc/sym    'com.example.example1/add-person
   ::pc/output [:person/id :person/name {:person/address [:address/id :address/street]}]}
  (let [address     (get person :person/address)
        address-id  (swap! local-db/next-id inc)
        person-id   (swap! local-db/next-id inc)
        new-address (assoc address :address/id address-id)
        new-person  (-> person
                      (assoc :person/id person-id)
                      (assoc :person/address address-id))]
    (swap! local-db/server assoc-in [:address address-id] new-address)
    (swap! local-db/server assoc-in [:people person-id] new-person)
    (local-db/save-db!)
    (assoc new-person
      :tempids {id person-id}
      :person/address new-address)))

(def resolvers [add-person all-people-resolver address-resolver person-resolver])
