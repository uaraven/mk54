MK-54
=====

`mk54` is a compiler for soviet-era MK-series programmable calculator programs. `mk54` translates MK programs \
into JVM byte-code and produces executable JAR files.


**Note**. This project does not attempt to exactly recreate program execution of MK-series. Those calculators 
were well-known for a variety of undocumented behaviors and outright bugs.


Usage
-----

You can download jar file from [releases](https://github.com/uaraven/mk54/releases) on github:
    
Latest release at this time is [version 0.1])https://github.com/uaraven/mk54/releases/download/v0.1/mk54-0.1.jar)

To perform compilation execute

    java -jar mk54-0.1.jar OPTIONS SOURCE

Supported options:

    -d --debug       - include debugging information into resulting code. Register and memory state
                       will be dumped after each operation of MK program
    -v --verbose     - more output during compilation
    -o --output FILE - write resulting jar file to FILE
    -h --help        - print this message
    

MK programs syntax
------------------

See [separate document](docs/syntax.md) for syntax reference.

If you can read russian there are couple ([1](docs/mk52_doc/part1.pdf), [2](docs/mk52_doc/part2.pdf)) scans of original
manual for MK-52.

Executing compiled MK programs
------------------------------

See [document](docs/mk-execution.md) for instructions how to execute compiled programs.

Differences from MK hardware
----------------------------

`mk54` compiler performs most direct translation of MK opcodes into JVM byte code. Only documented commands and 
explicitly documented behaviour is supported. 

MK programmable calculators supported numbers from `-9.9999999*10^99` to `9.9999999*10^99` with most of operations having
error of `10^-6` to `3*10^-7`. JVM code uses [`double`](https://en.wikipedia.org/wiki/Double-precision_floating-point_format) 
to represent floating point values, so error margins and limits are different.

Compiled programs are non-interactive, there is no way to stop execution, ask user to put some new values in registers
and/or memory and continue from the next operation. Executing `С/П` operation will terminate running program.

On MK calculator program which does not have `С/П` command will continue to run indefinitely until stopped manually. 
JVM byte code always contains `return` as the last instruction in program.

Currently two sets of operations are not implemented:

  - degree conversions
  - logical operations

Degree conversions will be implemented soon, I am on the fence about implementing logical operations.

In case you want real hardware-level emulator of calculators built with soviet 145-series chips see [here](http://www.emulator3000.org/c3.htm).


Implementation details
----------------------

MK-series worked with decimal floating numbers represented as 8-digit mantissa and 2-digit exponent with the base of 10. 
`mk54` uses Java `double` type for all calculations. This should not cause any problems with simple programs, 
but more sophisticated programs abusing number overflow or relying on floating point number representation will fail.

[Hex code mapping](docs/hexcode.md) to MK-series 8-segment display symbols.

[JMP instructions](docs/goto.md)

Some details about Java [Byte code generation](docs/code_generation.md)

[Links](docs/links.md) to JVM spec and other documents.