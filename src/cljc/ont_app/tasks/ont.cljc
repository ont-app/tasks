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

;; TODO: Return the the question of representing this in RDF-formats
;; #?(:cljs
;;    ;; Call a macro to reify the contents of the edn as cljs...
;;    (def ontology-source (graph-source "edn/tasks.edn"))
;;    )

;; (defn read-ontology []
;;   #?(:cljs (add (g/make-graph)
;;                 ontology-source)
;;      :clj
;;      (igv-io/read-graph-from-source "edn/tasks.edn")))

;; ;; NO READER MACROS BELOW THIS POINT


;; ^{:doc "
;;   The supporting ontology for task analysis and modeling, as an 
;;   Igraph.graph, using keyword identifiers interned per ont-app.vocabulary. "
;;   }
;; (defonce ontology
;;   (let []
;;     (voc/clear-caches!)
;;     (reduce-s-p-o igv/resolve-namespace-prefixes
;;                   (g/make-graph)
;;                   (read-ontology))))


(def ontology-ref (atom (g/make-graph)))
(defn update-ontology [to-add]
  (swap! ontology-ref add to-add))

(update-ontology
 [
  [:tasks/Entity
   :rdfs/subClassOf :rdf/Resource
   :rdfs/comment "
Some phenomenon being tracked and categorized by some impliciit observer as 
a single thing. Typically expressed as a Noun in natural language."
   ]
  [:tasks/Relation
   :rdfs/subClassOf :rdf/Resource
   :rdfs/comment "
Some phenomenon being tracked and categorized by some implicit observer as a
relationship between a plurality of Entities, Typically a State or Event.
"
   ]
  [:tasks/Unspecified
   :rdf/type :rdf/Resource
   :rdfs/comment "Indicates that some parameter is deliberately left unspecified"
   ]
  [
   :dc/description
   :proto/aggregation :proto/Occlusive
   :rdfs/comment """
    :rdfs/comment is inclusive, :dc/description is occlusive.  Use
    :dc/description to specify the docstring to be rendered in the
    browser.  
        """
   ]

  [:tasks/planDescription
   :rdfs/rdfsSubPropertyOf :dc/description
   :proto/aggregation :proto/Occlusive
   :rdfs/comment """
        <goal> ta:planDescription <description>
        Asserts that <description> documents the place of <goal>
        in the overall plan of execution for the host application.
        Typically it starts off 'In order to .... it is necessary|helpful...
        to ..., followed by a description of subgoals one might take, and
        the circumstances under which you will be able to discern whether
        and when to take such steps.
        """
   ]
  ]
 )

