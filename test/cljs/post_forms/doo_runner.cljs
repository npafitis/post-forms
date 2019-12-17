(ns post-forms.doo-runner
  (:require [doo.runner :refer-macros [doo-tests]]
            [post-forms.core-test]))

(doo-tests 'post-forms.core-test)

