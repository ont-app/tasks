# tasks

UNDER CONSTRUCTION

This aims to provide an ontology with accomanying logic to support performing a task analysis, and for modeling a task as an IGraph-based model. 

This ontology is encoded in Clojure in graphs implementing the IGraph protocol. 
It aims to align with RDF representaions.

## Overview

The ontology allows expressing the answers to questions like:

- What user goals are in play?
- Which of these are the primary goals, definitive of your application or service?
- Why pursue such goals? (what larger goals do they support?)
- When are you done?
- How do you know you're done?
- What states might you be in that fall short of being done?
- How do you know you're in those states?
- What can you do about being in those states?
- How do you know that that's what you can do?

Watch this space for more details


## Setup

To get an interactive development environment run:

    lein figwheel

and open your browser at [localhost:3449](http://localhost:3449/).
This will auto compile and send all changes to the browser without the
need to reload. After the compilation process is complete, you will
get a Browser Connected REPL. An easy way to try it is:

    (js/alert "Am I connected?")

and you should see an alert in the browser window.

To clean all compiled files:

    lein clean

To create a production build run:

    lein do clean, cljsbuild once min

And open your browser in `resources/public/index.html`. You will not
get live reloading, nor a REPL. 

## License

Copyright © 2019 Eric D. Scott

Distributed under the Eclipse Public License either version 1.0 or (at your option) any later version.
