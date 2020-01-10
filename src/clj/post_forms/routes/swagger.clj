(ns post-forms.routes.swagger
  (:require [post-forms.middleware :as middleware]
            [post-forms.utils :refer [map-to-vector
                                      flatten-one-level]]
            [ring.util.request :refer [body-string]]
            [ring.util.response :refer [response]]
            [ring.util.http-response :as http-response]
            [clojure.string :as str]))

(declare advanced-form-field)

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
                 {:required required}}
      "string" (if (contains? parameter "enum")
                 {:dropdown
                  {:required required
                   :options (parameter "enum")}}
                 {:input-text
                  {:required required}})
      "integer" {:input-text
                 {:required required
                  :validation-regex  "\\d+"}}}
     type)))

(defn extract-property-field [visited-classes definitions]
  (fn [property]
    (let [property-name (first (keys property))
          property-value (property property-name)]
      (if (contains? property-value "$ref")
        (let [classname (get-definition-name property-value)
              class (definitions classname)]
          (if (not (some #(= classname %) visited-classes))
            (advanced-form-field class classname
                                 (conj
                                  visited-classes
                                  classname) definitions)
            {property-name {:ref classname}}))
        (if (contains? property-value "type")
          {:param-name property-name
           :param-structure (basic-parameter-field property-value property-name)}
          nil)))))

(defn advanced-form-field [class classname visited-classes definitions]
  (let [properties (map-to-vector (class "properties"))]
    {:classname classname
     :class-structure (map (extract-property-field visited-classes definitions) properties)}))

(defn extract-parameter-field [definitions]
  (fn [parameter]
    (if (contains? parameter "schema")
      (if (contains? (parameter "schema") "$ref")
        (let [classname (get-definition-name (parameter "schema"))
              class (definitions classname)]
          (advanced-form-field class classname '() definitions))
        basic-body-form-field)
      {:param-name (parameter "name")
       :param-structure (basic-parameter-field parameter (parameter "name"))})))

(defn extract-method-form [definitions]
  (fn [method]
    (let [method-key (key method)
          method-val (val method)
          parameters (method-val "parameters")]
      {:method method-key
       :params {:query-params (->>
                               parameters
                               (filter query?)
                               (map (extract-parameter-field definitions)))
                :body (->>
                       parameters
                       (filter body?)
                       (map (extract-parameter-field definitions))
                       (flatten-one-level))}})))

(defn analyze-endpoint-methods [definitions]
  (fn [path]
    (let [endpoint-key (first (keys path))
          endpoint (path endpoint-key)]
      {:endpoint endpoint-key
       :methods (map (extract-method-form definitions) endpoint)})))

(defn analyze-endpoints [swagger-json]
  (let [paths (get-endpoints swagger-json)
        definitions (swagger-json "definitions")]
    (map (analyze-endpoint-methods definitions) paths)))

(defn analyze-swagger-handler [request]
  (let [swagger-json (request :body)]
    (->
     (http-response/ok (analyze-endpoints swagger-json))
     (http-response/header "Content-Type" "application/json"))))

(defn swagger-routes []
  ["/swagger"
   ["/analyze" {:post analyze-swagger-handler}]])