;;;;;;;;;;;
;; STATES
;;;;;;;;;;;
(update-ontology
 [
  [:tasks/State
   :rdfs/subClassOf :task/Relation;
   :rdfs/comment """
        Refers to a state within the operation of the program. 
        """
   ]
  [:tasks/StateDisjunction
   :rdf/type :proto/Prototype
   :rdfs/subClassOf :tasks/State
   :proto/hasParameter :tasks/option
   :rdfs/comment """
        Refers to a state describable as one of a plurality of states.
        """
   ]

  [:tasks/option
   :rdf/type :rdf/Property
   :rdfs/domain :tasks/StateDisjunction
   :rdfs/range :tasks/State
   :rdfs/comment """
        <disjunctive state> :tasks/option <state>
        Asserts that we are in <disjunctive state> if we are in <state>
        """
    ]

  ;;;;;;;;;;;
  ;; EVENTS
  ;;;;;;;;;;;
  [:tasks/Event
   :rdfs/subClassOf :tasks/Relation
   :rdf/type :proto/Prototype
   :proto/hasParameter :tasks/outcome
   :rdfs/comment """
        Refers to an event while using an interface.
        """
   ]
  
  [:tasks/subEventOf
   :proto/aggregation :proto/Occlusive
   :rdf/type :proto/Property
   :rdfs/domain :tasks/Event
   :rdfs/range :tasks/Event
   :owl:inverseOf :tasks/hasSubEvent
   :rdfs/comment """
        <Event> :tasks/subEventOf <Larger Event>
        Asserts that <Event> happens in the course of <LargerEvent>
        """
   ]
  [:tasks/hasSubEvent
   :proto/aggregation :proto/Inclusive
   :rdfs/domain :tasks/Event
   :rdfs/range :tasks/Event
   :owl:inverseOf :tasks/subEventOf
   :rdfs/comment "
Asserts that some larger event has subordinate events whose outcomes 
represent intermediate states in the larger event.
"
   ]
  [:tasks/embed
   :proto/aggregation :proto/Occlusive
   :rdfs/domain :tasks/UserGoal
   :rdfs/range :tasks/UserGoal
   :rdfs/subPropertyOf :tasks/hasSubEvent
   :rdfs/comment "Asserts that some supporting goal should be embedded in the interface that presents the larger goal."
   ]
        
  [:tasks/Process
   :rdf/type :rdfs/Class
   :rdfs/comment """

        Refers to a process characterized by a set of typical states
        which follow each other in a cycle.

        """
   ]

  [:tasks/conditionedOn
   :proto/aggregation :proto/Inclusive
   :rdfs/comment """
        <event> :tasks/conditionedOn <state>;

        Asserts that any process involving <event> must be in <state> for
        <event> to occur.
        """
    ]

  [:tasks/engageWhen
   :proto/aggregation :proto/Inclusive
   :rdfs/domain :Process
   :rdfs/range :tasks/State
   :rdfs/comment """
        <process> :tasks/engageWhen <state>
        Asserts that <process> is expected be initiated when in <State>.
        """
    :owl/seeAlso :disengageWhen
   ]

  [:tasks/disengageWhen :proto/aggregation :proto/Inclusive
    :rdfs/domain :Process
    :rdfs/range :tasks/State
    :rdfs/comment """
        <process> :tasks/disengageWhen <state>
        Asserts that <process> is expected continue until we encounter <state>.
        """
    :owl/seeAlso :engageWhen
    ]

  [:tasks/outcome
   :proto/aggregation :proto/Occlusive
   :rdfs/range :tasks/State
   :owl/inverseOf :tasks/outcomeOf
   ]

  [:tasks/outcomeOf
   :proto/aggregation :proto/Inclusive
    :rdfs/comment """
        <state> :tasks/outcomeOf <event>

    Asserts thate <state> describes the expected outcome of <event>.
    
    This is typically expressed using the past particple X'd.
    
        """
   ]

  [:tasks/Operator
   :rdf/type  :proto/Prototype
   :rdfs/subClassOf :proto/UserGoal
   :proto/hasParameter :tasks/operatorLabel
   :proto/hasParameter :tasks/causes
   :rdfs/comment """
        Something on the interface that changes the state of the system.
        """
   ]
  

  [:tasks/operatorLabel
   :proto/aggregation :proto/Occlusive
   :rdfs/comment """
        <Operator> :tasks/operatorLabel <Label>
        Asserts that when rendered in the interface it should
        be rendered as <Label>
        """
   ]
    

  [:tasks/causedBy
   :proto/aggregation :proto/Inclusive
   :rdfs/domain :tasks/Relation
   :rdfs/range :Operator
   :rdfs/comment """
        <Event> :tasks/causedBy <Operator>.
        Asserts that the effect of using <Operator> is to cause <Event>
        """
   ]

  [:tasks/causes
   :proto/aggregation :proto/Inclusive
   :owl/inverseOf :causedBy
   :rdfs/comment """
        <Operator> :tasks/causes <Event>.
        Asserts that the effect of using <Operator> is to cause <Event>
        """
    ]

    
  [:tasks/followsInCycle
   :proto/aggregation :proto/Occlusive
   :rdfs/comment """
        <event2> followsInCycle <event1>

    Asserts that <event2> follows <event1> in a cycle of events in the
    context of some process.

        """
   ]
    

  [:tasks/determinedBy
   :rdf/type :proto/InclusveProperty
    :rdfs/domain :tasks/UserGoal
    :rdfs/domain :tasks/KnowledgeGoal
   :rdfs/comment """
        <Goal> :tasks/determinedBy <Inspect Something>
        Asserts that <Inspect something>  allows you 
        to determine the status of <Goal>.
        """
   ]
  ])


    
;; SERVICES and INTERFACES
(update-ontology
 [
  [:tasks/Service
   :rdf/type :proto/Prototype
   :rdf/type :rdfs/Class
   :rdfs/comment """
    Refers to a software service dedicated to one or more user goals
    """
   :proto/hasParameter :tasks/dedicatedTo
   ]
  
  [:tasks/dedicatedTo
   :proto/aggregation :proto/Inclusive
   :rdfs/domain :tasks/Service
   :rdfs/range :tasks/UserGoal
   :rdfs/comment """

    <interface> dedicatedTo <goal>

    Asserts that <interface> aims to implement a plan for achieving <goal>

    """;
   ]

  [:tasks/UserInterface
   :proto/elaborates :tasks/Service
   :rdf/type :tasks/Process
   :proto/hasParameter :tasks/engageWhen
   :proto/hasParameter :tasks/disengageWhen
   :rdfs/comment """
        Refers to an interactive Service 
        """;
   ]
  ])
