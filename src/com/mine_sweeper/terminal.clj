(ns com.mine-sweeper.terminal
  (:import (com.googlecode.lanterna.terminal DefaultTerminalFactory Terminal)
           (java.nio.charset StandardCharsets)))


(defn- init-terminal [^Terminal t]
  (.enterPrivateMode t)
  (.clearScreen t))

(defn terminal []
  (let [t (.createTerminal (new DefaultTerminalFactory System/out, System/in, StandardCharsets/UTF_8))]
    (init-terminal t)
    t))

(defn move-cursor!
  "Move the cursor. Requires a `flush!`."
  [^Terminal t x y] (.setCursorPosition t x y))

(defn put-character!
  "Put the first character of object `c` at x,y. Requires a flush."
  [^Terminal t x y c]
  (.setCursorPosition t x y)
  (.putCharacter t (first (str c))))

(defn poll-for-input
  "Returns a string version of the key pressed, or nil if no key is available."
  ^String [^Terminal t]
  (let [keyStroke (.pollInput t)]
    (when keyStroke
      (str (.getCharacter keyStroke)))))

(defn next-input
  "BLOCKS until the user presses a key, then returns a string version of the key pressed (or possibly nil)."
  ^String [^Terminal t]
  (let [keyStroke (.readInput t)]
    (when keyStroke
      (str (.getCharacter keyStroke)))))

(defn flush! [^Terminal t]
  (.flush t))
