(ns passa-paraula.navigation
  (:require [reagent.core :as reagent]
            [secretary.core :as secretary :include-macros true]
            [secretary.core :as secretary :include-macros true]
            [reagent.session :as session]
            [goog.events :as events]
            [goog.history.EventType :as EventType]
            [passa-paraula.ui.views :as view])
  (:import goog.History))



(secretary/set-config! :prefix "#")

(defn current-page []
  [:div [(session/get :current-page)]])

(secretary/defroute "/" []
  (session/put! :current-page #'view/home-page))

(secretary/defroute "/about" []
  (session/put! :current-page #'view/about-page))

(secretary/defroute "/end" []
  (session/put! :current-page #'view/end-page))



(defn mount-root []
  (reagent/render [current-page] (.getElementById js/document "app")))

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
