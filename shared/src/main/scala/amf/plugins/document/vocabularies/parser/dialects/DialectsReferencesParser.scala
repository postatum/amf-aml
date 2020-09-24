package amf.plugins.document.vocabularies.parser.dialects

import amf.core.annotations.Aliases
import amf.core.annotations.Aliases._
import amf.core.model.document.{BaseUnit, DeclaresModel, RecursiveUnit}
import amf.core.parser.{ParsedReference, Reference}
import amf.plugins.document.vocabularies.model.document.{Dialect, DialectFragment, Vocabulary}
import amf.plugins.document.vocabularies.model.domain.External
import amf.validation.DialectValidations.DialectError
import org.yaml.model.{YMap, YMapEntry, YScalar, YType}
import amf.plugins.document.vocabularies.parser.dialects.DialectAstOps._

case class DialectsReferencesParser(dialect: Dialect, map: YMap, references: Seq[ParsedReference])(
    implicit ctx: DialectContext) {

  def parse(): ReferenceDeclarations = {
    val declarations = ReferenceDeclarations()

    references.foreach {
      case ParsedReference(f: DialectFragment, origin: Reference, None) => declarations += (origin.url, f)
      case ParsedReference(r: RecursiveUnit, origin: Reference, _)      => declarations += (origin.url, r)
      case _                                                            =>
    }

    parseUses(declarations)
    parseExternals(declarations)
    declarations
  }

  private def parseUses(declarations: ReferenceDeclarations): Unit = {
    map.key(
        "uses",
        entry =>
          entry.value
            .as[YMap]
            .entries
            .foreach(entry => parseUsesEntry(entry, declarations))
    )
  }

  private def parseUsesEntry(e: YMapEntry, declarations: ReferenceDeclarations): Unit = {
    val alias: String = e.key.as[YScalar].text
    val url: String   = targetUrl(e)
    targetBaseUnit(url).foreach {
      case vocabulary: Vocabulary =>
        collectAlias(dialect, alias -> (vocabulary.base.value(), url))
        declarations += (alias, vocabulary)
      case module: DeclaresModel =>
        collectAlias(dialect, alias -> (module.id, url))
        declarations += (alias, module)
      case other =>
        ctx.recursiveDeclarations.get(url) match {
          case Some(r: RecursiveUnit) =>
            declarations += (alias, r)
          case None =>
            val node = dialect.location().getOrElse(dialect.id)
            ctx.eh.violation(DialectError, node, s"Expected vocabulary module but found: $other", e) // todo Uses should only reference modules...
        }
    }
  }

  private def targetBaseUnit(url: String): Option[BaseUnit] =
    references.find(r => r.origin.url.equals(url)).map(_.unit)

  private def targetUrl(e: YMapEntry): String = e.value.tagType match {
    case YType.Include => e.value.as[YScalar].text
    case _             => e.value
  }

  private def collectAlias(aliasCollectorUnit: BaseUnit, alias: (Alias, (FullUrl, RelativeUrl))): BaseUnit = {
    aliasCollectorUnit.annotations.find(classOf[Aliases]) match {
      case Some(aliases) =>
        aliasCollectorUnit.annotations.reject(_.isInstanceOf[Aliases])
        aliasCollectorUnit.add(aliases.copy(aliases = aliases.aliases + alias))
      case None => aliasCollectorUnit.add(Aliases(Set(alias)))
    }
  }

  private def parseExternals(result: ReferenceDeclarations): Unit = {
    map.key(
        "external",
        entry =>
          entry.value
            .as[YMap]
            .entries
            .foreach(e => {
              val alias: String = e.key.as[YScalar].text
              val base: String  = e.value
              val external      = External()
              result += external.withAlias(alias).withBase(base)
            })
    )
  }
}
