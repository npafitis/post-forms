(ns post-forms.routes.swagger
  (:require [post-forms.middleware :as middleware]
            [post-forms.utils :refer [map-to-vector]]
            [ring.util.request :refer [body-string]]
            [ring.util.response :refer [response]]
            [ring.util.http-response :as http-response]
            [clojure.string :as str]))

(def definitions (atom nil))

(defn get-endpoints [swagger-json]
  (map-to-vector (swagger-json "paths")))

(defn advanced-form-field [class]
  nil)

(defn basic-form-field [parameter]
  (let [type (parameter "type")
        name (parameter "name")]
    ({"boolean" {:checkbox
                 {:label name}}
      "string" {"input-text"
                {:label name}}
      "integer" {"input-text"
                 {:label name
                  :validation-regex  "\\d+"}}}
     type)))


(defn extract-parameter-field [parameter]
  (prn "Extracting Parameter Form Field")
  (if (contains? parameter "schema")
    (if (contains? (parameter "schema") "$ref")
      (let [classname (->
                       parameter
                       (get "schema")
                       (get "$ref")
                       (str/split #"/")
                       (last))
            class (@definitions classname)]
        (advanced-form-field class))
      nil)
    (basic-form-field parameter)))

(defn extract-method-form [method]
  (let [method-key (key method)
        method-val (val method)]
    (prn "Extract Method Form")
    {method-key
     (for [parameter (method-val "parameters")]
       (extract-parameter-field parameter))}))

(defn analyze-endpoint-methods [path]
  (let [endpoint-key (first (keys path)) ;; endpoint-key is the string of the endpoint e.g "/foo/bar
        endpoint (path endpoint-key)]
    {endpoint-key
     (for [method endpoint]
       (extract-method-form method))}))

(defn analyze-endpoints [swagger-json]
  (let [paths (get-endpoints swagger-json)]
    (reset! definitions (swagger-json "definitions"))
    (for [path paths]
      (analyze-endpoint-methods path))))

(defn analyze-swagger-handler [request]
  (let [swagger-json (request :body)]
    (->
     (http-response/ok (analyze-endpoints swagger-json))
     (http-response/header "Content-Type" "text/plain; charset=utf-8"))))

(defn swagger-routes []
  ["/swagger"
   ["/analyze" {:post analyze-swagger-handler}]])
