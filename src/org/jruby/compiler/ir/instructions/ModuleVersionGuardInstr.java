package org.jruby.compiler.ir.instructions;

import java.util.Map;
import org.jruby.compiler.ir.Operation;
import org.jruby.compiler.ir.operands.Label;
import org.jruby.compiler.ir.operands.Operand;
import org.jruby.compiler.ir.representations.InlinerInfo;
import org.jruby.RubyModule;
import org.jruby.runtime.DynamicScope;
import org.jruby.runtime.ThreadContext;
import org.jruby.runtime.builtin.IRubyObject;

/**
 * This instruction will be generated whenever speculative optimizations are performed
 * based on assuming that an object's metaclass is C (as determined by the version number
 * of C -- where the version number changes every time C's class structure changes).
 */
public class ModuleVersionGuardInstr extends Instr {
    /** The token value that has been assumed */
    private final int expectedVersion;

    /** The module whose version we are testing */
    private final RubyModule module;

    /** The object whose metaclass token has to be verified*/
    private Operand candidateObj;

    /** Where to jump if the version assumption fails? */
    private Label failurePathLabel;

    public ModuleVersionGuardInstr(RubyModule module, int expectedVersion, Operand candidateObj, Label failurePathLabel) {
        super(Operation.MODULE_GUARD);
        this.module = module;
        this.expectedVersion = expectedVersion;
        this.candidateObj = candidateObj;
        this.failurePathLabel = failurePathLabel;
    }

    public Label getFailurePathLabel() {
        return failurePathLabel;
    }

    @Override
    public Operand[] getOperands() {
        return new Operand[] { candidateObj };
    }

    @Override
    public void simplifyOperands(Map<Operand, Operand> valueMap, boolean force) {
        candidateObj = candidateObj.getSimplifiedOperand(valueMap, force);
    }

    @Override
    public String toString() {
        return super.toString() + "(" + candidateObj + ", " + expectedVersion + "[" + module.getName() + "], " + failurePathLabel + ")";
    }

    @Override
    public Instr cloneForInlinedScope(InlinerInfo ii) {
        return new ModuleVersionGuardInstr(module, expectedVersion, candidateObj.cloneForInlining(ii), ii.getRenamedLabel(failurePathLabel));
    }

    public boolean versionMatches(ThreadContext context, DynamicScope currDynScope, IRubyObject self, Object[] temp) {
        IRubyObject receiver = (IRubyObject) candidateObj.retrieve(context, self, currDynScope, temp);
        // if (module.getGeneration() != expectedVersion) ... replace this instr with a direct jump
        return (receiver.getMetaClass().getGeneration() == expectedVersion);
    }
}
