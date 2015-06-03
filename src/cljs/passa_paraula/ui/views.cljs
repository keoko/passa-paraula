(ns passa-paraula.ui.views
  (:require [passa-paraula.game :as game]))


#_(def center-x 500)
#_(def center-y 500)

(def window-width (.-innerWidth js/window))
(def window-height (.-innerHeight js/window))

(def center-x (/ (.-innerWidth js/window) 2))
(def center-y (/ (.-innerHeight js/window) 2))

(def radius (/ (.-innerWidth js/window) 4.5))
(def circle-radius (/ (.-innerWidth js/window) 20))

(def status-colors {:ok "green"
                    :failed "red"
                    :pass "orange"
                    :init "blue"}) 

(def circle-line-height (str circle-radius "px"))


(def cur-letter-color "purple")
(def letter-color "black")
(def letter-width "1")

(def highlight-letter-color "orange")
(def highlight-letter-width "5")
  


(defn svg-letter-circle-component
  [x y pos letter]
  [:g {:transform (str "translate(" x "," y ")")}
   ^{:key pos} 
   [:circle {:id pos 
             :r circle-radius 
             :stroke (if (= pos (game/cur-letter-id)) 
                       highlight-letter-color
                       letter-color)
             :strokeWidth (if (= pos (game/cur-letter-id)) 
                             highlight-letter-width 
                             letter-width)
             :fill (get status-colors (game/get-letter-status pos)) }]
   [:Text {:dx "-5" :dy "5"} letter]])


(defn set-letter-color [pos]
  (if (= pos (game/cur-letter-id))
    cur-letter-color
    (get status-colors (game/get-letter-status pos))))


(defn letter-circle-component [x y pos letter]
  ^{:key pos} [:div {:id pos
                     :style {:position "absolute"
                             :top y
                             :left x
                             :width circle-radius
                             :height circle-radius
                             :border-radius "50%"
                             :font-size (/ circle-radius 2)
                             :color "#fff"
                             :line-height circle-line-height
                             :text-align "center"
                             :background (set-letter-color pos)}} 
               letter])



(defn build-circles []
  (let [get-circle (fn [x] {
                            :x (Math/round (+ center-x (* radius  (Math/cos (-  (/ (* Math/PI 2 x) game/num-letters) (/ Math/PI 2))))))
                            :y (Math/round (+ center-y (* radius  (Math/sin (-  (/ (* Math/PI 2 x) game/num-letters) (/ Math/PI 2))))))
                            :pos x
                            :letter (get game/letters x)})]
       (map get-circle (range game/num-letters))))




(defn start-dialog []
  (let [show-dialog (if (game/game-in-start?) "block" "none")]
    [:div {:style {:display show-dialog}}
     "start dialog"]))

(defn end-dialog []
  (let [show-dialog (if (game/game-ended?) "block" "none")]
    [:div {:style {:display show-dialog}}
     "end dialog"]))

(defn pause-dialog []
  (let [show-dialog (if (game/game-paused?) "block" "none")]
    [:div {:style {:display show-dialog}}
     "pause dialog"]))



(defn board-component []
  [:div
   [:div
    (for [circle (build-circles)]
      [letter-circle-component (:x circle) (:y circle) (:pos circle) (:letter circle)])]
   [start-dialog]
   [end-dialog]
   [pause-dialog]])


(defn svg-board-component []
  [:svg {:height "500" :width "500"}
   (for [circle (build-circles)]
     [svg-letter-circle-component (:x circle) (:y circle) (:pos circle) (:letter circle)])])


(defn format-time [t]
  (let [minutes (quot t 60)
        seconds (rem t 60)]
    (str
     (if (> 10 minutes) (str "0" minutes) minutes)
     ":"
     (if (> 10 seconds) (str "0" seconds) seconds))))

(defn home-page []
  [:div
   [:div {:style {:position "absolute"
                  :top (str (/  window-height 2) "px")
                  :left (str (/ window-width 2) "px")}} 
    [:div {:id "score"
           :style {:font-size "100px" :display "inline-block"}} 
     (game/get-score)]
    [:div {:id "time:" :style {:display "inline-block"}} (format-time (game/get-time))]]
   [board-component]]) 


(defn about-page []
  [:div [:h2 "About passa-paraula"]
   [:div [:a {:href "#/"} "go to the home page"]]])
