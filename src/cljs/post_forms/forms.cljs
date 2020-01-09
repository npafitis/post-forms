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
     [:h2 "Form View"]]))



