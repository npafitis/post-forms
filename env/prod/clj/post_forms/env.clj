(ns post-forms.env
  (:require [clojure.tools.logging :as log]))

(def defaults
  {:init
   (fn []
     (log/info "\n-=[post-forms started successfully]=-"))
   :stop
   (fn []
     (log/info "\n-=[post-forms has shut down successfully]=-"))
   :middleware identity})
