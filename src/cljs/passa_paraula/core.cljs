(ns passa-paraula.core
    (:require [reagent.core :as reagent]
              [secretary.core :as secretary :include-macros true]
              [passa-paraula.game :as game]
              [passa-paraula.ui.views :as view]
              [secretary.core :as secretary :include-macros true]
              [reagent.session :as session]
              [goog.events :as events]
              [goog.history.EventType :as EventType])
    (:import goog.History)) 




(defn end-game []
  (secretary/dispatch! "/end"))


(defn handle-letter [letter-id status]
  (do
    (game/change-letter-status letter-id status)
    (game/update-score)
    (game/jump-next-letter)
    (when (game/end-game?)
      (end-game))))

(defn handle-keys [event]
  (when-let [key (.-charCode event)]
    (case key
      111 (handle-letter (game/cur-letter-id) :ok)
      112 (handle-letter (game/cur-letter-id) :pass)
      107 (handle-letter (game/cur-letter-id) :failed)
      (.log js/console (str "key pressed not valid" key)))))
 
(defn hook-keyboard-listener!
  []
   (.addEventListener js/window "keypress" handle-keys))


(defn tick []
  (game/update-time)
  (js/setTimeout tick 1000)
  (when (game/end-game?)
    (end-game)))

(defn hook-clock-update! []
  (js/setTimeout tick 1000))


(declare home-page about-page end-page)

;; -------------------------
;; Initialize app

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


(defn mount-root []
  (reagent/render [current-page] (.getElementById js/document "app")))



(defn init! []
  (game/reset-state!)
  (hook-browser-navigation!)
  (hook-keyboard-listener!)
  (hook-clock-update!)
  (mount-root))
