.text
	.align 4
.globl  main
main:
	pushl	%EBP
	movl	%ESP, %EBP
main_bb1:
	movl	$5, %EAX
main_bb6:
	cmpl	$10, %EAX
	jl	main_bb5
main_bb3:
	movl	$6, %EAX
main_bb5:
	subl	$4, %EAX
main_bb2:
	movl	%EBP, %ESP
	popl	%EBP
	ret
