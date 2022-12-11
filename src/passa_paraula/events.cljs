(ns passa-paraula.events
  (:require [passa-paraula.game :as game]
            [passa-paraula.ui.views :as view]))

(defn handle-keys [event]
  (when-let [key (.-charCode event)]
    (case key
      111 (game/handle-letter (game/cur-letter-id) :ok)
      112 (game/handle-letter (game/cur-letter-id) :pass)
      107 (game/handle-letter (game/cur-letter-id) :failed)
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

(defn hook-window-blur! []
  (.addEventListener js/window "blur" game/pause-game))
