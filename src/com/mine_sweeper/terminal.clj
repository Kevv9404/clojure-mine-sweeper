(ns com.mine-sweeper.terminal
  (:import (java.nio.charset StandardCharsets)
           (com.googlecode.lanterna.terminal DefaultTerminalFactory Terminal)))

(defn terminal []
  (.createTerminal (new DefaultTerminalFactory System/out, System/in, StandardCharsets/UTF_8)))

(defn init-terminal [^Terminal t]
  (.enterPrivateMode t)
  (.clearScreen t))

(defn move-cursor [^Terminal t x y]
  (.setCursorPosition t x y))

(defn put-character [^Terminal t x y ^Character c]
  (.setCursorPosition t x y)
  (.putCharacter t c)
  )

(defn get-next-key-press [^Terminal t]
  (let [keyStroke (.readInput t)]
    (when keyStroke
      (.getCharacter keyStroke))))

(defn put-string [^Terminal t h w ^String s]
  (.setCursorPosition t (/ w 2) (/ h 2))
  (.putString t s)
  (.flush t))

(defn exit-terminal [^Terminal t]
  (.close t))

(defn flush! [^Terminal t]
  (.flush t))
