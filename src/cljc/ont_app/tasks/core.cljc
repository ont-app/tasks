(ns ont-app.tasks.core
  (:require
   [ont-app.igraph.core :as igraph :refer [add subtract traverse reduce-s-p-o]]
   [ont-app.igraph.graph :as g]
   [ont-app.vocabulary.core :as voc]
   [ont-app.igraph-vocabulary.core :as igv]
   [ont-app.prototypes.core :as proto]
   [ont-app.tasks.ont :as ont]
   [taoensso.timbre :as log]
   )
  )

(voc/cljc-put-ns-meta!
 'ont-app.tasks.core
 {
  :voc/mapsTo 'ont-app.tasks.ont
  }
 )

;;;;;;;;;;;;;;;;;;;;;;;;;;
;; FUN WITH READER MACROS
;;;;;;;;;;;;;;;;;;;;;;;;;;
#?(:cljs
   (enable-console-print!)
   ;; (defn on-js-reload [])
   )

(defn error [msg] #?(:clj (Error. msg)
                     :cljs (js/Error msg)))


;; NO READER MACROS BELOW THIS POINT

(def ontology
  "The supporting ontology for task analysis and modeling, as an 
  Igraph.graph, using keyword identifiers interned per ont-app.vocabulary. "
  ont/ontology)

;; TODO: add supporting logic as needed.
                   
