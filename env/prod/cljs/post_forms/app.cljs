(ns post-forms.app
  (:require [post-forms.core :as core]))

;;ignore println statements in prod
(set! *print-fn* (fn [& _]))

(core/init!)
