package tacit.core

import fastparse.all._

import Arithmetic._

object LineParser {
  val whitespace: P[Unit] = P(
    CharIn(" ")
      .rep(1)
      .map(_ => ()))

  def location[T](
    parser: P[SourceLocation => T]
  ): P[T] =
    (Index ~ parser ~ Index) map {
      case (begin, partial, end) =>
        partial(SourceLocation(begin, end))
    }

  def text[T](
    parser: P[Unit],
    transform: String => SourceLocation => T
  ): P[T] =
    location((parser.!) map (transform))

  def noText[T](
    parser: P[Unit],
    result: SourceLocation => T
  ): P[T] =
    location((parser) map (_ => result))

  val termEnd: P[Unit] = P(CharIn(" ") | End)

  val notTermEnd: P[Unit] = P(!(termEnd) ~ AnyChar)

  def validTerm(
    parser: P[SyntaxTerm]
  ): P[SyntaxTerm.Valid] =
    (parser ~ &(termEnd)) map (SyntaxTerm.Valid)

  val number: P[SyntaxTerm.Literal] = P(
    text(
      CharIn("-").? ~ CharIn('0' to '9').rep(1),
      text => SyntaxTerm.Literal(text.toInt, _)))

  val invalidNumber: P[SyntaxTerm.Unknown] = P(
    CharIn('0' to '9')
      .rep(1)
      ~ noText(
        notTermEnd.rep(1),
        SyntaxTerm.InvalidLiteralSuffix))

  val invalidOther: P[SyntaxTerm.Unknown] = P(
    noText(notTermEnd.rep(1), SyntaxTerm.InvalidOther))

  def op(
    symbol: Char,
    arithmetic: Arithmetic
  ): P[SyntaxTerm.Operator] =
    noText(
      CharIn(symbol to symbol),
      SyntaxTerm.Operator(arithmetic, _))

  val operator: P[SyntaxTerm.Operator] = P(
    op('+', A_+)
      | op('-', A_-)
      | op('*', A_*)
      | op('/', A_/))

  val unknownTerm: P[SyntaxTerm.Unknown] = P(
    validTerm(number)
      | validTerm(operator)
      | invalidNumber
      | invalidOther)

  val expression: P[Seq[SyntaxTerm.Unknown]] = P(
    Start
      ~ whitespace.?
      ~ unknownTerm.rep(0, whitespace)
      ~ whitespace.?
      ~ End)

  def checkExpression(
    result: Parsed[Seq[SyntaxTerm.Unknown]]
  ): Either[Seq[GuestError], Seq[SyntaxTerm]] = {
    val unknown = result.fold(
      (failedParser, index, _) =>
        Seq(
          SyntaxTerm.ParseError(
            s"Failed to parse $failedParser",
            SourceLocation(index, index + 1))),
      (parseResult, _) => parseResult)

    def errors = unknown collect {
      case invalidTerm: SyntaxTerm.Invalid =>
        (convertToError(invalidTerm)(
          invalidTerm.sourceLocation))
    }

    if (errors.nonEmpty) {
      Left(errors)
    } else {
      Right(unknown collect {
        case SyntaxTerm.Valid(term) => term
      })
    }
  }

  def convertToError(
    invalidTerm: SyntaxTerm.Invalid
  ): SourceLocation => GuestError =
    invalidTerm match {
      case SyntaxTerm.InvalidLiteralSuffix(_) =>
        GuestError.withoutAnnotations(
          _,
          "This number suffix is not valid")
      case SyntaxTerm.InvalidOther(_) =>
        GuestError.withoutAnnotations(
          _,
          "This name is not in scope")
      case SyntaxTerm.ParseError(message, _) =>
        GuestError.withoutAnnotations(_, message)
    }

  def parse(
    in: String
  ): Either[Seq[GuestError], Seq[SyntaxTerm]] =
    checkExpression(expression.parse(in))
}
