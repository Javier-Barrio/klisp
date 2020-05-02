#Klisp -- a small lisp-y interpreter written in Kotlin.

Klisp is based on lis.py (see http://norvig.com/lispy.html).

This is a project written for educational purposes and is not intended to be used in production environments.

##Example programs

Factorial
```
(begin (define fact 
            (lambda (n) 
                (if (< n 2) 
                    1 
                    (* n (fact (- n 1)))))) 
        (fact 4))
```