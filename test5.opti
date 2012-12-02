(DATA  a)
(FUNCTION  addThem  [(int d) (int e)]
  (BB 0
    (OPER 1 Func_Entry []  [])
  )
  (BB 1
    (OPER 4 Add_I [(r 4)]  [(r 1)(r 2)])
    (OPER 5 Mov [(r 3)]  [(r 4)])
    (OPER 6 Mov [(m RetReg)]  [(r 3)])
  )
  (BB 2
    (OPER 2 Func_Exit []  [])
    (OPER 3 Return []  [(m RetReg)])
  )
)
(FUNCTION  main  []
  (BB 0
    (OPER 1 Func_Entry []  [])
  )
  (BB 1
    (OPER 4 Mov [(r 1)]  [(i 5)])
  )
  (BB 6
    (OPER 5 EQ [(r 6)]  [(r 1)(i 5)])
    (OPER 6 BNE []  [(r 6)(i 0)(bb 4)])
  )
  (BB 3
    (OPER 7 Load [(r 7)]  [(s a)])
    (OPER 8 Mov [(r 7)]  [(i 3)])
    (OPER 9 Store []  [(r 7)(s a)])
  )
  (BB 5
    (OPER 14 Mov [(r 3)]  [(i 0)])
    (OPER 15 Mov [(r 5)]  [(i 1)])
  )
  (BB 9
    (OPER 16 LTE [(r 9)]  [(r 5)(i 8)])
    (OPER 17 BNE []  [(r 9)(i 0)(bb 8)])
  )
  (BB 7
    (OPER 18 Add_I [(r 10)]  [(r 3)(r 5)])
    (OPER 19 Mov [(r 3)]  [(r 10)])
    (OPER 20 Add_I [(r 11)]  [(r 5)(i 1)])
    (OPER 21 Mov [(r 5)]  [(r 11)])
    (OPER 22 Jmp []  [(bb 9)])
  )
  (BB 8
    (OPER 23 Div_I [(r 12)]  [(r 3)(i 3)])
    (OPER 24 Mov [(r 4)]  [(r 12)])
    (OPER 25 Mul_I [(r 13)]  [(r 4)(i 4)])
    (OPER 26 Mov [(r 3)]  [(r 13)])
    (OPER 27 Pass []  [(r 1)])
    (OPER 29 Load [(r 14)]  [(s a)])
    (OPER 28 Pass []  [(r 14)])
    (OPER 30 JSR []  [(s addThem)])
    (OPER 31 Mov [(r 15)]  [(m RetReg)])
    (OPER 32 Mov [(r 2)]  [(r 15)])
    (OPER 34 Add_I [(r 16)]  [(r 2)(r 3)])
    (OPER 35 Add_I [(r 17)]  [(r 16)(i 48)])
    (OPER 33 Pass []  [(r 17)])
    (OPER 36 JSR []  [(s putchar)])
    (OPER 37 Mov [(r 18)]  [(m RetReg)])
    (OPER 38 Pass []  [(i 10)])
    (OPER 39 JSR []  [(s putchar)])
    (OPER 40 Mov [(r 19)]  [(m RetReg)])
    (OPER 41 Mov [(m RetReg)]  [(r 0)])
  )
  (BB 2
    (OPER 2 Func_Exit []  [])
    (OPER 3 Return []  [(m RetReg)])
  )
  (BB 4
    (OPER 10 Load [(r 8)]  [(s a)])
    (OPER 11 Mov [(r 8)]  [(i 4)])
    (OPER 12 Store []  [(r 8)(s a)])
    (OPER 13 Jmp []  [(bb 5)])
  )
)
