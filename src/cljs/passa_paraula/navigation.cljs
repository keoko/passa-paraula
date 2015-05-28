(ns passa-paraula.navigation
  (:require [secretary.core :as secretary :include-macros true]
            [reagent.session :as session]
            [goog.events :as events]
            [goog.history.EventType :as EventType])
  (:import goog.History))

(declare home-page about-page end-page)


;; -------------------------
;; Routes
