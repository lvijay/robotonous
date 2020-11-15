;;; robotonous.lisp

(in-package :robotonous)

(defmacro with-robot (&body body)
  )

;; (:type a "E" ($ctrl ($alt ^)))
;;     ->
;; (progn
;;   (progn
;;     (jcall "keyPress"   robot #\e)
;;     (jcall "keyRelease" robot #\e))
;;   (progn
;;     (jcall "keyPress"   robot 16)       ; 16=shift key
;;     (jcall "keyPress"   robot #\e)
;;     (jcall "keyRelease" robot #\e)
;;     (jcall "keyRelease" robot 16))
;;   (progn
;;     (jcall "keyPress"   robot 17)       ; 17=ctrl key
;;     (jcall "keyPress"   robot 18)       ; 18=alt key
;;     (jcall "keyPress"   robot 16)
;;     (jcall "keyPress"   robot #\6)
;;     (jcall "keyRelease" robot #\6)
;;     (jcall "keyRelease" robot 16)
;;     (jcall "keyRelease" robot 18)
;;     (jcall "keyRelease" robot 17)))

(defun handle-command (state command &rest args)
  (loop :with cmdfn := (command-function state command)
     :as arg :in args
     :appending (funcall cmdfn arg)))

;;; robotonous.lisp ends here
