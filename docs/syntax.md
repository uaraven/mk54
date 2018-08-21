MK syntax
=========

This is just a short introduction to MK-series hardware and program syntax.

MK hardware
-----------

MK programmable calculators supported 98 or 104 bytes of program memory,
4 operation stack registers and 14 or 15 addressable memory registers.

Older calculators (Б3-34, MK-54) had 98 bytes of program memory and 14 memory registers, newer calculators (MK-52, MK-62)
had 104 bytes of program memory and 15 memory registers.

Multi-operand commands use reverse polish notation, i.e. to perform 2 + 3, one would use following sequence:

    01. 2
    02. в↑
    03. 3
    04. +
    
Operation stack
---------------

Stack consists of four registers X, Y, Z and T. Calculator always displays contents of register X and any input goes
into register X.

Operation `в↑` or `enter` copies value of X into Y and moves values in stack up, X -> Y, Y -> Z, Z -> T. Value of 
register T is lost.

Every single-operand commands work on value in register X, two-operand commands work with values of registers X and Y.

Operation `F⟳` (`rot`) rotates stack X -> T, Y -> X, Z -> Y, T -> Z. Operation `↔` (or `swap`) swaps registers X and Y.

There is additional register X1 which saves previous value of register X before any operation or new number entry.

Memory
------

Each memory register is addressable with its number from 0 to E. Values in memory can be used for indirect jumps, 
subroutine calls and memory access

Program memory
--------------

Most of the operations occupy one byte. Exceptions are direct jump/call commands which include destination address as
a second byte.

`mk54` compiler does not limit program to 98 or 104 operations. Program size is limited by the fact that jump address
must fit in one byte, so maximal address for jump can be 255. 

Program file format
-------------------

Programs should be written using following format:

 [address.] operation\
 [address.] operation\
 ...\
 [address.] operation

Each operation must be entered on its own line. `address` is optional address of the operation in MK program memory. 
Addresses start from zero and **must** increase by one for each following operation. Address must be separated from 
operation mnemonic by . (dot) character.

`mk54` parser ignores addresses and only parses operation mnemonics. 

Example of MK program:

    00. 1
    01. 2
    02. В↑
    03. 1
    04. 2
    05. +
    06. П0
    07. ПХ 0
    08. F Вх
    09. STOP

See also [here](mk-program-format.md).

Operations and mnemonics
------------------------

MK-series operations correspond to button presses on calculator keyboard. Older/newer calculators may have different 
markings for the same operations. Some of the operations use cyrillic characters, while other use latin. 

To provide maximal flexibility `mk54` parser supports multiple mnemonics for some operations. 

