(ns post-forms.routes.swagger
  (:require [post-forms.middleware :as middleware]
            [ring.util.request :refer [body-string]]
            [ring.util.response :refer [response]]
            [ring.util.http-response :as http-response]))

(defn paths-to-vector [paths]
  (into []
        (for [path (keys paths)]
          (hash-map path (paths path)))))

(defn get-post-endpoints [swagger-json]
  (let [paths (swagger-json "paths")]
    (paths-to-vector paths)))

(defn analyze-swagger-handler [request]
  (let [swagger-json (first (request :body))]
    (->
     (http-response/ok (get-post-endpoints swagger-json))
     (http-response/header "Content-Type" "text/plain; charset=utf-8"))))

(defn swagger-routes []
  ["/swagger"
   ["/analyze" {:post analyze-swagger-handler}]])
