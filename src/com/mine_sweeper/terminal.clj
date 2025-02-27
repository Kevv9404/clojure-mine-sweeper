(ns com.mine-sweeper.terminal
  (:import (java.nio.charset StandardCharsets)
           (jdk.internal.org.line.terminal Terminal)
           (com.googlecode.lanterna.terminal DefaultTerminalFactory Terminal)))


(defn terminal []
  (.createTerminal (new DefaultTerminalFactory System/out, System/in, StandardCharsets/UTF_8)))

(defn init-terminal [^Terminal t]
  (.enterPrivateMode t)
  (.clearScreen t))

(defn move-cursor [^Terminal t x y]
  (.setCursorPosition t x y)
  (.flush t))

(defn put-character [^Terminal t x y ^Character c]
  (.setCursorPosition t x y)
  (.putCharacter t c)
  (.flush t))

(defn get-next-key-press [^Terminal t]
  (let [keyStroke (.pollInput t)]
    (when keyStroke
      (.getCharacter keyStroke))))

comment
((def t (terminal)))