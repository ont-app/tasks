(ns ont-app.tasks.core
  (:require
   [ont-app.igraph.core :as igraph :refer [add subtract traverse reduce-s-p-o]]
   [ont-app.igraph.graph :as g]
   [ont-app.vocabulary.core :as voc]
   [ont-app.igraph-vocabulary.core :as igv]
   [ont-app.prototypes.core :as proto]
   [taoensso.timbre :as log]
   #?(:clj [ont-app.igraph-vocabulary.io :as igv-io])
   #?(:cljs [ont-app.tasks.ont :as ont])
   ;; normal form translation of tasks.ttl, generated at compile time
   ;; via macro and code in clj-based ont-app.igraph-vocabulary.io

   )
  )

(voc/cljc-put-ns-meta!
 'ont-app.tasks.core
 {                   
  :rdfs/comment "Holds constructs for task analysis and modeling"
  :vann/preferredNamespacePrefix "tasks"
  :vann/preferredNamespaceUri "http://rdf.naturallexicon.org/tasks/ont#"
  })


(declare ontology-cache)
(defn clear-caches! []
  "SIDE EFFECTS: resets caches to initial state."
  (reset! ontology-cache nil))

;;;;;;;;;;;;;;;;;;;;;;;;;;
;; FUN WITH READER MACROS
;;;;;;;;;;;;;;;;;;;;;;;;;;
#?(:cljs
   (enable-console-print!)
   ;; (defn on-js-reload [])
   )

(defn read-ontology []
  #?(:cljs (add (g/make-graph)
                ont/ontology-source)
     :clj
     (let [source "edn/tasks.edn"]
       (igv-io/read-graph-from-source source))))

(defn error [msg] #?(:clj (Error. msg)
                     :cljs (js/Error msg)))


;; NO READER MACROS BELOW THIS POINT

(def ontology-cache (atom nil))
(defn ontology []
  "The supporting ontology for prototypes, as an Igraph.graph, using keyword
  identifiers interned per ont-app.vocabulary. 
  "
  (when-not @ontology-cache
    (reset! ontology-cache
            (read-ontology)))
  @ontology-cache)

