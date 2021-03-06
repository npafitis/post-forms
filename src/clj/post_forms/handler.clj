(ns post-forms.handler
  (:require
   [post-forms.middleware :as middleware]
   [post-forms.layout :refer [error-page]]
   [post-forms.routes.home :refer [home-routes]]
   [reitit.ring :as ring]
   [ring.middleware.content-type :refer [wrap-content-type]]
   [ring.middleware.webjars :refer [wrap-webjars]]
   [post-forms.env :refer [defaults]]
   [mount.core :as mount]
   [post-forms.routes.swagger :refer [swagger-routes]]))

(mount/defstate init-app
  :start ((or (:init defaults) (fn [])))
  :stop  ((or (:stop defaults) (fn []))))

(mount/defstate app-routes
  :start
  (ring/ring-handler
   (ring/router
    [(home-routes)
     (swagger-routes)])
   (ring/routes
    (ring/create-resource-handler
     {:path "/"})
    (wrap-content-type
     (wrap-webjars (constantly nil)))
    (ring/create-default-handler
     {:not-found
      (constantly (error-page {:status 404, :title "404 - Page not found"}))
      :method-not-allowed
      (constantly (error-page {:status 405, :title "405 - Not allowed"}))
      :not-acceptable
      (constantly (error-page {:status 406, :title "406 - Not acceptable"}))}))))

(def file)

(defn app []
  (middleware/wrap-base #'app-routes))
