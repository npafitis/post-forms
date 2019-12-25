(ns post-forms.routes.swagger
  (:require [post-forms.middleware :as middleware]
            [ring.util.request :refer [body-string]]
            [ring.util.response :refer [response]]
            [ring.util.http-response :as http-response]))

(defn analyze-swagger-handler [request]
  (->
   (http-response/ok {:foo "bar"})
   (http-response/header "Content-Type" "text/plain; charset=utf-8")))

(defn swagger-routes []
  ["/swagger"
   ["/analyze" {:post analyze-swagger-handler}]])
