(ns post-forms.routes.swagger
  (:require [post-forms.middleware :as middleware]
            [ring.util.http-response :as response]))

(defn analyze-swagger-handler []
  (fn [_]
    (->
     (response/ok "ok")
     (response/header "Content-Type" "text/plain; charset=utf-8"))))

(defn swagger-routes []
  ["swagger"
   {:middleware [middleware/wrap-csrf
                 middleware/wrap-formats]}
   ["/analyze" {:post analyze-swagger-handler}]])
