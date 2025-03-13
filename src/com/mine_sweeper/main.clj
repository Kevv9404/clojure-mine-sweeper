(ns com.mine-sweeper.main
  (:require [com.mine-sweeper.logic :as logic]
            [com.mine-sweeper.model.cell :as cell]
            [com.mine-sweeper.terminal :as terminal])
  (:import (jdk.internal.org.jline.terminal Terminal)))

(defn display-game-over-message [^Terminal t w h]
  (terminal/put-string t w h "Game Over! Press y to start again"))

(defn draw! [^Terminal t {:mine-field/keys [width height grid] :as mine-field}]
  (terminal/clear! t)
  (when (logic/game-over? mine-field)
    (display-game-over-message t width height))
  (let [[cx cy] (:cursor-position mine-field)]
    (doseq [y (range height)]
      (doseq [x (range width)]
        (terminal/put-character t x y (first (cell/cell-character (get-in grid [x y]))))))
    (terminal/move-cursor t cx cy)
    (terminal/flush! t)))

(defn game-loop [^Terminal t app-state]
  (draw! t @app-state)
  (while true
    (let [k (terminal/get-next-key-press t)]
      (swap! app-state logic/game-step k)
      (draw! t @app-state))))

(defn initialize-game [w h m]
  (let [app-state (atom (logic/setup w h m))
        t         (terminal/terminal)]
    (terminal/init-terminal t)
    [t app-state]))

;; Fulcro ideas:
;; 1. Improve network interactions (LATER)
;; 2. Create applications that work THIS way (global state -> MUTATIONS -> new global state). Render each change.
;;    * Reason about the majority of the application as PURE data
;; 3. Client-side normalized database AS the representation of "global state"
;; 4. MOST of the code in the Fulcro libraries are just that: code YOU could write, but I needed it too, so why not give it to you.
;;    * UI Routing - has some implications for Fulcro, but no "one" implementation
;;    * RAD - patterns I discovered and shared for rapid dev
;;

(defn play! [w h m]
  (let [[t app-state] (initialize-game w h m)]
    (game-loop t app-state)))

(comment
  (future-cancel game)
  (def game
    (future
      (play! 20 10 20))))