Table below is incomplete list of supported operations, please consult [opcodes.json](https://github.com/uaraven/mk54/blob/master/src/main/resources/opcodes.json)
file for complete list of supported operations and mnemonics.

Parser ignores case and whitespace in command mnemonics, so Fвх and F ВХ will be treated identically.

| Operation code(hex)  | Mnemonics | Description                                               |
|:--------------------:|:----------|-----------------------------------------------------------|
| 00, 01, 02, 03, 04, 05, 06, 07, 08, 09  | 0, 1, 2, 3, 4, 5, 6, 7, 8, 9  | Inputs next digit to the number in register X |
| 0A                   | .         | Decimal point |
| 0B                   | /-/, neg  | Negation. Inverts sign of either mantissa or exponent, depending on entry mode|
| 0C                   | вп, ee    | Exponent entry. Following numbers will be entered as part of exponent, not mantissa|
| 0D                   | cx        | "Clear X" Set register X to zero. This command can be entered using either cyrillic or latin characters |
| 0E                   | ↑, b↑, в↑, b^, в^, enter | Copies register X into Y, prepares for next entry |
| 0F                   | f bx, f вx| Restore previous value. Copies X1 into X |
| 10                   | +         | Add values in X and Y, put result into X |
| 11                   | -         | Subtract X from Y, put result into X  |
| 12                   | *, x      | Multiply X by Y, put result into X |
| 13                   | /, ÷      | Divide Y by X, put result into X |
| 14                   | ↔, x<->y, <->, xy, swap | Swaps values in registers X and Y |
| 15                   | f 10^x    | Calculate 10 to power X |
| 16                   | f e^x     | Calculate `e` to power X |
| 17                   | f lg      | Calculate decimal logarithm of X |
| 18                   | f ln      | Calculate natural logarithm of X |
| 19                   | f arcsin, f asin | Calculate arcsine |
| 1A                   | f arccos, f acos | Calcualte arccosine |
| 1B                   | f arctg, f arctan, f atan| Calculate arctangent |
| 1C                   | f sin     | Calculate sine |
| 1D                   | f cos     | Calculate cosine |
| 1E                   | f tg, f tan | Calculate tangent |
| 20                   | f pi, f π | Puts value of π in X register |
| 21                   | f √, f sqrt | Calculate square root of X |
| 22                   | f x^2"    | Calculate square of the X register |
| 23                   | f 1/x     | Calculate inverse of the X register |
| 24                   | f x^y     | Calculate X to the power of Y |
| 25                   | f ⟳, f r, f rot | Rotate stack. X -> T, Y -> X, Z -> Y, T -> Z |
| 26                   | k m→g, k м→г, k m->g, k м->г | Convert minutes into degrees. Not supported yet |
| 27                   | k -       | Program fault. Causes program to stop execution and display `Error` message |
| 28                   | k +       | Program fault. Causes program to stop execution and display `Error` message |
| 29                   | k /, k ÷  | Program fault. Causes program to stop execution and display `Error` message |
| 2A                   | k ms→g, k мс→г, k ms->g, k мс->г | Convert minutes and seconds to degrees. Not supported yet|
| 30                   | k g→ms, г→мс, g->ms, г->мс | Convert degrees to minutes and seconds. Not supported yet | 
| 31                   | k abs     | Calculate absolute value of the X |
| 32                   | k зн, k sign | Evaluate numbers sign. If value in X is greater then zero, 1 will be placed in X, if X < 0, then -1 one will be placed in X |
| 33                   | k g→m, k г→м, k g->m, k г->м | Convert degrees to minutes. Not supported yet |
| 34                   | k [x], k trunc | Stores integer part of the number into X |
| 35                   | k {x}, k frac  | Stores fractional part of the number into X |
| 36                   | k max     | Calculate maximal of register X and Y. Known bug in MK calculators treated zero as the largest number. This bug is replicated in compiled code |
| 37                   | k and, k ∧ | Logical AND. Not implemented | 
| 38                   | k or, k ∨ | Logical OR. Not implemented |
| 39                   | k xor, k ⊕ | Logical XOR. Not implemented |
| 3A                   | k not, k инв | Logical NOT. Not implemented | 
| 3B                   | k rand, k сч | Generate random number in 0..1 interval. `mk54` uses algorithm used in MK calculators, not Java `Random`. Note that MK's PRNG is very poor |
| 4*M*                 | пM, xпM, x->пM, stoM | Stores value from X into memory register M. M must be in 0..E range |
| 50                   | с/п, r/s, stop | Start/stop program. Actually only `stop` is supported | 
| 51                   | бп, goto | Unconditional jump. Next byte in program memory must contain address of the jump target |
| 52                   | в/о, ret | Return from subroutine |
| 53                   | пп, call | Call subroutine. Next byte in program memory must contain address of the subroutine |
| 54                   | k nop, k ноп | No operation | 
| 57                   | f x!=0, f x≠0, f x<>0 | Conditional goto when X is not equal to 0. Next byte contains address of the jump | 
| 58                   | f l2 | Loop over value in memory register 2. See below for loop details |
| 59                   | f x>=0, f x≥0 | Conditional goto when X is greater than or equal to 0. Next byte contains address of the jump |
| 5A                   | f l3 | Loop over value in memory register 3. See below for loop details |
| 5B                   | f l1 | Loop over value in memory register 1. See below for loop details |
| 5C                   | f x<0 | Conditional goto when X is less than 0. Next byte contains address of the jump |
| 5D                   | f l0 | Loop over value in memory register 0. See below for loop details |
| 5E                   | f x=0, f x==0 |  Conditional goto when X is equal to 0. Next byte contains address of the jump |
| 6*M*                   | ипM, пxM, rclM | Recalls value from memory register M into X. M must be in 0..E range |
| 7*M*                   | k x!=0 M, k x<>0 M, k x≠0 M | Conditional indirect jump to address in memory register M when X is not equal to 0. Value in memory register M is modified prior to jump. See below for modification rules |
| 8*M*                   | k goto M, k бп M | Goto address in memory register M. Value in memory register M is modified prior to jump. See below for modification rules | 
| 9*M*                   | k x>=0 M, k x≥0 M | Conditional indirect jump to address in memory register M when X is greater than 0. Value in memory register M is modified prior to jump. See below for modification rules |
| A*M*                   | k call M, k пп M | Call subroutine at address in memory register M. Value in memory register M is modified prior to jump. See below for modification rules |
| B*M*                   | k sto M, k x->п M, k x→п M | Indirect memory write by address in register M. Value in memory register M is modified prior to jump. See below for modification rules |
| C*M*                   | k x<0 M | Indirect conditional goto by address in memory register M when X is less than zero. Value in memory register M is modified prior to jump. See below for modification rules |
| D*M*                   | k rcl M, k п->x M, k п→х M | Indirect memory read from address in register M. Value in memory register M is modified prior to jump. See below for modification rules |
| E*M*                   | k x=0 M | Indirect conditional goto by address in memory register M when X is equal to zero. Value in memory register M is modified prior to jump. See below for modification rules |


### Register modification during indirect operations

Operations which perform indirect access to memory register modify address in the register before executing operation.

Depending on the memory register number following modifications are performed:

  - M0..M3 - register value is decremented
  - M4..M6 - register value is incremented
  - M7..ME - register value is left unchanged
  
If any of the register contains floating point value, then fractional part is truncated. There are no boundary 
checks performed, so if register contains invalid address, it will cause program termination with error.

### Loops

Loop commands decrement number in corresponding memory register by one and if it is not equal to zero jump to address
in the next byte of program memory. After all loop iterations have completed, register will contain 1.

MK series had undocumented feature, when loop counter register contained negative number or zero, first iteration will
put -99999999 into counter and then loop will increment register until it reaches zero. JVM byte code does not replicate
this behaviour, instead loop is immediately terminated.