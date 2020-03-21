package com.anatawa12.jasm.parser

import org.objectweb.asm.Opcodes

enum class GrammarInstrucion(val type: InstrucionType, val opcode: Int) {
    NOP(InstrucionType.Insn, Opcodes.NOP),
    ACONST_NULL(InstrucionType.Insn, Opcodes.ACONST_NULL),
    ICONST_M1(InstrucionType.Insn, Opcodes.ICONST_M1),
    ICONST_0(InstrucionType.Insn, Opcodes.ICONST_0),
    ICONST_1(InstrucionType.Insn, Opcodes.ICONST_1),
    ICONST_2(InstrucionType.Insn, Opcodes.ICONST_2),
    ICONST_3(InstrucionType.Insn, Opcodes.ICONST_3),
    ICONST_4(InstrucionType.Insn, Opcodes.ICONST_4),
    ICONST_5(InstrucionType.Insn, Opcodes.ICONST_5),
    LCONST_0(InstrucionType.Insn, Opcodes.LCONST_0),
    LCONST_1(InstrucionType.Insn, Opcodes.LCONST_1),
    FCONST_0(InstrucionType.Insn, Opcodes.FCONST_0),
    FCONST_1(InstrucionType.Insn, Opcodes.FCONST_1),
    FCONST_2(InstrucionType.Insn, Opcodes.FCONST_2),
    DCONST_0(InstrucionType.Insn, Opcodes.DCONST_0),
    DCONST_1(InstrucionType.Insn, Opcodes.DCONST_1),
    BIPUSH(InstrucionType.IntInsn, Opcodes.BIPUSH),
    SIPUSH(InstrucionType.IntInsn, Opcodes.SIPUSH),
    LDC(InstrucionType.LdcInsn, Opcodes.LDC),
    ILOAD(InstrucionType.VarInsn, Opcodes.ILOAD),
    LLOAD(InstrucionType.VarInsn, Opcodes.LLOAD),
    FLOAD(InstrucionType.VarInsn, Opcodes.FLOAD),
    DLOAD(InstrucionType.VarInsn, Opcodes.DLOAD),
    ALOAD(InstrucionType.VarInsn, Opcodes.ALOAD),
    IALOAD(InstrucionType.Insn, Opcodes.IALOAD),
    LALOAD(InstrucionType.Insn, Opcodes.LALOAD),
    FALOAD(InstrucionType.Insn, Opcodes.FALOAD),
    DALOAD(InstrucionType.Insn, Opcodes.DALOAD),
    AALOAD(InstrucionType.Insn, Opcodes.AALOAD),
    BALOAD(InstrucionType.Insn, Opcodes.BALOAD),
    CALOAD(InstrucionType.Insn, Opcodes.CALOAD),
    SALOAD(InstrucionType.Insn, Opcodes.SALOAD),
    ISTORE(InstrucionType.VarInsn, Opcodes.ISTORE),
    LSTORE(InstrucionType.VarInsn, Opcodes.LSTORE),
    FSTORE(InstrucionType.VarInsn, Opcodes.FSTORE),
    DSTORE(InstrucionType.VarInsn, Opcodes.DSTORE),
    ASTORE(InstrucionType.VarInsn, Opcodes.ASTORE),
    IASTORE(InstrucionType.Insn, Opcodes.IASTORE),
    LASTORE(InstrucionType.Insn, Opcodes.LASTORE),
    FASTORE(InstrucionType.Insn, Opcodes.FASTORE),
    DASTORE(InstrucionType.Insn, Opcodes.DASTORE),
    AASTORE(InstrucionType.Insn, Opcodes.AASTORE),
    BASTORE(InstrucionType.Insn, Opcodes.BASTORE),
    CASTORE(InstrucionType.Insn, Opcodes.CASTORE),
    SASTORE(InstrucionType.Insn, Opcodes.SASTORE),
    POP(InstrucionType.Insn, Opcodes.POP),
    POP2(InstrucionType.Insn, Opcodes.POP2),
    DUP(InstrucionType.Insn, Opcodes.DUP),
    DUP_X1(InstrucionType.Insn, Opcodes.DUP_X1),
    DUP_X2(InstrucionType.Insn, Opcodes.DUP_X2),
    DUP2(InstrucionType.Insn, Opcodes.DUP2),
    DUP2_X1(InstrucionType.Insn, Opcodes.DUP2_X1),
    DUP2_X2(InstrucionType.Insn, Opcodes.DUP2_X2),
    SWAP(InstrucionType.Insn, Opcodes.SWAP),
    IADD(InstrucionType.Insn, Opcodes.IADD),
    LADD(InstrucionType.Insn, Opcodes.LADD),
    FADD(InstrucionType.Insn, Opcodes.FADD),
    DADD(InstrucionType.Insn, Opcodes.DADD),
    ISUB(InstrucionType.Insn, Opcodes.ISUB),
    LSUB(InstrucionType.Insn, Opcodes.LSUB),
    FSUB(InstrucionType.Insn, Opcodes.FSUB),
    DSUB(InstrucionType.Insn, Opcodes.DSUB),
    IMUL(InstrucionType.Insn, Opcodes.IMUL),
    LMUL(InstrucionType.Insn, Opcodes.LMUL),
    FMUL(InstrucionType.Insn, Opcodes.FMUL),
    DMUL(InstrucionType.Insn, Opcodes.DMUL),
    IDIV(InstrucionType.Insn, Opcodes.IDIV),
    LDIV(InstrucionType.Insn, Opcodes.LDIV),
    FDIV(InstrucionType.Insn, Opcodes.FDIV),
    DDIV(InstrucionType.Insn, Opcodes.DDIV),
    IREM(InstrucionType.Insn, Opcodes.IREM),
    LREM(InstrucionType.Insn, Opcodes.LREM),
    FREM(InstrucionType.Insn, Opcodes.FREM),
    DREM(InstrucionType.Insn, Opcodes.DREM),
    INEG(InstrucionType.Insn, Opcodes.INEG),
    LNEG(InstrucionType.Insn, Opcodes.LNEG),
    FNEG(InstrucionType.Insn, Opcodes.FNEG),
    DNEG(InstrucionType.Insn, Opcodes.DNEG),
    ISHL(InstrucionType.Insn, Opcodes.ISHL),
    LSHL(InstrucionType.Insn, Opcodes.LSHL),
    ISHR(InstrucionType.Insn, Opcodes.ISHR),
    LSHR(InstrucionType.Insn, Opcodes.LSHR),
    IUSHR(InstrucionType.Insn, Opcodes.IUSHR),
    LUSHR(InstrucionType.Insn, Opcodes.LUSHR),
    IAND(InstrucionType.Insn, Opcodes.IAND),
    LAND(InstrucionType.Insn, Opcodes.LAND),
    IOR(InstrucionType.Insn, Opcodes.IOR),
    LOR(InstrucionType.Insn, Opcodes.LOR),
    IXOR(InstrucionType.Insn, Opcodes.IXOR),
    LXOR(InstrucionType.Insn, Opcodes.LXOR),
    IINC(InstrucionType.IincInsn, Opcodes.IINC),
    I2L(InstrucionType.Insn, Opcodes.I2L),
    I2F(InstrucionType.Insn, Opcodes.I2F),
    I2D(InstrucionType.Insn, Opcodes.I2D),
    L2I(InstrucionType.Insn, Opcodes.L2I),
    L2F(InstrucionType.Insn, Opcodes.L2F),
    L2D(InstrucionType.Insn, Opcodes.L2D),
    F2I(InstrucionType.Insn, Opcodes.F2I),
    F2L(InstrucionType.Insn, Opcodes.F2L),
    F2D(InstrucionType.Insn, Opcodes.F2D),
    D2I(InstrucionType.Insn, Opcodes.D2I),
    D2L(InstrucionType.Insn, Opcodes.D2L),
    D2F(InstrucionType.Insn, Opcodes.D2F),
    I2B(InstrucionType.Insn, Opcodes.I2B),
    I2C(InstrucionType.Insn, Opcodes.I2C),
    I2S(InstrucionType.Insn, Opcodes.I2S),
    LCMP(InstrucionType.Insn, Opcodes.LCMP),
    FCMPL(InstrucionType.Insn, Opcodes.FCMPL),
    FCMPG(InstrucionType.Insn, Opcodes.FCMPG),
    DCMPL(InstrucionType.Insn, Opcodes.DCMPL),
    DCMPG(InstrucionType.Insn, Opcodes.DCMPG),
    IFEQ(InstrucionType.JumpInsn, Opcodes.IFEQ),
    IFNE(InstrucionType.JumpInsn, Opcodes.IFNE),
    IFLT(InstrucionType.JumpInsn, Opcodes.IFLT),
    IFGE(InstrucionType.JumpInsn, Opcodes.IFGE),
    IFGT(InstrucionType.JumpInsn, Opcodes.IFGT),
    IFLE(InstrucionType.JumpInsn, Opcodes.IFLE),
    IF_ICMPEQ(InstrucionType.JumpInsn, Opcodes.IF_ICMPEQ),
    IF_ICMPNE(InstrucionType.JumpInsn, Opcodes.IF_ICMPNE),
    IF_ICMPLT(InstrucionType.JumpInsn, Opcodes.IF_ICMPLT),
    IF_ICMPGE(InstrucionType.JumpInsn, Opcodes.IF_ICMPGE),
    IF_ICMPGT(InstrucionType.JumpInsn, Opcodes.IF_ICMPGT),
    IF_ICMPLE(InstrucionType.JumpInsn, Opcodes.IF_ICMPLE),
    IF_ACMPEQ(InstrucionType.JumpInsn, Opcodes.IF_ACMPEQ),
    IF_ACMPNE(InstrucionType.JumpInsn, Opcodes.IF_ACMPNE),
    GOTO(InstrucionType.JumpInsn, Opcodes.GOTO),
    JSR(InstrucionType.JumpInsn, Opcodes.JSR),
    RET(InstrucionType.VarInsn, Opcodes.RET),
    TABLESWITCH(InstrucionType.TableSwitchInsn, Opcodes.TABLESWITCH),
    LOOKUPSWITCH(InstrucionType.LookupSwitchInsn, Opcodes.LOOKUPSWITCH),
    IRETURN(InstrucionType.Insn, Opcodes.IRETURN),
    LRETURN(InstrucionType.Insn, Opcodes.LRETURN),
    FRETURN(InstrucionType.Insn, Opcodes.FRETURN),
    DRETURN(InstrucionType.Insn, Opcodes.DRETURN),
    ARETURN(InstrucionType.Insn, Opcodes.ARETURN),
    RETURN(InstrucionType.Insn, Opcodes.RETURN),
    GETSTATIC(InstrucionType.FieldInsn, Opcodes.GETSTATIC),
    PUTSTATIC(InstrucionType.FieldInsn, Opcodes.PUTSTATIC),
    GETFIELD(InstrucionType.FieldInsn, Opcodes.GETFIELD),
    PUTFIELD(InstrucionType.FieldInsn, Opcodes.PUTFIELD),
    INVOKEVIRTUAL(InstrucionType.MethodInsn, Opcodes.INVOKEVIRTUAL),
    INVOKESPECIAL(InstrucionType.MethodInsn, Opcodes.INVOKESPECIAL),
    INVOKESTATIC(InstrucionType.MethodInsn, Opcodes.INVOKESTATIC),
    INVOKEINTERFACE(InstrucionType.MethodInsn, Opcodes.INVOKEINTERFACE),
    INVOKEDYNAMIC(InstrucionType.InvokeDynamicInsn, Opcodes.INVOKEDYNAMIC),
    NEW(InstrucionType.TypeInsn, Opcodes.NEW),
    NEWARRAY(InstrucionType.IntInsn, Opcodes.NEWARRAY),
    ANEWARRAY(InstrucionType.TypeInsn, Opcodes.ANEWARRAY),
    ARRAYLENGTH(InstrucionType.Insn, Opcodes.ARRAYLENGTH),
    ATHROW(InstrucionType.Insn, Opcodes.ATHROW),
    CHECKCAST(InstrucionType.TypeInsn, Opcodes.CHECKCAST),
    INSTANCEOF(InstrucionType.TypeInsn, Opcodes.INSTANCEOF),
    MONITORENTER(InstrucionType.Insn, Opcodes.MONITORENTER),
    MONITOREXIT(InstrucionType.Insn, Opcodes.MONITOREXIT),
    MULTIANEWARRAY(InstrucionType.MultiANewArrayInsn, Opcodes.MULTIANEWARRAY),
    IFNULL(InstrucionType.JumpInsn, Opcodes.IFNULL),
    IFNONNULL(InstrucionType.JumpInsn, Opcodes.IFNONNULL),
    ;

    val keyword = TokenType.KeyWord(name.toLowerCase())
}

enum class InstrucionType {
    FieldInsn,
    IincInsn,
    Insn,
    IntInsn,
    InvokeDynamicInsn,
    JumpInsn,
    LdcInsn,
    LookupSwitchInsn,
    MethodInsn,
    MultiANewArrayInsn,
    TableSwitchInsn,
    TypeInsn,
    VarInsn,
}