;;;;;;;;;;;;;;;
;; USER GOALS
;;;;;;;;;;;;;;;
(update-ontology
 [
  [:tasks/UserGoal
   :rdfs/subClassOf :tasks/Relation
   :rdf/type :proto/Prototype
   :rdf/type :tasks/UserGoal
   :rdfs/comment """
        Refers to a goal pursued by you the user.
    """
   :proto/hasParameter :goalType
   ]

    
  [:tasks/goalType
   ;; TODO: consider changing this to EventDispatch
   :proto/aggregation :proto/Occlusive
   :proto/domain :tasks/UserGoal
   :rdfs/range :tasks/UserGoal
   :rdfs/comment """
        Refers to the unique primary class to which the referent goal 
        belongs. Typically keyed on to set new goals.
        """
   ]

  [:tasks/KnowledgeGoal
   :proto/elaborates :tasks/UserGoal
   :rdf/type :tasks/KnowledgeGoal
   :proto/hasParameter :tasks/subject
   :rdfs/comment """
        Refers to a goal to know something about the state of the 
        application or the wider world, typically by inspecting
        some part of the interface.
        """
    ]

  [:tasks/subject
   :proto/aggregation :proto/Inclusive
   :rdfs/domain :tasks/KnowledgeGoal
   :rdfs/range :rdf/Resource
   :rdfs/comment """
        <Knowabout> :tasks/subject <subject>;
        Asserts that <Knowabout> is dedicated to knowing about <subject>
        """
    ]
  ])

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; SPECIFIC CLASSES OF GOALS
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(update-ontology
 [
  [:tasks/Specify
   :rdf/type :proto/Prototype
   :rdf/type :tasks/UserGoal
   :proto/hasParameter :specified
   :proto/hasParameter :forPurpose
   :proto/hasParameter :as
   :rdfs/comment """
    <Specify> 
        specified <thing>;
        as <role>;
        forPurpose <purpose>;

    
    Work as an ensemble to assert that <thing> should serve
    <role> for <purpose>, typically such that
    <purpose> <role> <thing>.
        """
    ]

  [:tasks/specified
   :proto/aggregation :proto/Inclusive
   :rdfs/comment """
        The thing specified in a goal to specify.
        """
   ]

  [:tasks/as
   :proto/aggregation :proto/Exclusive
   :rdfs/comment """
        The role played by the specified thing(s)
        """
   ]
  
  [:tasks/forPurpose
   :proto/aggregation :proto/Exclusive
   :rdfs/comment """
        The purpose for which the thing was specified.
        """
   ]
    
  [:tasks/Select
   :proto/elaborates :tasks/Specify
    :rdf/type :tasks/Select
    :proto/hasParameter :task/fromCandidates
   ]

  [:tasks/fromCandidates
   :proto/aggregation :proto/Occlusive
   :rdfs/comment """ 
        
    A collection of candidates from which the specified thing(s)
    are selected.

    """
   ]

  [:tasks/SelectForPurpose
   :proto/elaborates :tasks/Select
    :proto/hasParameter :forPurpose
   :rdfs/comment """
        Refers to a
        """
   ]

  ;;;;;;;;;;;;;;;;;;
  ;; NAVIGATION
  ;;;;;;;;;;;;;;;;;;
  [:tasks/GoBack
   :rdf/type :tasks/UserGoal
   :rdfs/comment "A user goal to navigate backward."
   ]
  ;;;;;;;;;;;;;;;;;;
  ;; FINDING THINGS
  ;;;;;;;;;;;;;;;;;;
  
  [:tasks/FindData
   :proto/elaborates :tasks/UserGoal
    :rdfs/class :tasks/FindData
   :proto/hasParameter :targetDescription
   ]

  [:tasks/targetDescription
   :proto/aggregation :proto/Inclusive
   :rdfs/domain :tasks/FindData
   :rdfs/range :rdf/PlainLiteral
   :rdfs/comment """
    <find-data> :tasks/targetDescription <description>
    
    asserts <description> suits the data you are trying to find.
        Where

        """
   ]
  ])

        
