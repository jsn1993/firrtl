// See LICENSE for license details.

package firrtl.stage

import firrtl._
import firrtl.stage.TransformManager.TransformDependency
import firrtl.stage.transforms.HookTransform

/*
 * - InferWidths should have InferTypes split out
 * - ConvertFixedToSInt should have InferTypes split out
 * - Move InferTypes out of ZeroWidth
 */

object Forms {

  val ChirrtlForm: Seq[TransformDependency] = Seq.empty

  private[firrtl] val MinimalHighForm: Seq[TransformDependency] = ChirrtlForm ++
    Seq( classOf[passes.CheckChirrtl],
         classOf[passes.CInferTypes],
         classOf[passes.CInferMDir],
         classOf[passes.RemoveCHIRRTL] )

  private[firrtl] val WorkingIR: Seq[TransformDependency] = MinimalHighForm :+ classOf[passes.ToWorkingIR]

  private[firrtl] val Resolved: Seq[TransformDependency] = WorkingIR ++
    Seq( classOf[passes.CheckHighForm],
         classOf[passes.ResolveKinds],
         classOf[passes.InferTypes],
         classOf[passes.CheckTypes],
         classOf[passes.Uniquify],
         classOf[passes.ResolveFlows],
         classOf[passes.CheckFlows],
         classOf[passes.InferWidths],
         classOf[passes.CheckWidths],
         classOf[firrtl.transforms.InferResets] )

  private[firrtl] val Deduped: Seq[TransformDependency] = Resolved :+ classOf[firrtl.transforms.DedupModules]

  val HighForm: Seq[TransformDependency] = ChirrtlForm ++
    MinimalHighForm ++
    WorkingIR ++
    Resolved ++
    Deduped

  val MidForm: Seq[TransformDependency] = HighForm ++
    Seq( classOf[passes.PullMuxes],
         classOf[passes.ReplaceAccesses],
         classOf[passes.ExpandConnects],
         classOf[passes.RemoveAccesses],
         classOf[passes.ExpandWhensAndCheck],
         classOf[checks.CheckResets],
         classOf[passes.ConvertFixedToSInt],
         classOf[passes.ZeroWidth] )

  val LowForm: Seq[TransformDependency] = MidForm ++
    Seq( classOf[passes.LowerTypes],
         classOf[passes.Legalize],
         classOf[firrtl.transforms.RemoveReset],
         classOf[firrtl.transforms.CheckCombLoops],
         classOf[firrtl.transforms.RemoveWires] )

  val LowFormMinimumOptimized: Seq[TransformDependency] = LowForm ++
    Seq( classOf[passes.RemoveValidIf],
         classOf[passes.memlib.VerilogMemDelays],
         classOf[passes.SplitExpressions] )

  val LowFormOptimized: Seq[TransformDependency] = LowFormMinimumOptimized ++
    Seq( classOf[firrtl.transforms.ConstantPropagation],
         classOf[passes.PadWidths],
         classOf[firrtl.transforms.CombineCats],
         classOf[passes.CommonSubexpressionElimination],
         classOf[firrtl.transforms.DeadCodeElimination] )

  /** [[Transform]] that when defined as a prerequisite, will cause the defining transform to run after low FIRRTL
    * optimizations or, if running with only the low FIRRTL emitter, will run after [[LowForm]]. This replicates the old
    * behavior of transforms defining their input [[firrtl.CircuitForm]] as [[firrtl.LowForm]].
    */
  final class LowFormOptimizedHook extends HookTransform {
    override final val prerequisites = LowForm
  }

  val VerilogMinimumOptimized: Seq[TransformDependency] = LowFormMinimumOptimized ++
    Seq( classOf[firrtl.transforms.BlackBoxSourceHelper],
         classOf[firrtl.transforms.ReplaceTruncatingArithmetic],
         classOf[firrtl.transforms.FlattenRegUpdate],
         classOf[passes.VerilogModulusCleanup],
         classOf[firrtl.transforms.VerilogRename],
         classOf[passes.VerilogPrep],
         classOf[firrtl.AddDescriptionNodes] )

  val VerilogOptimized: Seq[TransformDependency] = LowFormOptimized ++
    Seq( classOf[firrtl.transforms.BlackBoxSourceHelper],
         classOf[firrtl.transforms.ReplaceTruncatingArithmetic],
         classOf[firrtl.transforms.FlattenRegUpdate],
         classOf[passes.VerilogModulusCleanup],
         classOf[firrtl.transforms.VerilogRename],
         classOf[passes.VerilogPrep],
         classOf[firrtl.AddDescriptionNodes] )

}