(FUNCTION  fact  [(int x)]
  (BB 0
    (OPER 1 Func_Entry []  [])
  )
  (BB 1
  )
  (BB 6
    (OPER 4 GT [(r 2)]  [(r 1)(i 1)])
    (OPER 5 BNE []  [(r 2)(i 0)(bb 4)])
  )
  (BB 3
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
    (OPER 4 JSR []  [(s read)])
    (OPER 5 Mov [(r 1)]  [(r 0)])
  )
  (BB 6
    (OPER 6 GT [(r 2)]  [(r 1)(i 0)])
    (OPER 7 BNE []  [(r 2)(i 0)(bb 5)])
  )
  (BB 3
    (OPER 9 Pass []  [(r 1)])
    (OPER 10 JSR []  [(s fact)])
    (OPER 8 Pass []  [(r 0)])
    (OPER 11 JSR []  [(s write)])
  )
  (BB 5
  )
  (BB 2
    (OPER 2 Func_Exit []  [])
    (OPER 3 Return []  [(m RetReg)])
  )
)
