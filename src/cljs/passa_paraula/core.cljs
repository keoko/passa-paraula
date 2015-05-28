(ns passa-paraula.core
    (:require [passa-paraula.game :as game]
              [passa-paraula.ui.views :as view]
              [passa-paraula.navigation :as nav])) 


(defn handle-letter [letter-id status]
  (do
    (game/change-letter-status letter-id status)
    (game/update-score)
    (game/jump-next-letter)
    (when (game/end-game?)
      (nav/end-game))))

(defn handle-keys [event]
  (when-let [key (.-charCode event)]
    (case key
      111 (handle-letter (game/cur-letter-id) :ok)
      112 (handle-letter (game/cur-letter-id) :pass)
      107 (handle-letter (game/cur-letter-id) :failed)
      (.log js/console (str "key pressed not valid" key)))))
 
(defn hook-keyboard-listener! []
   (.addEventListener js/window "keypress" handle-keys))


(defn tick []
  (game/update-time)
  (js/setTimeout tick 1000)
  (when (game/end-game?)
    (nav/end-game)))

(defn hook-clock-update! []
  (js/setTimeout tick 1000))


(defn init! []
  (game/reset-state!)
  (nav/hook-browser-navigation!)
  (hook-keyboard-listener!)
  (hook-clock-update!)
  (nav/mount-root))
