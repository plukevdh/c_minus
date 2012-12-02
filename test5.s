.data
.comm	a,4,4

.text
	.align 4
.globl  addThem
addThem:
	pushl	%EBP
	movl	%ESP, %EBP
	movl	8(%ESP), %EAX
	movl	12(%ESP), %ECX
addThem_bb1:
	addl	%ECX, %EAX
addThem_bb2:
	movl	%EBP, %ESP
	popl	%EBP
	ret
.globl  main
main:
	pushl	%EBP
	movl	%ESP, %EBP
	pushl	%EBX
	pushl	%EDI
main_bb1:
	movl	$5, %ECX
main_bb6:
	cmpl	$5, %ECX
	je	main_bb4
main_bb3:
	movl	$3, %EAX
	movl	%EAX, a
main_bb5:
	movl	$0, %EBX
	movl	$1, %EDX
main_bb9:
	cmpl	$8, %EDX
	jle	main_bb8
main_bb7:
	movl	%EBX, %EAX
	addl	%EDX, %EAX
	movl	%EAX, %EBX
	movl	%EDX, %EAX
	addl	$1, %EAX
	movl	%EAX, %EDX
	jmp	main_bb9
main_bb8:
	movl	$0, %EDX
	movl	%EBX, %EAX
	movl	$3, %EBX
	idivl	%EBX, %EAX
	movl	$4, %EDX
	imull	%EDX, %EAX
	movl	%EAX, %EBX
	pushl	%ECX
	movl	a, %EAX
	pushl	%EAX
	call	addThem
	addl	$8, %ESP
	addl	%EBX, %EAX
	addl	$48, %EAX
	pushl	%EAX
	call	putchar
	addl	$4, %ESP
	pushl	$10
	call	putchar
	addl	$4, %ESP
	movl	%EDI, %EAX
main_bb2:
	popl	%EDI
	popl	%EBX
	movl	%EBP, %ESP
	popl	%EBP
	ret
main_bb4:
	movl	$4, %EAX
	movl	%EAX, a
	jmp	main_bb5
