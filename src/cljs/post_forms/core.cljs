(ns post-forms.core
  (:require
   [day8.re-frame.http-fx]
   [reagent.core :as r]
   [re-frame.core :as rf]
   [goog.events :as events]
   [goog.history.EventType :as HistoryEventType]
   [markdown.core :refer [md->html]]
   [post-forms.ajax :as ajax]
   [post-forms.events]
   [reitit.core :as reitit]
   [re-com.core :as re-com]
   [clojure.string :as string])
  (:import goog.History))

(defn nav-link [uri title page]
  [:a.navbar-item
   {:href   uri
    :class (when (= page @(rf/subscribe [:page])) :is-active)}
   title])

(defn navbar []
  (r/with-let [expanded? (r/atom false)]
    [:nav.navbar.is-info>div.container
     [:div.navbar-brand
      [:a.navbar-item {:href "/" :style {:font-weight :bold}} "post-forms"]
      [:span.navbar-burger.burger
       {:data-target :nav-menu
        :on-click #(swap! expanded? not)
        :class (when @expanded? :is-active)}
       [:span][:span][:span]]]
     [:div#nav-menu.navbar-menu
      {:class (when @expanded? :is-active)}
      [:div.navbar-start
       [nav-link "#/" "Home" :home]
       [nav-link "#/about" "About" :about]
       [nav-link "#/forms" "Forms" :forms]]]]))

(defn about-page []
  [:section.section>div.container>div.content
   [:img {:src "/img/warning_clojure.png"}]])

(defn home-page []
  [:section.section>div.container>div.content
   (when-let [docs @(rf/subscribe [:docs])]
     [:div {:dangerouslySetInnerHTML {:__html (md->html docs)}}])])

(defn forms-page []
  [:section.section>div.container>div.content
   [re-com/v-box
    :children [
               [re-com/h-box
                :children [
                           [re-com/box
                            :child [left-panel]
                            :size "auto"]
                           [re-com/box
                            :child "json-view"
                            :size "auto"]]]]]])

(defn page []
  [:div
   [navbar]
   [(pages @(rf/subscribe [:page]))]])

(defn left-panel []
  [re-com/box
   :size "auto"
   :child (let [selected-tab-id (r/atom (:id (first left-panel-tabs-definition)))
                change-tab #(reset! selected-tab-id %)]
            [re-com/v-box
             :children [[re-com/horizontal-tabs
                         :model selected-tab-id
                         :tabs left-panel-tabs-definition
                         :on-change change-tab]
                        [(left-panel-tabs @selected-tab-id)]
                        ]])])

(defn forms-view []
  [:div "Forms View"])

(defn swagger-view []
  [:div "Swagger View"])
;; -------------------------
;; Functionality
(defn log [& args]
  (.apply js/console.log js/console (to-array args)))

(def pages
  {:home #'home-page
   :about #'about-page
   :forms #'forms-page})

(def left-panel-tabs
  {::swagger #'swagger-view
   ::forms #'forms-view})

(def left-panel-tabs-definition
  [{:id ::forms
    :label "Forms"
    :say-this "Forms View"}
   {:id ::swagger
    :label "Swagger"
    :say-this "Swagger View"}])
;; -------------------------
;; Routes

(def router
  (reitit/router
   [["/" :home]
    ["/about" :about]
    ["/forms" :forms]]))

;; -------------------------
;; History
;; must be called after routes have been defined
(defn hook-browser-navigation! []
  (doto (History.)
    (events/listen
     HistoryEventType/NAVIGATE
     (fn [event]
       (let [uri (or (not-empty (string/replace (.-token event) #"^.*#" "")) "/")]
         (rf/dispatch
          [:navigate (reitit/match-by-path router uri)]))))
    (.setEnabled true)))

;; -------------------------
;; Initialize app
(defn mount-components []
  (rf/clear-subscription-cache!)
  (r/render [#'page] (.getElementById js/document "app")))

(defn init! []
  (rf/dispatch-sync [:navigate (reitit/match-by-name router :home)])
  (ajax/load-interceptors!)
  (rf/dispatch [:fetch-docs])
  (hook-browser-navigation!)
  (mount-components))
