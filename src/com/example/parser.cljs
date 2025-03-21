(ns com.example.parser
  (:require
    [com.wsscode.pathom.connect :as pc]
    [com.example.resolvers.button :as r.button]
    [com.example.resolvers.person :as r.person]
    [com.wsscode.pathom.core :as p]))

(def all-resolvers [r.button/resolvers r.person/resolvers])

(def parser
  (p/parser {::p/env     {::p/reader               [p/map-reader
                                                    pc/reader2
                                                    pc/open-ident-reader
                                                    p/env-placeholder-reader]
                          ::p/placeholder-prefixes #{">"}}
             ::p/mutate  pc/mutate
             ::p/plugins [(pc/connect-plugin {::pc/register all-resolvers})
                          p/trace-plugin]}))

