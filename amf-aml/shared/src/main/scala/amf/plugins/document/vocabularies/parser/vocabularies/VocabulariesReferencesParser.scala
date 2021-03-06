package amf.plugins.document.vocabularies.parser.vocabularies
import amf.core.annotations.Aliases
import amf.core.model.document.{BaseUnit, DeclaresModel}
import amf.core.model.domain.AmfObject
import amf.core.parser.{CallbackReferenceCollector, ParsedReference, ReferenceCollector}
import amf.plugins.document.vocabularies.model.domain.External
import amf.validation.DialectValidations.ExpectedVocabularyModule
import org.yaml.model.{YMap, YScalar}
import amf.plugins.document.vocabularies.parser.dialects.DialectAstOps.DialectYMapOps

case class VocabulariesReferencesParser(map: YMap, references: Seq[ParsedReference])(implicit ctx: VocabularyContext) {

  def parse(location: String): ReferenceCollector[AmfObject] = {
    val result = CallbackReferenceCollector(VocabularyRegister())
    parseLibraries(result, location)
    parseExternals(result, location)
    result
  }

  private def target(url: String): Option[BaseUnit] =
    references.find(r => r.origin.url.equals(url)).map(_.unit)

  private def parseLibraries(result: ReferenceCollector[AmfObject], id: String): Unit = {
    map.key(
      "uses",
      entry =>
        entry.value
          .as[YMap]
          .entries
          .foreach(e => {
            val alias: String = e.key.as[YScalar].text
            val url: String   = e.value.as[YScalar].text
            target(url)
              .foreach {
                case module: DeclaresModel => result += (alias, collectAlias(module, alias -> (module.id, url)))
                case other =>
                  ctx.eh.violation(ExpectedVocabularyModule, id, s"Expected vocabulary module but found: $other", e) // todo Uses should only reference modules...
              }
          })
    )
  }

  private def parseExternals(result: ReferenceCollector[AmfObject], id: String): Unit = {
    map.key(
      "external",
      entry =>
        entry.value
          .as[YMap]
          .entries
          .foreach(e => {
            val alias: String = e.key.as[YScalar].text
            val base: String  = e.value.as[YScalar].text
            val external      = External()
            result += (alias, external.withAlias(alias).withBase(base))
          })
    )
  }

  private def collectAlias(module: BaseUnit,
                           alias: (Aliases.Alias, (Aliases.FullUrl, Aliases.RelativeUrl))): BaseUnit = {
    module.annotations.find(classOf[Aliases]) match {
      case Some(aliases) =>
        module.annotations.reject(_.isInstanceOf[Aliases])
        module.add(aliases.copy(aliases = aliases.aliases + alias))
      case None => module.add(Aliases(Set(alias)))
    }
  }
}
