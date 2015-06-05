(ns passa-paraula.ui.views
  (:require [passa-paraula.game :as game]
            [reagent.core :as reagent :refer [atom]]))


(def ui-state (atom {})) 

(def status-colors {:ok "green"
                    :failed "red"
                    :pass "orange"
                    :init "blue"}) 


(def cur-letter-color "purple")
(def letter-color "black")
(def letter-width "1")

(defn set-letter-color [pos]
  (if (= pos (game/cur-letter-id))
    cur-letter-color
    (get status-colors (game/get-letter-status pos))))


(defn letter-circle-component [x y pos letter]
  ^{:key pos} [:div {:id pos
                     :style {:position "absolute"
                             :top (Math/round (+ (:center-y @ui-state) y))
                             :left  (Math/round (+ (:center-x @ui-state) x))
                             :width (:circle-radius @ui-state)
                             :height (:circle-radius @ui-state)
                             :border-radius "50%"
                             :font-size (/ (:circle-radius @ui-state) 2)
                             :color "#fff"
                             :line-height (str (:circle-radius @ui-state) "px")
                             :text-align "center"
                             :background (set-letter-color pos)}} 
               letter])



(defn build-circles []
  (let [get-circle (fn [x] {
                            :x (* (:radius @ui-state)  (Math/cos (-  (/ (* Math/PI 2 x) game/num-letters) (/ Math/PI 2))))
                            :y  (* (:radius @ui-state)  (Math/sin (-  (/ (* Math/PI 2 x) game/num-letters) (/ Math/PI 2))))
                            :pos x
                            :letter (get game/letters x)})]
       (map get-circle (range game/num-letters))))



(defn board-component []
  [:div
   [:div
    (for [circle (build-circles)]
      [letter-circle-component (:x circle) (:y circle) (:pos circle) (:letter circle)])]])


(defn format-time [t]
  (let [minutes (quot t 60)
        seconds (rem t 60)]
    (str
     (if (> 10 minutes) (str "0" minutes) minutes)
     ":"
     (if (> 10 seconds) (str "0" seconds) seconds))))

(defn message-component []
  (let [messages {:start "Press key 'S' to play"
                  :end "Press key 'S' to play again"
                  :pause "Press key 'S' to continue"}
        message (get messages (game/get-state))
        show (if (game/game-in-run?) "block" "none")]
    [:blink {:id "instructions" 
             :display show 
             :style {:display "block"
                     :text-align "center"}} message]))

(defn home-page []
  (let [div-top  (- (:center-y @ui-state) (/ (:radius @ui-state) 3))
        div-left (- (:center-x @ui-state) (/ (:radius @ui-state) 3))]
    [:div
     [:div {:style {:position "absolute"
                    :top (str div-top "px")
                    :left (str div-left "px")
                    :width (str (:radius ui-state) "px")}} 
      [:div {:id "score"
             :style {:display "inline-block"
                     :text-align "center"
                     :font-size "100px"}} 
       (game/get-score)]
      [:div {:id "time" 
             :style {:display "inline-block"
                     :text-align "center"}} 
       (format-time (game/get-time))]
      [message-component]]
     [board-component]])) 


(defn recalculate-window-center! []
  (let [width  (.-innerWidth js/window)
        height (.-innerHeight js/window)]
    (reset! ui-state {:center-x (/ width 2)
                      :center-y (/ height 2)
                      :circle-radius (/ width 20)
                      :radius (/ width 4.5)})))

(defn mount-root []
  (reagent/render [home-page] (.getElementById js/document "app")))
