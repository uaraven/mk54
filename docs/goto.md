GOTO support
============

Being a machine code language MK-series allows unconditional jumps (called БП) in mnemonic language from any place to
any place. БП command (called GOTO in the rest of this document) will change instruction pointer to the address specified
as the next operation. Technically this allows to jump to 256 addresses, but MK calculators had only 98 to 105 bytes of
program memory. This address is referred to as *MK address* in this document.

Of course when you writing program in machine code which must be no longer than 98 bytes you have to be very clever 
with your code. MK programs are great examples of spaghetti-code. This causes challenges when generating JVM byte code
from MK code.

As `mk54` generates byte code, it keeps a list of labels (used by ASM to generate correct offsets for jump commands) for
each MK operation. When GOTO operation is encountered it can easily be compiled to byte code GOTO instruction with a
corresponding label.

And here come stack frames. JVM requires compiler to generate stack frame at any jump target. This is easy for jumps 
ahead, labels that are used as jump target can be marked and stack frame can be generated when this label is reached. 
But back jumps cannot be handled this way as byte code for these operations is already generated.

To simplify code generation, `mk54` code generator performs two passes over calculator code. On the first pass it creates
mapping between MK addresses and JVM labels, taking into account two byte operations, on the second pass actual code
generation happens.  

Direct jump
-----------

For each MK operation code generator creates a label and injects this label before the first byte of byte code generated 
for this instruction. New stack frame of type `F_SAME` is generated for that location as well.

Code generator keeps a map between MK address and JVM byte code address (AKA label).

This allows to generate simple JVM goto instruction for a label that corresponds to a target MK address.

Indirect jump
-------------

JVM does not have instruction for indirect jumps per se. It has `tableswitch` instruction that allows jump based on value
on stack. `tableswitch` contains a table of addresses to jump to based on the value of the stack.
See more details on `tableswitch` instruction [here](https://docs.oracle.com/javase/specs/jvms/se8/html/jvms-6.html#jvms-6.5.tableswitch)
and [here](https://cs.au.dk/~mis/dOvs/jvmspec/ref-tableswi.html).

For indirect jumps code generator will create a "trampoline" - special `tableswitch` instruction which can jump to any
of MK addresses. MK addresses can have values starting from 0 to the number of MK operations - 1. To perform indirect jump
code generator generates instructions to push MK address onto stack and jump to trampoline which will pick correct JVM 
byte code address to jump to.

Subroutines
-----------

Although JVM supports instructions for subroutine call/return (`jsr` and `ret`) these instructions cannot be used
in .class files with version 51 or higher (i.e. Java 6 and up).

### Calling subroutines 

Direct and indirect `gosub` operand does not significantly differ from direct and indirect jump commands. The only difference
is that gosub stores MK address of the next operation onto subroutine stack. Rest of the code will be identical to `goto`
command. 

### Returning from subroutines

When `В/О` operation is encountered code generator will generate byte code to pop latest MK address from subroutine 
stack onto JVM stack and then will generate jump to trampoline `tableswitch` instruction, which in turn will jump to
correct JVM address for that MK address.     

### Differences between MK calculators and generated JVM code

 - MK calculators subroutine return address stack was limited to 5 addresses, JVM code does not have a limits on return
address stack.
 - MK calculators allow following program
 
       00. 01
       01. 02
       03. GOTO
       04. 05
       05. GOTO
       06. 04
       
   Second `GOTO` will jump to address `04`, which contains address `05` of previous
   GOTO command. Code at this address is a valid operation which will append number 5
   to register X.
   
   `mk54` code generator does not support such jumps as there is no byte code generated for
   operations at addresses `04` and `06`. `GOTO 04` will generate code identical to `GOTO 03`
 - MK calculators allow jumps to anywhere in address space, for example following program is valid and will be executed
      
      00. GOTO
      01. 90
     
   It will jump to address 90, and execute operations there, which would be `00`, so it will run appending zeroes to 
   register X.
   
   `mk54` code generator does not generate byte code for operations which are not present in the program, so it is
   unable to generate jumps beyond last operation. Such jumps will generate exception during code generation. 
   
Loops
-----

MK calculators supported loops over values in memory registers 0..3. While value in corresponding register is greater
than 0, calculator will decrement register and perform jump to target address. 
Value of register will not be decremented to 0 though, it will be left equal to 1.

*Undocumented behaviour*
If value of memory register was less than or equal to zero, then it will be changed to -99999999 and the value will be
incremented towards 0, and not decremented. 

This behaviour is not supported by `mk54` code generator.     
 