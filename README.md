0x10^c Macro Assembler - or Tena for short
==========================================

Tena is a macro assembler that can compile mixed assembly/macro files
to create either executeable DCPU-16 bytecode or flat-assembly files suitable
for using in other assemblers.

For example, Tena compiles the below program:

    ; Reading characters from the keyboard
    ; by Markus Persson
    
    #macro push(what) {
        set push, what
    }
    
    #macro pop(what) {
        set pop, what
    }
    
    #macro nextkey(target) {
        push(i)
        set i,[keypointer]
        add i,0x9000
        set target,[i]
        ife target,0
            set pc, end
    
        set [i],0
        add [keypointer], 1
        and [keypointer], 0xf
    :end
        pop(i)
    }
    
    nextkey(a)
    nextkey(b)
    
    :keypointer
    dat 0

into this:

    19a1 7861 001e 7c62 9000 3801 800c 7dc1
    000e 80e1 85e2 001e bde9 001e 1981 19a1
    7861 001e 7c62 9000 3811 801c 7dc1 001d
    80e1 85e2 001e bde9 001e 1981 0020 0000

(note this program will not do much, you'll need an emulator with keyboard buffer support (NOT the DCPU Studio default mode) and register views, and you'll need to enclose the reading macros in a loop)

It can also correctly compile the example program in the DCPU specs and will attempt to use short-form literals for non-label values (labels need to be fixed up after code generation so are too complicated to make short-form).

Tena is written in Java and can be used on the command line like so:

java -jar assembler.jar [args]

where args are:

* \-in [file]: the file to assemble
* \-out [file]: the file[s] to output to (WILL OVERWRITE)
* \-f [type]: one of either `asm` (output bare assembly after processing macros), `bin` (output executable DCPU code) or `both` (output `bin` to [-out].bin and `asm` to [-out].dasm)

Tena supports decimal, hexadecimal (0xdeadbeef) and octal (0123) numbers. It currently does not support org, .inc, .incbin, .reserve, literal expressions, or other extensions. Some of these are planned later. 