package amf.plugins.document.vocabularies.model.document

import amf.core.metamodel.Obj
import amf.core.model.document.{BaseUnit, EncodesModel}
import amf.core.parser.{Annotations, Fields}
import amf.plugins.document.vocabularies.metamodel.document.DialectFragmentModel
import amf.plugins.document.vocabularies.metamodel.document.DialectModel.{Encodes, References}
import amf.plugins.document.vocabularies.model.domain.NodeMapping

case class DialectFragment(fields: Fields, annotations: Annotations)
    extends BaseUnit
    with EncodesModel
    with ExternalContext[DialectFragment] {

  def references: Seq[BaseUnit]     = fields.field(References)
  override def encodes: NodeMapping = fields.field(Encodes)

  override def componentId: String = ""

  def withEncodes(nodeMapping: NodeMapping): DialectFragment = set(Encodes, nodeMapping)

  def meta: Obj = DialectFragmentModel
}

object DialectFragment {
  def apply(): DialectFragment = apply(Annotations())

  def apply(annotations: Annotations): DialectFragment = DialectFragment(Fields(), annotations)
}
