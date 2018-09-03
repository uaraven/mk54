package net.ninjacat.mk54.opcodes;

import com.google.common.collect.ImmutableSet;
import net.ninjacat.mk54.exceptions.UnknownOperationException;

import java.util.Set;
import java.util.stream.IntStream;

public final class Opcode {
    public static final String DECIMAL_POINT = "0A";
    public static final String NEG = "0B";
    public static final String EXP = "0C";
    public static final String CX = "0D";
    public static final String ENTER = "0E";
    public static final String RESTORE_X = "0F";

    public static final String ADD = "10";
    public static final String SUB = "11";
    public static final String MUL = "12";
    public static final String DIV = "13";

    public static final String SWAP = "14";
    public static final String TEN_TO_POWER_X = "15";
    public static final String E_TO_POWER_X = "16";
    public static final String LOG10 = "17";
    public static final String LN = "18";

    public static final String ARCSIN = "19";
    public static final String ARCCOS = "1A";
    public static final String ARCTAN = "1B";
    public static final String SIN = "1C";
    public static final String COS = "1D";
    public static final String TAN = "1E";

    public static final String PI = "20";
    public static final String SQRT = "21";
    public static final String POW2 = "22";
    public static final String INV = "23";
    public static final String X_POW_Y = "24";
    public static final String ROT = "25";
    public static final String MIN_TO_DEG = "26";
    public static final String FAIL1 = "27";
    public static final String FAIL2 = "28";
    public static final String FAIL3 = "29";
    public static final String MIN_SEC_TO_DEG = "2A";
    public static final String DEG_TO_MIN_SEC = "30";
    public static final String ABS = "31";
    public static final String SIGN = "32";
    public static final String DEG_TO_MIN = "33";

    public static final String TRUNC = "34";
    public static final String FRAC = "35";
    public static final String MAX = "36";

    public static final String AND = "37";
    public static final String OR = "38";
    public static final String XOR = "39";
    public static final String NOT = "3A";

    public static final String RND = "3B";

    public static final String STOP = "50";
    public static final String GOTO = "51";
    public static final String RET = "52";
    public static final String GOSUB = "53";
    public static final String NOP = "54";

    public static final String JNZ = "57";
    public static final String JGEZ = "59";
    public static final String JLTZ = "5C";
    public static final String JZ = "5E";

    public static final String LOOP0 = "5D";
    public static final String LOOP1 = "5B";
    public static final String LOOP2 = "58";
    public static final String LOOP3 = "5A";

    private static final String STO_BASE = "4%X";
    private static final String RCL_BASE = "6%X";
    private static final String IJNZ_BASE = "7%X";
    private static final String IGOTO_BASE = "8%X";
    private static final String IJGEZ_BASE = "9%X";
    private static final String ICALL_BASE = "A%X";
    private static final String ISTO_BASE = "B%X";
    private static final String IJLZ_BASE = "C%X";
    private static final String IRCL_BASE = "D%X";
    private static final String IJZ_BASE = "E%X";

    public static String DIGIT(final int digit) {
        return String.format("%02X", digit);
    }

    public static String STO(final int location) {
        return String.format(STO_BASE, location);
    }

    public static String RCL(final int location) {
        return String.format(RCL_BASE, location);
    }

    public static String IGOTO(final int register) {
        return String.format(IGOTO_BASE, register);
    }

    public static String LOOP(final int register) {
        switch (register) {
            case 0:
                return LOOP0;
            case 1:
                return LOOP1;
            case 2:
                return LOOP2;
            case 3:
                return LOOP3;
            default:
                throw new UnknownOperationException("LOOP(" + register + ")");
        }
    }

    public static String IJNZ(final int register) {
        return String.format(IJNZ_BASE, register);
    }

    public static String IJGEZ(final int register) {
        return String.format(IJGEZ_BASE, register);
    }

    public static String IJLZ(final int register) {
        return String.format(IJLZ_BASE, register);
    }

    public static String IJZ(final int register) {
        return String.format(IJZ_BASE, register);
    }

    public static String ICALL(final int register) {
        return String.format(ICALL_BASE, register);
    }

    public static String ISTO(final int register) {
        return String.format(ISTO_BASE, register);
    }

    public static String IRCL(final int register) {
        return String.format(IRCL_BASE, register);
    }

    /**
     * Jump operations with additional operand - take two bytes and need to be handled correctly when building
     * trampoline table
     */
    private static final Set<String> JUMP_OPS = ImmutableSet.of(
            Opcode.GOTO,
            GOSUB,
            JNZ,
            JZ,
            JGEZ,
            JLTZ,
            LOOP0,
            LOOP1,
            LOOP2,
            LOOP3
    );

    private static final ImmutableSet.Builder<String> KEEP_STACK_BUILDER = ImmutableSet.builder();

    static {
        KEEP_STACK_BUILDER.add(DECIMAL_POINT);
        KEEP_STACK_BUILDER.add(NEG);
        KEEP_STACK_BUILDER.add(EXP);

        IntStream.range(0, 10)
                .forEach(digit -> KEEP_STACK_BUILDER.add(DIGIT(digit)));
    }

    /**
     * List of operations which do not set 'pushStack' flag
     */
    private static final Set<String> KEEP_STACK = KEEP_STACK_BUILDER.build();

    /**
     * Checks if operation is a jump instruction, which contain address in the next byte
     */
    public static boolean isJump(final String code) {
        return JUMP_OPS.contains(code);
    }

    /**
     * Checks if operation should set reset X flag
     *
     * @param code operation code
     * @return true or false
     */
    public static boolean shouldResetX(final String code) {
        return !KEEP_STACK.contains(code);
    }

    private Opcode() {
    }
}