;;;;;;;;;;;;;;;;;;;;;
;; DECISION SUPPORT
;;;;;;;;;;;;;;;;;;;;;
(update-ontology
 [
  [:tasks/Review
   :proto/elaborates :tasks/KnowledgeGoal
   :proto/hasParameter :tasks/fromDataSource
   :proto/hasParameter :tasks/queryFn
   :rdfs/comment "A user goal to review a set of elements drawn from some data 
source, typically by some kind of query, which should be expressed as a 
function of the model and the URI of the Review goal."
   ]

  [:tasks/fromDataSource
   :proto/aggregation :proto/Occlusive
   :rdfs/domain :tasks/Review
   :rdfs/range :tasks/DataSource
   :rdfs/comment "Asserts that <review> draws its data from <source>"
   ]
  [:tasks/queryFn
   :rdfs/comment "Asserts the URI of (fn [model review]...) -> #{<bmap>, ...}"
   :proto/aggregation :proto/Exclusive
   :rdfs/domain :tasks/Review
   :rdfs/range :tasks/QueryFunction
   ]
  [:tasks/QueryFunction
   :proto/elaborates :proto/Function
   :proto/argumentList [:?model :?reviewListingKwi]
   :rdfs/domain :tasks/ReviewListing
   :rdfs/comment "
   A function [model reviewListingKwi] -> <listing>
   Where
   <model> is the task model
   <reviewListingKwi> names the operative user goal
   <listing> := [<binding>, ...]
   <binding> is some appropriate map.
   "
   ]
  [:tasks/DataSource
   :rdfs/comment "Refers to some body of data containing elements to be queried against and reviewed"
   ]


        
;; REVIEW LISTING
  [:tasks/ReviewListing
   :proto/elaborates :tasks/Review
   :rdf/type :ReviewListing ;;aggregates to resulting description
   :proto/hasParameter :tasks/query
   :proto/hasParameter :tasks/queryFormat
   :proto/hasParameter :tasks/listing
   :proto/hasParameter :tasks/hasListingItem
   :proto/hasParameter :tasks/listingFormat
   :rdfs/comment """

    Refers to a user goal to review a listing of some set of data.
    
    Typically supports an elaboration on <find-data>

    """;

   ]

  
  [:tasks/query
   :proto/aggregation :proto/Exclusive
   :rdfs/domain :tasks/ReviewListing
   ;;range is unspecified and application-specific
   :rdfs/comment """

    <Review> :tasks/query <query>.
    <Review> :tasks/queryFormat <format>.

    Asserts that <query> is in <format> 

    Where
    <query> is in an application-specific <format>
        """
   ]

  [:tasks/QueryFormat
   :rdf/type :rdf/Class
    :rdfs/comment """
        Refers to a query format interpretable by some program as a query 
        that supports finding some body of data.
        """
    ]

  [:tasks/queryFormat
   :proto/aggregation :proto/Exclusive
   :rdfs/range :QueryFormat
   :rdfs/comment """

    <ReviewListing> :tasks/listing <list>.
    <ReviewListing> :tasks/query <query>.
    <ReviewListing> :tasks/queryFormat <format>.

    Asserts that <query> is in <format> and will generate data from which 
    <list> can be inferred.

    Where
    <query> is in an application-specific <format>
        """
   ]

  [:tasks/listing
   :proto/aggregation :proto/Exclusive
   :rdfs/domain :ReviewListing
    ;; range is application-specific
    :rdfs/comment """

    <ReviewListing> :tasks/listing <list>.
    <ReviewListing> :tasks/listingFormat <format>.


    asserts that <list> contains the listing to review, and should
    be rendered using <format>.
    
    Where
    <list> is in an application-specific format.


        """
   ]

  [:tasks/ListingFormat
   :rdf/type :rdf/Class
   :rdfs/comment """
        Refers to a format interpretable by some program as a list of 
        objects.
        """
   ]
    
  [:tasks/listingFormat
   :proto/aggregation :proto/Exclusive
    :rdfs/domain :tasks/ReviewListing
    :rdfs/range :tasks/ListingFormat
    ;; range is unspecified and application-specific
    :rdfs/comment """
    <ReviewListing> :tasks/listing <list>.
    <ReviewListing> :tasks/listingFormat <format>.

    asserts that <list> contains the listing to review, and should
    be rendered using <format>.
    
    Where
    <list> is in an application-specific format.
        """
   ]
  [:tasks/hasListingItem
   :proto/aggregation :proto/Inclusive
   :rdfs/domain :tasks/ReviewListing
   :rdfs/comment "An element of <listing>"
   ]

  [:tasks/ReviewGraphQuery
   :proto/elaborates :tasks/ReviewListing
   :rdfs/comment "A user goal to look at a listing of results to something implementing IGraph."
   ]
  [:tasks/ReviewIGraphGraphQuery
   :proto/elaborates :tasks/ReviewGraphQuery
   :tasks/goalType :tasks/ReviewIGraphGraphQuery
   :tasks/queryFormat :tasks/IGraphGraphQuery
   :tasks/listingFormat :tasks/IGraphQueryBindings
   :dc/description
   "A goal to review bindings from a query to an IGraph.Graph"
   ]
  [:tasks/subjectVariable
   :rdfs/domain :tasks/ReviewGraphQuery
   :rdfs/comment "
<Review> queryFn <fn>
         subjectVariable :?myVar
Asserts the :?myVar variable in <binding>  indicates the tasks/subject of knowledge goals related to the list elements being reviewed.
Where
<Review> is a goal to review a query posed to a graph
<fn> := (fn [model] -> [<binding>, ...]
<binding> := {:?myVar..., ...}
"
   ]
  [:tasks/IGraphGraphQuery
   :rdf/type :tasks/QueryFormat
   :rdfs/comment "Refers to a Query suitable for IGraph.Graph"
   ]
  [:tasks/IGraphQueryBindings
   :rdf/type :tasks/ListingFormat
   :rdfs/comment "Refers to a listing of bindings from some Query posed to an IGraph implementation "
   ]

  ;; INSPECT ENTITY

  [:tasks/InspectEntity
   :proto/elaborates :tasks/KnowledgeGoal
   :rdfs/comment """
        A user goal to inspect a single entity.
        """
   :rdf/type :InspectEntity
   ]

  [:tasks/Scan
   :proto/elaborates :tasks/KnowledgeGoal
   :rdf/type :tasks/Scan
   :proto/hasParameter :tasks/informsInvocationOf
   :rdfs/comment """
        A user goal to glance at a brief summary of some entity presented 
to the user, often to inform the decision whether and how to invoke some 
operator.
        """
    ]

  [:tasks/informsInvocationOf
   :proto/aggregation :proto/Inclusive
   :rdfs/domain :tasks/Scan
   :rdfs/range :task/Operator
   :rdfs/comment "
Asserts that some Scan goal informs the decision to invoke some Operator.
"
   ]
  [:tasks/ScanListingItem
   :proto/elborates :tasks/Scan
   :proto/hasParameter :tasks/inReviewListing
   :proto/hasParameter :tasks/listingIndex
   :rdfs/comment "Quick glance at a summary of an item in a listing of items under review."
   ]
  [:tasks/inReviewListing
   :proto/aggregation :proto/Exclusive
   :rdfs/domain :tasks/ScanListingItem
   :rdfs/range :tasks/ReviewListing
   ]
  [:tasks/listingIndex
   :proto/aggregation :proto/Exclusive
   :rdfs/domain :tasks/ScanListingItem
   :rdfs/range :xsd/integer
   :rdfs/comment "Asserts that some ScanListingItem is assocated with the 
member  so indexed in some goal to review a listing."
   ]

  [:tasks/ReadValue
   :proto/elaborates :tasks/KnowledgeGoal
   :rdfs/comment """
        A user goal to read the value of some property of some subject.
        """
    :proto/hasParameter :tasks/property
    ]
    
  [:tasks/property
   :proto/aggregation :proto/Exclusive
   :rdfs/domain :tasks/ReadValue
   :rdfs/comment """
        <Read> ::tasks/subject <s>
        <Read> :tasks/property <p>
        As an ensemble asserts <Read> is a goal to read (the (g s p))
        Where
        <g> is a graph holding the context of the current goal.
        """
   ]


  [:tasks/acknowledgedBy
   :proto/aggregation :proto/Inclusive
   :rdfs/domain :tasks/State
   :rdfs/range :Operator
   :rdfs/comment """
        <State> :tasks/acknowledgedBy <Operator>
        Asserts that <Operator> is used in the interface to indicate
        your judgement that <State> is true.
        """
   ]
  ])
    
(def ontology @ontology-ref)
    

    





        
