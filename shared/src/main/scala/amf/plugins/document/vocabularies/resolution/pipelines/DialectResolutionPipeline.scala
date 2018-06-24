package amf.plugins.document.vocabularies.resolution.pipelines

import amf.ProfileNames
import amf.ProfileNames.ProfileName
import amf.core.resolution.pipelines.ResolutionPipeline
import amf.core.resolution.stages.ResolutionStage
import amf.plugins.document.vocabularies.model.document.Dialect
import amf.plugins.document.vocabularies.resolution.stages.DialectReferencesResolutionStage

class DialectResolutionPipeline(override val model: Dialect) extends ResolutionPipeline[Dialect] {

  override protected val steps: Seq[ResolutionStage] = Seq(new DialectReferencesResolutionStage())
  override def profileName: ProfileName              = ProfileNames.AMF
}
