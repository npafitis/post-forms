(ns post-forms.routes.swagger
  (:require [post-forms.middleware :as middleware]
            [post-forms.utils :refer [map-to-vector
                                      flatten-one-level]]
            [ring.util.request :refer [body-string]]
            [ring.util.response :refer [response]]
            [ring.util.http-response :as http-response]
            [clojure.string :as str]))

(declare advanced-form-field)

(def definitions (atom nil)) ;; Might have issue if application is deployed and concurrent applications are made on

(defn query? [parameter]
  (= (parameter "in") "query"))

(defn get-definition-name [definition]
  (->
   definition
   (get "$ref")
   (str/split #"/")
   (last)))

(defn body? [parameter]
  (= (parameter "in") "body"))

(defn get-endpoints [swagger-json]
  (map-to-vector (swagger-json "paths")))

(def basic-body-form-field
  {:json {:label "Body"}})

(defn basic-parameter-field [parameter name]
  (let [type (parameter "type")
        required (parameter "required")]
    ({"boolean" {:checkbox
                 {:label name
                  :required required}}
      "string" (if (contains? parameter "enum")
                 {:dropdown
                  {:label name
                   :required required
                   :options (parameter "enum")}}
                 {:input-text
                  {:label name
                   :required required}})
      "integer" {:input-text
                 {:label name
                  :required required
                  :validation-regex  "\\d+"}}}
     type)))

(defn extract-property-field [visited-classes]
  (fn [property]
    (let [property-name (first (keys property))
          property-value (property property-name)]
      (if (contains? property-value "$ref")
        (let [classname (get-definition-name property-value)
              class (@definitions classname)]
          (if (not (some #(= classname %) visited-classes))
            (advanced-form-field class (conj
                                        visited-classes
                                        classname))
            {property-name {:ref classname}}))
        (if (contains? property-value "type")
          (basic-parameter-field property-value property-name)
          nil)))))

(defn advanced-form-field [class visited-classes]
  (let [properties (map-to-vector (class "properties"))]
    (map (extract-property-field visited-classes) properties)))

(defn extract-parameter-field [parameter]
  (if (contains? parameter "schema")
    (if (contains? (parameter "schema") "$ref")
      (let [classname (get-definition-name (parameter "schema"))
            class (@definitions classname)]
        {classname
         (advanced-form-field class '())})
      basic-body-form-field)
    {(parameter "name")
     (basic-parameter-field parameter (parameter "name"))}))

(defn extract-method-form [method]
  (let [method-key (key method)
        method-val (val method)
        parameters (method-val "parameters")]
    {method-key
     {:query-params (->>
                     parameters
                     (filter query?)
                     (map extract-parameter-field))
      :body (->>
             parameters
             (filter body?)
             (map extract-parameter-field)
             (flatten-one-level))}}))

(defn analyze-endpoint-methods [path]
  (let [endpoint-key (first (keys path)) ;; endpoint-key is the string of the endpoint e.g "/foo/bar
        endpoint (path endpoint-key)]
    {endpoint-key
     (map extract-method-form endpoint)}))

(defn analyze-endpoints [swagger-json]
  (let [paths (get-endpoints swagger-json)]
    (reset! definitions (swagger-json "definitions"))
    (map analyze-endpoint-methods paths)))

(defn analyze-swagger-handler [request]
  (let [swagger-json (request :body)]
    (->
     (http-response/ok (analyze-endpoints swagger-json))
     (http-response/header "Content-Type" "text/plain; charset=utf-8"))))

(defn swagger-routes []
  ["/swagger"
   ["/analyze" {:post analyze-swagger-handler}]])
