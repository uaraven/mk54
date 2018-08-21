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
| 14                   | ↔,x<->y,<->, xy, swap | Swaps values in registers X and Y |
| 15                   | f 10^x    | Calculate 10 to power X |
| 16                   | f e^x     | Calculate `e` to power X |
| 17                   | f lg      | Calculate decimal logarithm of X |
| 18                   | f ln      | Calculate natural logarithm of X |
| 19                   | f arcsin, f asin | Calculate arcsine |
| 1A                   | f arccos, f acos | Calcualte arccosine |
| 1B                   | f arctg, f arctan, f atan| Calculate arctangent |
| 1C                   | f sin     | Calculate sine |
| 1D                   | f cos     | Calculate cosine |
| 1E                   | f tg, f tan | Calculate tagent | 