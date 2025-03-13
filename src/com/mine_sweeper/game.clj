(ns com.mine-sweeper.game
  (:require
    [com.mine-sweeper.model.grid :as grid]
    [com.mine-sweeper.model.field :as field]
    ))

(def game-state (atom (field/mine-field 10 10)))


(while (not (game-over @game-state))
  (let [user-input (poll-for-input)]
    (swap! game-state update-game user-input) ; how is this thread safe???
    (render! @game-state)
    (Thread/sleep 100)))
