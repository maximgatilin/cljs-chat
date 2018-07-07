(ns cljs-chat.core
  (:require [rum.core :refer [defc] :as rum]
            [secretary.core :as secretary :include-macros true]
            [accountant.core :as accountant]
            [goog.events :as events]
            [goog.history.EventType :as HistoryEventType]

            [components.login :refer [login]]
            [components.chat :refer [chat]])
  (:import goog.History))

(defonce app-state (atom {:text "Hello world!"}))

(def currentPage (atom login))

(defc page < rum/reactive [] [((rum/react currentPage))])

;; -------------------------
;; Routes
(secretary/defroute "/" []
                    (do (js/console.log "/ route")
                        (reset! currentPage login)))

(secretary/defroute "/chat" []
                    (do (js/console.log "chat route")
                        (reset! currentPage chat)))

;; -------------------------
;; History
;; must be called after routes have been defined

(defn hook-browser-navigation! []
  (doto (History.)
    (events/listen
      HistoryEventType/NAVIGATE
      (fn [event]
        (secretary/dispatch! (.-token event))))
    (.setEnabled true)))

(hook-browser-navigation!)

;; -------------------------
;; Initialize app

(defc hello-world []
  [:div
   [:nav
    [:a {:href "/chat"} "Chat"]
    [:a {:href "/"} "Login"]
    ]
   (page)
   ])

(defn mount-root []
  (rum/mount (hello-world)
             (. js/document (getElementById "app"))))

(defn init! []
  (accountant/configure-navigation!
    {:nav-handler
     (fn [path]
       (secretary/dispatch! path))
     :path-exists?
     (fn [path]
       (secretary/locate-route path))})
  (accountant/dispatch-current!)
  (mount-root))

(init!)

(defn on-js-reload []
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  ;; (swap! app-state update-in [:__figwheel_counter] inc)
)
