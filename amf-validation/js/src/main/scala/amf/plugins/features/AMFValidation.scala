package amf.plugins.features

import amf.plugins.features.validation.AMFValidatorPlugin

import scala.scalajs.js.annotation.JSExportAll

@JSExportAll
object AMFValidation {
  def register(): Unit = {
    amf.Core.registerPlugin(AMFValidatorPlugin)
  }
}
