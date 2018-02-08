package tacit.jsShell

import scalacss.internal.Macros.Color

import CssSettings.Defaults._

object Colors extends StyleSheet.Inline {
  import Colors.dsl._

  val background: Color = c"#1a1a1a"
  val error: Color = c"#e82d2d"
  val highlight: Color = c"#d3a7ff"
  val link: Color = c"#9069b3"
  val normal: Color = c"#e5ffe5"
  val edgeHighlight: Color = c"rgba(229, 255, 229, 0.08)"
  val value: Color = c"#46f6f6"
}
