(ns com.mine-sweeper.main
  (:require [com.mine-sweeper.logic :as logic]
            [com.mine-sweeper.model.cell :as cell]))


#_(defn draw! [^Terminal t {:mine-field/keys [width height grid] :as initial-state}]
  (let [[cx cy] (:cursor-position initial-state)]
    (doseq [y (range height)]
      (doseq [x (range width)]
        (terminal/put-character t x y (first (cell/cell-character (get-in grid [x y]))))))
    (terminal/move-cursor t cx cy)
    (terminal/flush! t)))

#_(defn display-game-over-message [^Terminal t w h]
  (terminal/put-string t w h "Game Over! Press y to start again or x to exit"))

(defn game-loop [app-state k]
  (while (not (logic/game-over? @app-state))
    (when-let [cmd (logic/get-command @app-state k)]
      (swap! app-state cmd)
      #_(draw! t @app-state))))

(defn ask-play-again? [^Terminal t w h]
  (display-game-over-message t w h)
  (loop []
    (when-let [k (terminal/get-next-key-press t)]
      (if-let [result (logic/user-input->command k)]
        result
        (recur)))))

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

(defn play! [w h m]
  (loop []
    (let [[t app-state] (initialize-game w h m)]
      (draw! t @app-state)
      (game-loop t app-state)
      (let [decision (ask-play-again? t w h)]
        (case decision
          :exit (System/exit 0)
          (recur))))))

(comment
  (def game
    (future
      (play! 20 10 20))))
