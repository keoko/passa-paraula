(ns passa-paraula.ui.views
  (:require [passa-paraula.game :as game]
            [reagent.core :as reagent :refer [atom]]
            [reagent.session :as session]
            [secretary.core :as secretary :include-macros true]
            [accountant.core :as accountant]
            [reagent-forms.core :refer [bind-fields init-field value-of]]))


(def ui-state (atom {}))

(def status-colors {:ok "green"
                    :failed "red"
                    :pass "orange"
                    :init "black"}) 


(def cur-letter-color "purple")
(def letter-color "blue")
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
                            :x (* (:radius @ui-state)  (Math/cos (-  (/ (* Math/PI 2 x) (game/num-letters)) (/ Math/PI 2))))
                            :y  (* (:radius @ui-state)  (Math/sin (-  (/ (* Math/PI 2 x) (game/num-letters)) (/ Math/PI 2))))
                            :pos x
                            :letter (get (game/get-letters) x)})]
    (map get-circle (range (game/num-letters)))))



(defn board-component []
  [:div.container
   [:div.row
    (for [circle (build-circles)]
      [letter-circle-component (:x circle) (:y circle) (:pos circle) (:letter circle)])]])


(defn format-time [t]
  (let [minutes (quot t 60)
        seconds (rem t 60)]
    (str
     (if (> 10 minutes) (str "0" minutes) minutes)
     ":"
     (if (> 10 seconds) (str "0" seconds) seconds))))


(defn start-component []
  [:span.glyphicon.glyphicon-play-circle 
   {:aria-hidden "true"
    :on-click game/toggle-status}])

(defn end-component []
  [:span.glyphicon.glyphicon-repeat 
   {:aria-hidden "true"
    :on-click game/toggle-status}])

(defn pause-component []
  [:span.glyphicon.glyphicon-pause 
   {:aria-hidden "true"
    :on-click game/toggle-status}])


(defn run-component []
  [:div
   [:span.glyphicon.glyphicon-ok
    {:aria-hidden "true"
     :style {:color (:ok status-colors)}
     :on-click (fn [_] (game/handle-letter (game/cur-letter-id) :ok))}]
   [:span.glyphicon.glyphicon-remove
    {:aria-hidden "true"
     :style {:color (:failed status-colors)}
     :on-click (fn [_] (game/handle-letter (game/cur-letter-id) :failed))}]
   [:span.glyphicon.glyphicon-question-sign 
    {:aria-hidden "true"
     :style {:color (:pass status-colors)}
     :on-click (fn [_] (game/handle-letter (game/cur-letter-id) :pass))}]])

(defn buttons-component []
  (let [buttons-by-state {:start (start-component)
                  :end (end-component)
                  :pause (pause-component)
                  :run (run-component)}
        buttons (get buttons-by-state (game/get-state))
        left-factor-by-state {:start 1
                              :end 1
                              :pause 1
                              :run 5}
        div-top  (- (:center-y @ui-state) (/ (:circle-radius @ui-state) 2))
        div-left (- (:center-x @ui-state) (* (get left-factor-by-state (game/get-state)) (/(:circle-radius @ui-state) 2)))
]
    [:div 
     {:id "buttons" 
      :style {:position "absolute"
              :top (str div-top "px")
              :left (str div-left "px")
              :width (str (:radius ui-state) "px")
              :text-align "center"
              :color (:init status-colors)
              :font-size (str (* 2 (:circle-radius @ui-state)))}} buttons]))

(defn score-component []
  [:span.navbar-text.pull-left {:id "score"} 
    " score " [:span.badge (game/get-score)]])


(defn timer-component []
  [:span.navbar-text.pull-right {:id "time"} 
   "timer " [:span.badge (format-time (game/get-time))]])

(defn preferences-button-component []
  [:div
   [:button {:on-click #(secretary/dispatch! "/preferences")} "preferences"]]

)

(defn navbar-component []
  [:nav.navbar.navbar-fixed-top.navbar-inverse {:style {:background-color (game/get-team-color)}}
;;    [:a.navbar-brand {:style {:color "white"}} (game/get-team-name)]    
    [:div.navbar-header
     [:div (game/get-team-name)]
     [score-component]
     [timer-component]
     [preferences-button-component]]])



(defn home-page []
  [:div 
   [navbar-component]
   [buttons-component]
   [board-component]
   ])

(defn set-preferences [x]
  (.log js/console "test"))

(defn row [label input]
  [:div.row
   [:div.col-md-2 [:label label]]
   [:div.col-md-5 input]])

(defn input [label type id]
  (row label [:input.form-control {:field type :id id}]))

(defn convert-letters [[id] value {:keys [letters] :as state}]
  (when (= id :letters)
    (assoc state :letters (clojure.string/split value #","))))

(def form-template
  [:div
   (input "team color" :text :team-color)
   (input "team name" :text :team-name)
   (input "time (seconds)" :text :time)
   (input "letters" :text :letters)])

(defn preferences-page []
  [:div
   [:div.page-header [:h1 "Preferences"]]
   [bind-fields form-template (game/get-app-state) convert-letters]
   [:button.btn.btn-default
    {:on-click #(secretary/dispatch! "/")}
    "save"]])


(defn current-page []
  [:div [(session/get :current-page)]])


;; -------------------------
;; Routes

(secretary/defroute "/" []
  (session/put! :current-page #'home-page))

(secretary/defroute "/preferences" []
  (session/put! :current-page #'preferences-page))

(defn recalculate-window-center! []
  (let [width  (.-innerWidth js/window)
        height (.-innerHeight js/window)]
    (swap! ui-state merge {:center-x (/ width 2)
                           :center-y (/ height 2)
                           :circle-radius (/ width 20)
                           :radius (/ width 4.5)})))

(defn mount-root []
  (reagent/render [current-page] (.getElementById js/document "app")))

(defn init! []
  (accountant/configure-navigation!)
  (accountant/dispatch-current!)
  (mount-root))
