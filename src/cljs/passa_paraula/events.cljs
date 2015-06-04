(ns passa-paraula.events
  (:require [passa-paraula.game :as game]
            [passa-paraula.navigation :as nav]
            [passa-paraula.ui.views :as view]))


(defn handle-letter [letter-id status]
  (when (game/game-in-run?)
    (game/change-letter-status letter-id status)
    (game/update-score)
    (game/jump-next-letter)
    (when (game/end-game?)
      (game/end-game))))

(defn handle-keys [event]
  (when-let [key (.-charCode event)]
    (case key
      111 (handle-letter (game/cur-letter-id) :ok)
      112 (handle-letter (game/cur-letter-id) :pass)
      107 (handle-letter (game/cur-letter-id) :failed)
      115 (game/toggle-status)
      (.log js/console (str "key pressed not valid" key)))))
 
(defn hook-keyboard-listener! []
   (.addEventListener js/window "keypress" handle-keys))


(defn tick []
  (game/update-time)
  (js/setTimeout tick 1000)
  (when (game/end-game?)
    (game/end-game)))

(defn hook-clock-update! []
  (js/setTimeout tick 1000))

(defn hook-window-resize! []
  (.addEventListener js/window "resize" view/recalculate-window-center!))

