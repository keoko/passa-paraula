(ns passa-paraula.core
    (:require [passa-paraula.game :as game]
              [passa-paraula.ui.views :as view]
              [passa-paraula.navigation :as nav]
              [passa-paraula.events :as events])) 



(defn init! []
  (game/reset-state!)
  (view/recalculate-window-center!)
  (nav/hook-browser-navigation!)
  (events/hook-keyboard-listener!)
  (events/hook-clock-update!)
  (events/hook-window-resize!)
  (nav/mount-root))
