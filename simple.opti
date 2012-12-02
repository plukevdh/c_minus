(FUNCTION  main  []
  (BB 0
    (OPER 1 Func_Entry []  [])
  )
  (BB 1
    (OPER 4 Mov [(r 1)]  [(i 5)])
  )
  (BB 6
    (OPER 5 LT [(r 2)]  [(r 1)(i 10)])
    (OPER 6 BNE []  [(r 2)(i 0)(bb 5)])
  )
  (BB 3
    (OPER 7 Mov [(r 1)]  [(i 6)])
  )
  (BB 5
    (OPER 8 Sub_I [(r 3)]  [(r 1)(i 4)])
    (OPER 9 Mov [(r 1)]  [(r 3)])
  )
  (BB 2
    (OPER 2 Func_Exit []  [])
    (OPER 3 Return []  [(m RetReg)])
  )
)
