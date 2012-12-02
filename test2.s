.text
	.align 4
.globl  gcd
gcd:
	pushl	%EBP
	movl	%ESP, %EBP
	pushl	%EBX
	pushl	%EBP
	movl	16(%ESP), %EBP
	movl	20(%ESP), %EBX
gcd_bb6:
	cmpl	$0, %EBX
	je	gcd_bb4
gcd_bb3:
	movl	%EBP, %EAX
gcd_bb2:
	popl	%EBP
	popl	%EBX
	movl	%EBP, %ESP
	popl	%EBP
	ret
gcd_bb4:
	movl	$0, %EDX
	movl	%EBP, %EAX
	idivl	%EBX, %EAX
	imull	%EBX, %EAX
	movl	%EAX, %ECX
	movl	%EBP, %EAX
	subl	%ECX, %EAX
	pushl	%EAX
	pushl	%EBX
	call	gcd
	addl	$8, %ESP
	movl	(%EAX), %EAX
gcd_bb5:
.globl  main
main:
	pushl	%EBP
	movl	%ESP, %EBP
	pushl	%EBX
main_bb1:
	call	input
	movl	(%EAX), %EAX
	movl	%EAX, %EBX
	call	input
	movl	(%EAX), %EAX
	pushl	%EAX
	pushl	%EBX
	call	gcd
	addl	$8, %ESP
	movl	(%EAX), %EAX
	pushl	%EAX
	call	output
	addl	$4, %ESP
main_bb2:
	popl	%EBX
	movl	%EBP, %ESP
	popl	%EBP
	ret
