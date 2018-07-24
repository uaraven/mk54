GOTO support
============

Being a machine code language MK-series allows unconditional jumps (called БП) in mnemonic language from any place to
any place. БП command (called GOTO in the rest of this document) will change instruction pointer to the address specified
as the next operation. Technically this allows to jump to 256 addresses, but MK calculators had only 98 to 105 bytes of
program memory. 

Of course when you writing program in machine code which must be no longer than 98 bytes you have to be very clever 
with your code. MK programs are great examples of spaghetti-code. This causes challenges when transpiling MK code to
byte code. 

As `mk54` generates byte code, it keeps a list of labels (used by ASM to generate correct offsets for jump commands) for
each MK operation. When GOTO operation is encountered it can easily be compiled to byte code GOTO instruction with a
corresponding label.

And here come stack frames. JVM requires compiler to generate stack frame at any jump target. This is easy for jumps 
ahead, labels that are used as jump target can be marked and stack frame can be generated when this label is reached. 
But back jumps cannot be handled this way as byte code for these operations is already generated.

Possible solutions
------------------

1. Do two-pass compilation, noting all jumps and storing their target addresses. This will allow to generate stack frames
when they are needed. Unfortunately this doesn't work well with indirect jumps, where target address is known only in runtime.

2. Generate trampoline table consisting of stack frames and GOTO instructions to each block of code corresponding to MK 
operations. Generate stack frame for each such block of code and JVM should not complain, as each will be a target of 
GOTO instructions. Indirect jumps will be routed through trampolines. Trampoline table will add some overhead, though. 

