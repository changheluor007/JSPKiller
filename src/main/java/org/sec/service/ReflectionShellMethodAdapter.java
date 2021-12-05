package org.sec.service;

import org.apache.log4j.Logger;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.sec.core.CoreMethodAdapter;

import java.util.*;

public class ReflectionShellMethodAdapter extends CoreMethodAdapter<String> {
    private Logger logger = Logger.getLogger(ReflectionShellMethodAdapter.class);

    private final int access;
    private final String desc;
    private final Map<String, List<Boolean>> analysisData;

    public ReflectionShellMethodAdapter(int api, MethodVisitor mv, String owner,
                                        int access, String name, String desc,
                                        String signature, String[] exceptions,
                                        Map<String, List<Boolean>> analysisData) {
        super(api, mv, owner, access, name, desc, signature, exceptions);
        this.access = access;
        this.desc = desc;
        this.analysisData = analysisData;
    }

    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
        if (opcode == Opcodes.INVOKEINTERFACE) {
            boolean getParam = name.equals("getParameter") &&
                    owner.equals("javax/servlet/http/HttpServletRequest") &&
                    desc.equals("(Ljava/lang/String;)Ljava/lang/String;");
            if (getParam) {
                super.visitMethodInsn(opcode, owner, name, desc, itf);
                logger.info("find source: request.getParameter");
                operandStack.get(0).add("get-param");
                return;
            }
        }
        if(opcode==Opcodes.INVOKESTATIC){
            boolean forName = name.equals("forName") &&
                    owner.equals("java/lang/Class") &&
                    desc.equals("(Ljava/lang/String;)Ljava/lang/Class;");
            if(forName){
                if(operandStack.get(0).contains("ldc-runtime")){
                    super.visitMethodInsn(opcode, owner, name, desc, itf);
                    logger.info("-> get Runtime class");
                    operandStack.get(0).add("class-runtime");
                    return;
                }
            }
        }
        if(opcode==Opcodes.INVOKEVIRTUAL){
            boolean getMethod = name.equals("getMethod") &&
                    owner.equals("java/lang/Class") &&
                    desc.equals("(Ljava/lang/String;[Ljava/lang/Class;)Ljava/lang/reflect/Method;");

            boolean invoke = name.equals("invoke") &&
                    owner.equals("java/lang/reflect/Method") &&
                    desc.equals("(Ljava/lang/Object;[Ljava/lang/Object;)Ljava/lang/Object;");
            if(getMethod){
                if(operandStack.get(1).contains("ldc-get-runtime")){
                    super.visitMethodInsn(opcode, owner, name, desc, itf);
                    logger.info("-> get getRuntime method");
                    operandStack.get(0).add("method-get-runtime");
                    return;
                }
                if(operandStack.get(1).contains("ldc-exec")){
                    super.visitMethodInsn(opcode, owner, name, desc, itf);
                    logger.info("-> get exec method");
                    operandStack.get(0).add("method-exec");
                    return;
                }
            }
            if(invoke){
                if(operandStack.get(0).contains("get-param")){
                    if(operandStack.get(2).contains("method-exec")){
                        logger.info("find reflection webshell!");
                        super.visitMethodInsn(opcode, owner, name, desc, itf);
                        return;
                    }
                    super.visitMethodInsn(opcode, owner, name, desc, itf);
                    logger.info("-> method exec invoked");
                }
            }
        }
        super.visitMethodInsn(opcode, owner, name, desc, itf);
    }

    @Override
    public void visitInsn(int opcode) {
        if(opcode==Opcodes.AASTORE){
            if(operandStack.get(0).contains("get-param")){
                logger.info("store request param into array");
                super.visitInsn(opcode);
                operandStack.get(0).clear();
                operandStack.get(0).add("get-param");
                return;
            }
        }
        super.visitInsn(opcode);
    }

    @Override
    public void visitLdcInsn(Object cst) {
        if(cst.equals("java.lang.Runtime")){
            super.visitLdcInsn(cst);
            operandStack.get(0).add("ldc-runtime");
            return;
        }
        if(cst.equals("getRuntime")){
            super.visitLdcInsn(cst);
            operandStack.get(0).add("ldc-get-runtime");
            return;
        }
        if(cst.equals("exec")){
            super.visitLdcInsn(cst);
            operandStack.get(0).add("ldc-exec");
            return;
        }
        super.visitLdcInsn(cst);
    }
}
