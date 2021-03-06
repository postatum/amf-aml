package amf.dialects
import amf.ProfileName
import amf.client.parse.DefaultParserErrorHandler
import amf.core.services.RuntimeValidator
import amf.core.unsafe.PlatformSecrets
import amf.core.{AMFCompiler, CompilerContextBuilder}
import amf.plugins.document.vocabularies.AMLPlugin
import amf.plugins.document.vocabularies.model.document.Dialect
import amf.plugins.features.validation.AMFValidatorPlugin
import org.scalatest.{Assertion, AsyncFunSuite, Matchers}

import scala.concurrent.{ExecutionContext, Future}

class DialectDefinitionValidationTest extends AsyncFunSuite with Matchers with ReportComparison with PlatformSecrets with DefaultAmfInitialization {

  override implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global

  test("Test missing version") {
    validate("/missing-version/dialect.yaml", Some("/missing-version/report.json"))
  }

  test("Test missing dialect name") {
    validate("/missing-dialect-name/dialect.yaml", Some("/missing-dialect-name/report.json"))
  }

  test("Test invalid property term uri for description") {
    validate("/schema-uri/dialect.yaml", Some("/schema-uri/report.json"))
  }

  test("Test missing range in property mapping") {
    validate("/missing-range-in-mapping/dialect.yaml", Some("/missing-range-in-mapping/report.json"))
  }

  private val path: String = "amf-aml/shared/src/test/resources/vocabularies2/instances/invalids"

  protected def validate(dialect: String, goldenReport: Option[String]): Future[Assertion] = {
    for {
      dialect <- {
        new AMFCompiler(
          new CompilerContextBuilder("file://" + path + dialect, platform, eh = DefaultParserErrorHandler.withRun())
            .build(),
          Some("application/yaml"),
          Some(AMLPlugin.ID)
        ).build()
      }
      report <- {
        RuntimeValidator(
          dialect,
          ProfileName(dialect.asInstanceOf[Dialect].nameAndVersion())
        )
      }
      assertion <- assertReport(report, goldenReport.map(g => s"$path/$g"))
    } yield {
      assertion
    }
  }
}
