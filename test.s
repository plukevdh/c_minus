.text
	.align 4
.globl  main
main:
	pushl	%EBP
	movl	%ESP, %EBP
	pushl	%EBX
main_bb1:
	movl	$5, %EBX
	pushl	$4
	call	putchar
	addl	$4, %ESP
main_bb6:
	cmpl	$4, %EBX
	jg	main_bb4
main_bb3:
main_bb5:
main_bb2:
	popl	%EBX
	movl	%EBP, %ESP
	popl	%EBP
	ret
main_bb4:
	jmp	main_bb5
