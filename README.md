# Klisp -- a small lisp-y interpreter written in Kotlin.

Klisp is based on lis.py (see http://norvig.com/lispy.html).

The project is written for educational purposes and is not intended to be used in production environments.

## Running Klisp
Build and run REPL (requires Java and Gradle in `$PATH`)
```
gradle build && java -jar ./build/libs/klisp.jar
```
Run a file
```
java -jar ./build/libs/klisp.jar [file]
```
## Example programs

Factorial
```
(begin (define fact 
            (lambda (n) 
                (if (< n 2) 
                    1 
                    (* n (fact (- n 1)))))) 
        (fact 4))
```

Lists and quote
```
(cdr (quote (rose mary juan)))
```
