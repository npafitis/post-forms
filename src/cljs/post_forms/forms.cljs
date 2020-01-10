(ns post-forms.forms
  (:require
   [post-forms.utils :refer [log]]
   [reagent.core :as r]
   [clojure.string :as string]
   [re-com.core :as re-com]
   [re-frame.core :as rf]))

(defn forms-view []
  (fn []
    (when-let [form-representation @(rf/subscribe [:form-representation])]
      (log "Hello World")
      (log form-representation)
      [(forms-view-content form-representation)])))

(defn forms-view-content [form-representation]
  (fn []
    [:div
     [:h2 "Form View"]
     (map extract-endpoint-view form-representation)]))

(defn extract-endpoint-view [endpoint-representation]
  (let [endpoint-name (endpoint-representation "endpoint")
        methods (endpoint-representation "methods")]
    [:div
     [:h3 endpoint-name]
     (map extract-method-view methods)]))

(defn extract-method-view [method-representation]
  (let [method-name (method-representation "method")]
    [:div [:h5 method-name]]))


