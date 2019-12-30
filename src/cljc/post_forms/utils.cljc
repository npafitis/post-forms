(ns post-forms.utils)

(defn map-to-vector [map]
  (into []
        (for [path (keys map)]
          (hash-map path (map path)))))
