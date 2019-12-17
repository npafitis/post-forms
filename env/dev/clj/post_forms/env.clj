(ns post-forms.env
  (:require
    [selmer.parser :as parser]
    [clojure.tools.logging :as log]
    [post-forms.dev-middleware :refer [wrap-dev]]))

(def defaults
  {:init
   (fn []
     (parser/cache-off!)
     (log/info "\n-=[post-forms started successfully using the development profile]=-"))
   :stop
   (fn []
     (log/info "\n-=[post-forms has shut down successfully]=-"))
   :middleware wrap-dev})
