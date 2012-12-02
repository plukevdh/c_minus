(FUNCTION  main  []
  (BB 0
    (OPER 1 Func_Entry []  [])
  )
  (BB 1
    (OPER 4 Mov [(r 1)]  [(i 5)])
    (OPER 5 Pass []  [(i 4)])
    (OPER 6 JSR []  [(s putchar)])
    (OPER 7 Mov [(r 2)]  [(m RetReg)])
  )
  (BB 6
    (OPER 8 GT [(r 3)]  [(r 1)(i 4)])
    (OPER 9 BNE []  [(r 3)(i 0)(bb 4)])
  )
  (BB 3
    (OPER 10 Mov [(r 1)]  [(i 0)])
  )
  (BB 5
    (OPER 13 Mov [(r 1)]  [(i 10)])
  )
  (BB 2
    (OPER 2 Func_Exit []  [])
    (OPER 3 Return []  [(m RetReg)])
  )
  (BB 4
    (OPER 11 Mov [(r 1)]  [(i 99)])
    (OPER 12 Jmp []  [(bb 5)])
  )
)
