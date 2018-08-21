Executing compiled programs
===========================

`mk54` compiler produces executable jar file without any external dependencies. It can be executed with JRE's `java` command.
Given lack of interactive capabilities of compiled program, data for the program must be preloaded into stack and memory
registers. This is done through command-line arguments to a compiled program.  

Setting initial configuration
-----------------------------

    java -jar program.jar [options]
    
    Options:
     -x VALUE    - sets value of X register
     -y VALUE    - sets value of Y register
     -z VALUE    - sets value of Z register
     -t VALUE    - sets value of T register
     -x1 VALUE   - sets value of X1 register
     -M<r> VALUE - sets value of memory address <r>, where <r> is number from 0 to E
     -s ADDRESS  - sets starting address for program execution (0..104), default is 0
     -h          - prints this message
     
     
Retrieving calculation results
------------------------------

After execution is completed, contents of all stack and memory registers are printed out, similar to example below

     X: 14.4
     Y: 12e-12
     Z: 0.0
     T: 0.0
    X1: 7.2
        
    M[0] 0.0  M[1] 0.0   M[2] 0.0
    M[3] 0.0  M[4] 0.0   M[5] 0.0
    M[6] 0.0  M[7] 0.0   M[8] 0.0
    M[9] 0.0  M[A] 0.0   M[B] 0.0
    M[C] 0.0  M[D] 0.0   M[E] 0.0
