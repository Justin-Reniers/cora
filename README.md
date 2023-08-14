# Rewriting Induction
To use this tool for rewriting induction, it is of importance to load in
an LCTRS first. The LCTRS format used in this tool follows the general 
[MSTRS format](http://project-coco.uibk.ac.at/problems/mstrs.php) used for problems in the 
[Confluence Problems database](https://cops.uibk.ac.at/), and looks as follows:

```
      trs           ::= (SIG funlist) (RULES rulelist)
      funlist       ::= fun | fun funlist
      fun           ::= (id sort)
      sort          ::= idlist -> id
      idlist        ::= ε | id idlist
      rulelist      ::= ε | rule rulelist
      rule          ::= term -> term constraint
      constraint    ::= [term]
      term          ::= id | id() | id(termlist)
      termlist      ::= term | term, termlist
```

An example of this format looks like the following, and comes from [Fuhs, Nishida, Kop](https://arxiv.org/pdf/1409.0166.pdf):

```
    (SIG
        (factiter   Int -> Int)
        (iter       Int Int Int -> Int)
        (return     Int -> Int)
        (factrec    Int -> Int)
        (mul        Int Int -> Int)
        (f          Int -> Int)
    )
    (RULES
        factiter(x) -> iter(x, 1, 1)
        iter(x, z, i) -> iter(x, z*i, i+1)  [i <= x]
        iter(x, z, i) -> return(z)          [i > x]
        factrec(x) -> return(1)             [x <= 1]
        factrec(x) -> mul(x, factrec(x-1))  [x > 1]
        mul(x, return(y)) -> return(x*y)
    )
```

It is important to note here that the basic types of integers and booleans are 
always included in the theory for an LCTRS in this tool, as well as basic
operations on them. This means that there is also an implicit requirement that 
a constraint is of the boolean type.

The included basic operations (as functions) included in the theory are all used
as infix, and are the following functions:

```math
    Unary math operators        :   -
    Unary boolean operators     :   ~
    Binary integer operators    :   -, +, *, /, %, 
    Binary comparison operators :   <, <=, >, >=, !=, ==
    binary boolean operators    :   /\, \/, -->, <-->
```

When using the tool, after loading a `.lctrs` file with an LCTRS
adhering to the aforementioned format, you can enter an initial
proof state by pressing the `proof` button with the following format:

```math
    term term [term]
```

The last term is of course the initial constraint of the proof, and
thus has to adhere to the implicit requirement that its final type
is boolean.

## Rewriting Induction Rules
Currently, there are eight out of nine supported rewriting 
rules that are introduced in the paper by [Fuhs, Nishida, Kop](https://arxiv.org/pdf/1409.0166.pdf).
These rules will be outlined below together with the expected
input format, as well as some additional rules to help the user.

### Simplification
The simplification rule follows a simple format:

```math
    simplify q i
    with q a position, 0 denoting the empty position,
         0.1 denoting the first subterm of a term etc.
         i \ge 1, the index of the rewriting rule to be applied
```

The simplification rule can also be applied to rewrite calculation
rules, such as $1+1$ to $2$, or replace subterms such as $x + 1$ with 
a new fresh variable (e.g. y) and add the equation $y = x + 1$ to the
constraint.

### Expansion
The expansion rule follows a similar format to simplification:

```math
    expand q
    with q a position, 0 denoting the empty position,
         0.1 denoting the first subterm of a term etc.
```

### Deletion
The application of the deletion rule follows a very simple format:

```
    delete
```

### Postulate
The postulate rule allows you to introduce a new rule at the cost of completeness,
and takes three arguments: the left-hand side of the rule, the right-hand
side of the rule, and the constraint. The format of the postulate rule
is thus as follows;


```
    postulate s t [c]
    with s, t terms
         c a logical term
```

### Generalization
Generalization is currently not supported yet.

### EQ-Deletion
EQ-deletion is applied similarly to delete, where the format is 
simply:

```
    eq-delete
```

### Constructor
Yet again, the format of constructor is simply the rule name:

```
    constructor
```

### Disprove
For disprove, the same holds and the format is as follows:

```
    disprove
```

### Completeness
The same also currently holds for the completeness rule:

```
    completeness
```

## Extra rules
As said before, the tool contains some extra rules to help the 
user navigate. 

### Swap
Swap is introduced for two reasons: first off, the commands are
generally always applied to the top-left term shown in the tool
(except for when they are applied to the entire equation, which 
will always be the equation on top), but the user might want to
work on another equation or need to apply the rule to the right-hand
side of the proof. That is what the swap rule is for. The format
is as follows:

```
    swap (x) (y)
    with (x), (y) optional arguments denoting the index of equations
```

If the swap rule is applied without arguments, the left-hand side
and right-hand side of the current equation are swapped in the tool.
If swap is supplied with two valid indices of existing equations,
then these equations swap places in the tool, allowing the user to 
work on the necessary equation.

### Undo
In case you made want to go back steps, the tool has the undo
command, which moves the proof back a single state, meaning to
the state before the last command. Multiple applications of undo
can be chained in case you want to move back multiple steps. The
application of undo is as follows:

```
    undo
```

### Rewrite
In case the user wants to remove some redundancy from the proof constraint,
this can be done. The application of rewrite is as follows:

```
    rewrite [x] [y]
    where x is a collection of constraint components separated by /\,
    and y is the collection of constraint components with which x should 
    be replaced, separated by /\
```


### Rename
In case the user wants to rename a variable, that works as follows:

```
    rename s t
    with s a variable existing in the current equation of the proof
         t a fresh variable name that does not exist in the current
         equation of the proof yet
```

### Up and Down arrow keys
While not a separate command, the up and down arrow keys can be used to cycle
through the previously applied user inputs. This can be useful in combination 
with undo, since there is no redo.