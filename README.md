== Statically Typed Pure Functinal Strict Languages IMO
* IMO is not practical
* IMO is abbreviation of IoMOnad
* Statically Typed
* Pure Functional
* Strict Evaluation (or Eager Evaluation)
  * I dislike lazy languages such as Haskell
* Data Type
  * IO (parameterrized): io('a)
    * like Haskell's IO monad
  * Function (parameterrized): 'a -> 'b
  * String: string
  * Integer: int
  * Boolean: bool
  * Unit: unit
* No Type Inference
  * For simplifying implementation
* Grammar
  * Comment: //Line Comment
  * No Statement.  IMO program is consisted of variable binding and expression.
  * Function Definition:
```
    def var_name(arg : arg_type) : ret_type = body
    Example:
      def foo(x :int) :int = x
```
  * Expression
    * x op y //binary operator
      Example: x + y
    * op x //unary operator
      Example: not true
    * f x //function application
      println "foo"
    * let var_name = exp in body
      let x = 1 in x
* Execution Model
  * An Execution starts from main.  The type of main is assumed
	  to io('a), where 'a is arbitrary type.
* Builtin Operators Constructing IO Objects.
```
  x >>= y :: io('a) -> (a -> io('b)) -> io('b)
  x >> y :: io('a) -> io('b) -> io('b)
```
* Function Application
  f x
* Binary Operator
```
  x and y :: bool -> bool -> bool
  x or y :: bool -> bool -> bool
  x + y :: int -> int -> int
  x * y :: int -> int -> int
  x / y :: int -> int -> int
  x % y :: int -> int -> int
  x / y :: int -> int -> int
  x < y :: int -> int -> bool
  x > y :: int -> int -> bool
  x <= y :: int -> int -> bool
  x >= y :: int -> int -> bool
```
* Unary Operator
```
  not x :: bool -> bool
  + x :: int -> int
  - x :: int -> int  
```
* Builtin Function
```
  parse_int :: string -> int
  print :: string -> io(unit)
  println :: string -> io(unit)
  print_number :: int -> io(unit)
  read_line :: int -> io(unit)
```
