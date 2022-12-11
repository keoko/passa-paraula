(ns passa-paraula.core
  (:require
   [passa-paraula.game :as game]
   [passa-paraula.events :as events]
   [passa-paraula.ui.views :as view]
   [reagent.dom :as d]))

;; -------------------------
;; Initialize app

(defn mount-root []
  (d/render [view/current-page] (.getElementById js/document "app")))

(defn ^:export init! []
  (game/reset-state!)
  (view/recalculate-window-center!)
  (events/hook-keyboard-listener!)
  (events/hook-clock-update!)
  (events/hook-window-resize!)
  (events/hook-window-blur!)
  (view/init!)
  (mount-root))
