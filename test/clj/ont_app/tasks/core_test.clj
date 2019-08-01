(ns ont-app.tasks.core-test
  (:require [clojure.test :refer :all]
            [ont-app.igraph.core :refer [normal-form add]]
            [ont-app.igraph.graph :as g]
            [ont-app.prototypes.core :as proto]
            [ont-app.vocabulary.core :as voc]
            [ont-app.tasks.core :as tasks]
            ))

(deftest ontology-test
  (testing "Ontology declaration should exist"
    (is (not (nil? (tasks/ontology))))))
