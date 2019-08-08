(ns ont-app.tasks.ont
  (:require
   [ont-app.igraph.core :as igraph :refer [add
                                           subtract
                                           traverse
                                           reduce-s-p-o
                                           ]]
   [ont-app.igraph.graph :as g]
   [ont-app.vocabulary.core :as voc]
   [ont-app.igraph-vocabulary.core :as igv]
   [taoensso.timbre :as log]
   #?(:clj [ont-app.igraph-vocabulary.io :as igv-io])
   )
  #?(:cljs (:require-macros
            [ont-app.igraph-vocabulary.macros :refer [graph-source]]
            ))
  )

(voc/cljc-put-ns-meta!
 'ont-app.tasks.ont
 {                   
  :rdfs/comment "Holds constructs for task analysis and modeling"
  :vann/preferredNamespacePrefix "tasks"
  :vann/preferredNamespaceUri "http://rdf.naturallexicon.org/tasks/ont#"
  })


;;;;;;;;;;;;;;;;;;;;;;;;;;
;; FUN WITH READER MACROS
;;;;;;;;;;;;;;;;;;;;;;;;;;

#?(:cljs
   ;; Call a macro to reify the contents of the edn as cljs...
   (def ontology-source (graph-source "edn/tasks.edn"))
   )

(defn read-ontology []
  #?(:cljs (add (g/make-graph)
                ontology-source)
     :clj
     (igv-io/read-graph-from-source "edn/tasks.edn")))

;; NO READER MACROS BELOW THIS POINT


^{:doc "
  The supporting ontology for task analysis and modeling, as an 
  Igraph.graph, using keyword identifiers interned per ont-app.vocabulary. "
  }
(defonce ontology
  (let []
    (voc/clear-caches!)
    (reduce-s-p-o igv/resolve-namespace-prefixes
                  (g/make-graph)
                  (read-ontology))))
