Byte code generation
--------------------

Code generation target
======================

Target of `Mk54` compiler is to produce a JAR file that when run with with JRE will perform calculations
defined by MK program. I selected JAR file instead of .class because JAR files are easier to execute.

Generated class should be able to handle command line parameters used to set up starting values of stack and memory 
registers and print stack and/or memory registers after program completion.

Easiest way to achieve this without writing byte code for all operations is to use "template" class where all necessary 
scaffolding will be written in Java and one method will work as a placeholder for actual generated byte code. This class
is `net.ninjacat.mk54.Mk54`. It has main() method which will handle all setup code, then call `execute()` method 
and then perform all necessary teardown.  

With the help of [ASM](https://asm.ow2.io/) new class will be created based on `net.ninjacat.mk54.Mk54` where empty 
`execute()` method body will be replaced with generated byte code. This new class will *not* be a subclass of 
`net.ninjacat.mk54.Mk54`, but instead exactly the same class with one additional method `execute()`.   


Details
=======

### Numeric entry

Being a calculator, MK family enter numbers one digit at a time. To enter number 42.42e-42 will require following 
program

    00. 4
    01. 2
    02. .
    03. 4
    04. 2
    05. ВП
    06. 4
    07. 2
    08 /-/

After each operand register X will contain a new number, first 4, then 42, then 42.0, then 42.4 and so on. This means
that to correctly support the same in `Mk54` it will require a helper class which will handle the state of 
register X, by deciding whether next digit should be part of integer or fractional part of mantissa or exponent.

Helper class, even internal means another .class-file generated by compiler and I try to avoid it if possible. 
This means I should generate the code for each digit and maintain state of register X inside main class.

Solution to that is to have separate variables for mantissa and exponent and to modify them separately depending on
entry mode. Modifications are done by calling Mk54 class internal methods which also will calculate actual value of
X register.

---

After any calculation flag `entryMode` will be set to `MANTISSA` so that any digit entry will be performed on mantissa.
Variable `decimalFactor` will be reset to `0` at the same time.

Adding new digit to mantissa is more or less straightforward, but it requires a state variable `decimalFactor`. While
decimal factor is 0, all digits are added to integer part of the number. For each digit N, current value of register X
is multiplied by 10 and then N is added to register X. When `.` operation executes it will set `decimalFactor` to 10.
When `decimalFactor` is not equal to 0, each new digit will be divided by current `decimalFactor` and added to 
register X. After that `decimalFactor` itself will be divided by 10 to be ready for next fractional digit. This logic
is handled by `mantissaDigitEntry(int)` method.

Things become trickier as soon as `ВП` or `EXP` operation is encountered. This operation puts calculator in exponent 
entry mode. There is no easy way to manage adding exponent digit-by-digit and changing sign of the exponent when `/-/` 
operation is executed. 

`ВП` operation will switch state flag `entryMode` to value `EXPONENT`. When in `EXPONENT` mode for each digit entry 
method `exponentDigitEntry(int)` will be called. It will perform necessary operations to add next digit, while managing 
exponent in -99..+99 range. 

Unary minus operator `/-/` works the same way. Mantissa sign negation is implemented by calling `negateMantissa()` method,
while exponent sign change is done in `negateExponent()` method. 

---

After `В↑` operation is performed value from register X is copied to register Y. Register X still contains the same
value, but any digit operation will reset register X and start from scratch. This is implemented using flag `resetX`, 
which is checked on each digit operation and set to `true` on all operations not related to number entry.

---

Every new digit entry after performed operation will push stack forward, moving X->Y, Y->Z and Z->T. 

### Register size

Original MK-series registers were limited by their displays which could only show 8 digits of significand. Float data 
type does not have such limitation, and `mk54` also does not limit its registers to 8 digits. This means that unlike
in calculator one can create a program which enters more than 8 digits into a register and it will work. There is no
cutting off input after first 8 digits. 
 