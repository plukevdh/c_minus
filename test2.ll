(FUNCTION  gcd  [(int u) (int v)]
  (BB 0
    (OPER 1 Func_Entry []  [])
  )
  (BB 1
  )
  (BB 6
    (OPER 4 EQ [(r 3)]  [(r 2)(i 0)])
    (OPER 5 BNE []  [(r 3)(i 0)(bb 4)])
  )
  (BB 3
    (OPER 6 Mov [(m RetReg)]  [(r 1)])
  )
  (BB 2
    (OPER 2 Func_Exit []  [])
    (OPER 3 Return []  [(m RetReg)])
  )
  (BB 4
    (OPER 8 Div_I [(r 4)]  [(r 1)(r 2)])
    (OPER 9 Mul_I [(r 5)]  [(r 4)(r 2)])
    (OPER 10 Sub_I [(r 6)]  [(r 1)(r 5)])
    (OPER 7 Pass []  [(r 6)])
    (OPER 11 Pass []  [(r 2)])
    (OPER 12 JSR []  [(s gcd)])
    (OPER 13 Load [(r 7)]  [(m RetReg)])
    (OPER 14 Mov [(m RetReg)]  [(r 7)])
    (OPER 15 Jmp []  [(bb 5)])
  )
  (BB 5
  )
)
(FUNCTION  main  []
  (BB 0
    (OPER 1 Func_Entry []  [])
  )
  (BB 1
    (OPER 4 JSR []  [(s input)])
    (OPER 5 Load [(r 3)]  [(m RetReg)])
    (OPER 6 Mov [(r 1)]  [(r 3)])
    (OPER 7 JSR []  [(s input)])
    (OPER 8 Load [(r 4)]  [(m RetReg)])
    (OPER 9 Mov [(r 2)]  [(r 4)])
    (OPER 11 Pass []  [(r 2)])
    (OPER 12 Pass []  [(r 1)])
    (OPER 13 JSR []  [(s gcd)])
    (OPER 14 Load [(r 5)]  [(m RetReg)])
    (OPER 10 Pass []  [(r 5)])
    (OPER 15 JSR []  [(s output)])
    (OPER 16 Load [(r 6)]  [(m RetReg)])
  )
  (BB 2
    (OPER 2 Func_Exit []  [])
    (OPER 3 Return []  [(m RetReg)])
  )
)
