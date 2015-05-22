(ns passa-paraula.prod
  (:require [passa-paraula.core :as core]))

;;ignore println statements in prod
(set! *print-fn* (fn [& _]))

(core/init!)
