(ns passa-paraula.core
    (:require [reagent.core :as reagent]
              [passa-paraula.game :as game]
              [passa-paraula.ui.views :as view]
              [passa-paraula.events :as events])) 


(defn init! []
  (game/reset-state!)
  (view/recalculate-window-center!)
  (events/hook-keyboard-listener!)
  (events/hook-clock-update!)
  (events/hook-window-resize!)
  (events/hook-window-blur!)
  (view/mount-root))
