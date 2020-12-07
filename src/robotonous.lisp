;;; robotonous.lisp

(in-package :robotonous)

(defmacro with-robot ((robot robot-form) &body body)
  (if (not (symbolp robot))
      (error 'invalid-state-error :input "ROBOT must be a symbol"))
  (loop
     :as form :in body
     :if (atom form) :do 'ignored
     :else :collect
       (destructuring-bind (cmd &rest args) form
         (if (keywordp cmd)
             `(robotonous-top-level ,robot ,cmd .,args)
             form)) :into cmds
     :finally (return `(let ((,robot ,robot-form))
                         (macrolet ((robo (&body body)
                                      `(with-robot (,',robot ,',robot)
                                         ,@body)))
                           (progn .,cmds))))))

(defun robotonous-top-level (robot command &rest arguments)
  (case command
    (:type          (apply #'type          robot arguments))
    (:mousemove     (apply #'mouse-move    robot arguments))
    (:mousepress    (apply #'mouse-press   robot arguments))
    (:mouserelease  (apply #'mouse-release robot arguments))
    (:mouseclick    (apply #'mouse-click   robot arguments))
    (:delay         (apply #'delay         robot arguments))
    (t (error 'invalid-state-error :input command))))

(defun delay (robot time)
  (jcall "delay" robot time))

;;; java event classes
(defvar *input-event-class* (jclass "java.awt.event.InputEvent"))
(defvar *key-event-class*   (jclass "java.awt.event.KeyEvent"))
(defvar *mouse-event-class* (jclass "java.awt.event.MouseEvent"))
(defvar *mouse-info-class*  (jclass "java.awt.MouseInfo"))

;;; Mouse actions
(defun mouse-move (robot x y &optional (relative 'nil))
  (if relative
      (destructuring-bind (dx dy) (mouse-position)
        (jcall "mouseMove" robot (+ x dx) (+ y dy)))
      (jcall "mouseMove" robot x y)))

(defun mouse-position ()
  (let* ((mouse-ptr (jstatic "getPointerInfo" *mouse-info-class*))
         (point (jcall "getLocation" mouse-ptr)))
    (list (jfield "x" point) (jfield "y" point))))

(defun mouse-click (robot button)
  (mouse-press   robot button)
  (mouse-release robot button))

(defun mouse-press (robot button)
  (let ((button (_mouse-button button)))
    (jcall "mousePress" robot button)))

(defun mouse-release (robot button)
  (let ((button (_mouse-button button)))
    (jcall "mouseRelease" robot button)))

(defun _mouse-button (button)
  "Maps from a button representation, example :left, to the
corresponding Robot value."
  (let ((button (case button
                  (:left   (jfield *mouse-event-class* "BUTTON1"))
                  (:middle (jfield *mouse-event-class* "BUTTON2"))
                  (:right  (jfield *mouse-event-class* "BUTTON3"))
                  (t (error 'invalid-state-error :input button)))))
    (jstatic "getMaskForButton" *input-event-class* button)))

;;; Keyboard actions
(defparameter *keyboard-keys*
  (let ((keyboard-keys (make-hash-table
                        :size 150 :rehash-size 2.0 :rehash-threshold 0.99))
        (shift 16))
    ;; control keys
    (let ((char-keycodes '((:shift (16 ))
                           (:ctrl  (17 ))
                           (:alt   (18 ))
                           (:meta  (157)))))
      (loop
         :as (char keycodes) in char-keycodes
         :do (setf (gethash char keyboard-keys) keycodes)))
    ;; alphanumerics
    (loop
       :as i :from 1 :to 127
       :as c := (code-char i)
       :if (lower-case-p c)
       :do (setf (gethash c keyboard-keys) (list (char-code (char-upcase c))))
       :if (upper-case-p c)
       :do (setf (gethash c keyboard-keys) (list shift i))
       :if (digit-char-p c)
       :do (setf (gethash c keyboard-keys) (list i)))
    ;; other printable keys
    (let ((chars `((#\- (       ,(jfield *Key-Event-Class* "VK_MINUS")))
                   (#\_ (,shift ,(jfield *Key-Event-Class* "VK_MINUS")))
                   (#\= (       ,(jfield *Key-Event-Class* "VK_EQUALS")))
                   (#\+ (,shift ,(jfield *Key-Event-Class* "VK_EQUALS")))
                   (#\` (       ,(jfield *Key-Event-Class* "VK_BACK_QUOTE")))
                   (#\~ (,shift ,(jfield *Key-Event-Class* "VK_BACK_QUOTE")))
                   (#\, (       ,(jfield *Key-Event-Class* "VK_COMMA")))
                   (#\< (,shift ,(jfield *Key-Event-Class* "VK_COMMA")))
                   (#\. (       ,(jfield *Key-Event-Class* "VK_PERIOD")))
                   (#\> (,shift ,(jfield *Key-Event-Class* "VK_PERIOD")))
                   (#\/ (       ,(jfield *Key-Event-Class* "VK_SLASH")))
                   (#\? (,shift ,(jfield *Key-Event-Class* "VK_SLASH")))
                   (#\; (       ,(jfield *Key-Event-Class* "VK_SEMICOLON")))
                   (#\: (,shift ,(jfield *Key-Event-Class* "VK_SEMICOLON")))
                   (#\' (       ,(jfield *Key-Event-Class* "VK_QUOTE")))
                   (#\" (,shift ,(jfield *Key-Event-Class* "VK_QUOTE")))
                   (:dblquote (,shift ,(jfield *Key-Event-Class* "VK_QUOTE")))
                   (#\[ (       ,(jfield *Key-Event-Class* "VK_OPEN_BRACKET")))
                   (#\{ (,shift ,(jfield *Key-Event-Class* "VK_OPEN_BRACKET")))
                   (#\] (       ,(jfield *Key-Event-Class* "VK_CLOSE_BRACKET")))
                   (#\} (,shift ,(jfield *Key-Event-Class* "VK_CLOSE_BRACKET")))
                   (#\\ (       ,(jfield *Key-Event-Class* "VK_BACK_SLASH")))
                   (#\| (,shift ,(jfield *Key-Event-Class* "VK_BACK_SLASH")))
                   (#\! (,shift ,(jfield *Key-Event-Class* "VK_1")))
                   (#\@ (,shift ,(jfield *Key-Event-Class* "VK_2")))
                   (#\# (,shift ,(jfield *Key-Event-Class* "VK_3")))
                   (#\$ (,shift ,(jfield *Key-Event-Class* "VK_4")))
                   (#\% (,shift ,(jfield *Key-Event-Class* "VK_5")))
                   (#\^ (,shift ,(jfield *Key-Event-Class* "VK_6")))
                   (#\& (,shift ,(jfield *Key-Event-Class* "VK_7")))
                   (#\* (,shift ,(jfield *Key-Event-Class* "VK_8")))
                   (#\( (,shift ,(jfield *Key-Event-Class* "VK_9")))
                   (#\) (,shift ,(jfield *Key-Event-Class* "VK_0"))))))
      (loop
         :as (char keycodes) :in chars
         :do (setf (gethash char keyboard-keys) keycodes)))
    ;; other keys
    (let ((chars `((#\Newline  (,(jfield *Key-Event-Class* "VK_ENTER")))
                   (#\Tab      (,(jfield *Key-Event-Class* "VK_TAB")))
                   (:tab       (,(jfield *Key-Event-Class* "VK_TAB")))
                   (#\Space    (,(jfield *Key-Event-Class* "VK_SPACE")))
                   (:space     (,(jfield *Key-Event-Class* "VK_SPACE")))
                   (:escape    (,(jfield *Key-Event-Class* "VK_ESCAPE")))
                   (:backspace (,(jfield *Key-Event-Class* "VK_BACK_SPACE")))
                   (:pageup    (,(jfield *Key-Event-Class* "VK_PAGE_UP")))
                   (:pagedown  (,(jfield *Key-Event-Class* "VK_PAGE_DOWN")))
                   (:end       (,(jfield *Key-Event-Class* "VK_END")))
                   (:home      (,(jfield *Key-Event-Class* "VK_HOME")))
                   (:left      (,(jfield *Key-Event-Class* "VK_LEFT")))
                   (:up        (,(jfield *Key-Event-Class* "VK_UP")))
                   (:right     (,(jfield *Key-Event-Class* "VK_RIGHT")))
                   (:down      (,(jfield *Key-Event-Class* "VK_DOWN")))
                   (:delete    (,(jfield *Key-Event-Class* "VK_DELETE"))))))
      (loop
         :as (char keycodes) :in chars
         :do (setf (gethash char keyboard-keys) keycodes)))
    keyboard-keys)
  "Hashtable with all the keys")

;;; (:type "a B" :space "c" (:ctrl (:mouseclick :left)))
;;; (:type "a B" :space "c" (:ctrl (:shift      :left)))

(defun type (robot &rest args)
  (mapc (lambda (arg) (_type robot arg)) args))

(defgeneric _type (robot value))

(defmethod _type ((robot (jclass "java.awt.Robot")) (value string))
  (loop
     :as char :across value
     :as keycodes := (gethash char *keyboard-keys*)
     :if keycodes
     :do (_type-keycodes robot keycodes)))

(defmethod _type ((robot (jclass "java.awt.Robot")) (value symbol))
  (let ((keycodes (gethash value *keyboard-keys*)))
    (if keycodes
        (_type-keycodes robot keycodes)
        (type robot (format nil "~a" value)))))

(defmethod _type ((robot (jclass "java.awt.Robot")) (value cons))
  (destructuring-bind (cmd &rest args) value
    (if (not (keywordp cmd))
        (error 'invalid-state-error :input cmd))
    (let ((keycodes (gethash cmd *keyboard-keys*)))
      (if keycodes
          (progn
            (_keypress   robot keycodes)
            (apply #'type robot args)
            (_keyrelease robot keycodes))
          (apply #'robotonous-top-level robot cmd args)))))

(defmethod _type ((robot (jclass "java.awt.Robot")) (value t))
  (_type robot (format nil "~a" value)))

(defun _type-keycodes (robot keycodes)
  (_keypress   robot keycodes)
  (delay robot 1)
  (_keyrelease robot keycodes))

(defun _keypress (robot keycodes)
  (loop :for keycode :in keycodes
     :do (jcall "keyPress" robot keycode)))

(defun _keyrelease (robot keycodes)
  (loop :for keycode :in (reverse keycodes)
     :do (jcall "keyRelease" robot keycode)))

;;; robotonous.lisp ends here
