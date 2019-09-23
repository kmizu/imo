# Statically Typed Pure Functinal Strict Languages IMO

* IMO is abbreviation of IoMOnad
* Statically-typed
* Purely functional
* Strict evaluation
* Data type
  * IO (parameterrized): io('a)
    * like Haskell's IO monad
  * Function: 'a -> 'b
  * String: string
  * Integer: int
  * Boolean: bool
  * Unit: unit
* No Type Inference
  * For simplifying implementation
* Comment: //Line Comment
* Function Definition:

```
    def var_name(arg : arg_type) : ret_type = body
    Example:
      def foo(x :int) :int = x
```

* Expressions
  * `x op y` //binary operator
    e.g. `x + y`
  * `op x //unary operator``
    e.g. `not true`
  * `f x //function application`
    e.g. `println "foo"`
  * `let var_name = exp in body`
    `let x = 1 in x`
* Execution
  * An IMO program starts from the `main` function.  The type of main is assumed to io('a), where 'a is an arbitrary type.
* Builtin-operators for IO

```
  x >>= y :: io('a) -> (a -> io('b)) -> io('b)
  x >> y :: io('a) -> io('b) -> io('b)
```

* Function applications

```
  f x
```

* Binary operators

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

* Unary operators

```
  not x :: bool -> bool
  + x :: int -> int
  - x :: int -> int  
```

* Builtin-functions

```
  parse_int :: string -> int
  print :: string -> io(unit)
  println :: string -> io(unit)
  print_number :: int -> io(unit)
  read_line :: int -> io(unit)
```
