package tacit.jsShell

import org.scalajs.dom.html
import scalacss.ScalatagsCss._
import scalatags.JsDom.all.{
  Double2CssNumber => _,
  Int2CssNumber => _,
  _
}

import CssSettings.Defaults._

object ErrorLine extends Render[html.Div] {
  def render: RenderComponent = {
    val root = div(
      Style.root
    )
    (root, new Component(_))
  }

  final class Component(
    val root: Root
  )

  object Style extends StyleSheet.Inline {
    import Style.dsl._

    val root: StyleA = style(
      color(Colors.error)
    )
  }
}
