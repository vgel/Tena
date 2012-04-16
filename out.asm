set push, i
set i, [keypointer]
add i, 36864
set a, [i]
ife a, 0
set pc, end_gs0
set [i], 0
add [keypointer], 1
and [keypointer], 15
:end_gs0
set pop, i
set push, i
set i, [keypointer]
add i, 36864
set b, [i]
ife b, 0
set pc, end_gs1
set [i], 0
add [keypointer], 1
and [keypointer], 15
:end_gs1
set pop, i
:keypointer
dat 0
