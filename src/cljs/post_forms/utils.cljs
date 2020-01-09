(ns post-forms.utils)

(defn log [& args]
  (.apply js/console.log js/console (to-array args)))
