(ns passa-paraula.navigation
  (:require [secretary.core :as secretary :include-macros true]
            [reagent.session :as session]
            [goog.events :as events]
            [goog.history.EventType :as EventType])
  (:import goog.History))

(declare home-page about-page end-page)


;; -------------------------
;; Routes
(secretary/set-config! :prefix "#")

(defn current-page []
  [:div [(session/get :current-page)]])

(secretary/defroute "/" []
  (session/put! :current-page #'home-page))

(secretary/defroute "/about" []
  (session/put! :current-page #'about-page))

(secretary/defroute "/end" []
  (session/put! :current-page #'end-page))

;; -------------------------
;; History
;; must be called after routes have been defined
(defn hook-browser-navigation! []
  (doto (History.)
    (events/listen
     EventType/NAVIGATE
     (fn [event]
       (secretary/dispatch! (.-token event))))
    (.setEnabled true)))
