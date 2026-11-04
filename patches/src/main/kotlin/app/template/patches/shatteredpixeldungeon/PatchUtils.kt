package app.template.patches.shatteredpixeldungeon

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.extensions.InstructionExtensions.addInstruction
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.morphe.patcher.extensions.InstructionExtensions.replaceInstruction
import app.morphe.patcher.patch.BytecodePatchContext
import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.util.smali.ExternalLabel
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction

internal const val EXTENSION_PACKAGE = "Lapp/template/extension/shatteredpixeldungeon"

context(_: BytecodePatchContext)
internal fun Fingerprint.returnTrue() {
	method.addInstructions(
		0,
		"""
			const/4 v0, 0x1
			return v0
		""",
	)
}

/**
 * Makes a boolean method return true early when [check] returns true, otherwise runs the original code.
 * [check] is an extension method call whose arguments are given in [arguments].
 */
context(_: BytecodePatchContext)
internal fun Fingerprint.returnTrueWhen(check: String, arguments: String = "") {
	val implementation = method.implementation ?: throw PatchException("${method.name} has no code")
	val isStatic = AccessFlags.STATIC.isSet(method.accessFlags)
	val parameterRegisters = method.parameters.sumOf { if (it.type == "J" || it.type == "D") 2 else 1 } + if (isStatic) 0 else 1
	if (implementation.registerCount <= parameterRegisters) throw PatchException("${method.name} has no free register")

	method.addInstructionsWithLabels(
		0,
		"""
			invoke-static { $arguments }, $check
			move-result v0
			if-eqz v0, :original
			return v0
		""",
		ExternalLabel("original", 0),
	)
}

/**
 * Runs [smali] before the method returns. The return instruction is replaced in place, so branches that jump
 * straight to it also run the new code.
 */
context(_: BytecodePatchContext)
internal fun Fingerprint.callBeforeReturn(smali: String) {
	val instructions = method.implementation!!.instructions
	val index = instructions.indexOfLast { it.opcode == Opcode.RETURN_VOID }
	if (index < 0) throw PatchException("${method.name} has no return-void")
	method.replaceInstruction(index, smali)
	method.addInstruction(index + 1, "return-void")
}

/**
 * Passes every value the method returns through [adjust], a static method that takes the value
 * (plus [extraArguments]) and returns the new value. Only works for int and float returns.
 */
context(_: BytecodePatchContext)
internal fun Fingerprint.adjustReturns(adjust: String, extraArguments: String = "") {
	val instructions = method.implementation!!.instructions
	val indices = instructions.withIndex()
		.filter { it.value.opcode == Opcode.RETURN }
		.map { it.index }
	if (indices.isEmpty()) throw PatchException("${method.name} has no return")

	indices.reversed().forEach { index ->
		val register = (instructions[index] as OneRegisterInstruction).registerA
		if (register > 15) throw PatchException("${method.name} returns from v$register, which is out of range")
		val arguments = if (extraArguments.isEmpty()) "v$register" else "v$register, $extraArguments"
		method.replaceInstruction(index, "invoke-static { $arguments }, $adjust")
		method.addInstructions(
			index + 1,
			"""
				move-result v$register
				return v$register
			""",
		)
	}
}
