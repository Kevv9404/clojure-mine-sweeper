(ns com.example.resolvers.person
  (:require [com.fulcrologic.fulcro.algorithms.tempid :as tempid]
            [com.wsscode.pathom.connect :as pc]
            [com.example.local-db :as local-db]))

(pc/defresolver address-resolver [_ {:address/keys [id]}]
  {::pc/input  #{:address/id}
   ::pc/output [:address/id :address/street]}
  (get-in @local-db/server [:address id]))

(pc/defresolver person-resolver [_ {:person/keys [id]}]
  {::pc/input  #{:person/id}
   ::pc/output [:person/id :person/name :person/age {:person/address [:address/id]}]}
  (-> @local-db/server
      (get-in [:people id])
      (update :person/address (fn [id] {:address/id id}))))

(pc/defresolver all-people-resolver [env _]
  {::pc/output [{:people [:person/id]}]}
  {:people (mapv (fn [id] {:person/id id}) (keys (:people @local-db/server)))})


(pc/defmutation add-person [env {:person/keys [id name address] :as person}]
  {::pc/sym    'com.example.example1/add-person
   ::pc/output [:person/id :person/name :person/age
                {:person/address [:address/id]}]}
  (let [address-id (:address/id address)
        real-address-id (if (tempid/tempid? address-id) (local-db/next-id :address) address-id)
        real-person-id (if (tempid/tempid? id) (local-db/next-id :people) id)
        new-address (assoc address :address/id real-address-id)
        new-person (assoc person :person/id real-person-id
                                 :person/name name
                                 :person/address real-address-id)]
    (swap! local-db/server assoc-in [:address real-address-id] new-address)
    (swap! local-db/server assoc-in [:people real-person-id] new-person)
    (local-db/save-db!)
    (assoc new-person :tempids {id real-person-id}
                      :person/address real-address-id)))

(def resolvers [add-person all-people-resolver address-resolver person-resolver])
