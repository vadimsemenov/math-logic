package ru.ifmo.ctddev.semenov.mathlogic

import ru.ifmo.ctddev.semenov.mathlogic.expressions.Expression

import scala.collection.mutable.ArrayBuffer

/**
  * @author Vadim Semenov (semenov@rain.ifmo.ru)
  */
package object propositional {
  type Proof = ArrayBuffer[Expression]
  type AnnotatedProof = ArrayBuffer[AnnotatedExpression]
  type Context = ArrayBuffer[Expression]
  type Derivation = (Context, Proof)

  object Proof {
    def apply(expressions: Expression*): Proof = ArrayBuffer(expressions: _*)
  }

  object Context {
    def apply(expressions: Expression*): Context = ArrayBuffer(expressions: _*)
  }

  object Derivation {
    def apply(context: Context, proof: Proof): Derivation = (context, proof)
  }


  /**
    * ¬α ⊢ α → β
    */
  def proveImplicationF_(α: Expression, β: Expression): Proof = deduction(Derivation(
    Context(!α, α),
    Proof(
      (!α) -> ((!β) -> (!α)),
      !α,
      (!β) -> (!α),
      α -> ((!β) -> α),
      α,
      (!β) -> α,
      ((!β) -> α) -> (((!β) -> (!α)) -> (!(!β))),
      ((!β) -> (!α)) -> (!(!β)),
      !(!β),
      (!(!β)) -> β,
      β
    )
  ))._2

  /**
    * β ⊢ α → β
    */
  def proveImplication_T(α: Expression, β: Expression): Proof = Proof(
    β -> (α -> β),
    β,
    α -> β
  )

  /**
    * α, ¬β ⊢ ¬(α → β)
    */
  def proveImplicationTF(α: Expression, β: Expression): Proof = {
    Proof(
      α -> ((α -> β) -> α),
      α,
      (α -> β) -> α,
      (!β) -> ((α -> β) -> (!β)),
      !β,
      (α -> β) -> (!β),
      ((α -> β) -> (!β)) -> (((α -> β) -> ((!β) -> (!α))) -> ((α -> β) -> (!α))),
      ((α -> β) -> ((!β) -> (!α))) -> ((α -> β) -> (!α))
    ) ++
    contraposition(α, β) ++
    Proof(
      (α -> β) -> (!α),
      ((α -> β) -> α) -> (((α -> β) -> (!α)) -> (!(α -> β))),
      ((α -> β) -> (!α)) -> (!(α -> β)),
      !(α -> β)
    )
  }

  /**
    * ¬α ⊢ ¬(α & β)
    */
  def proveConjunctionF_(α: Expression, β: Expression): Proof = Proof(
    (α & β) -> α,
    (!α) -> ((α & β) -> (!α)),
    !α,
    (α & β) -> (!α),
    ((α & β) -> α) -> (((α & β) -> (!α)) -> (!(α & β))),
    ((α & β) -> (!α)) -> (!(α & β)),
    !(α & β)
  )

  /**
    * ¬β ⊢ ¬(α & β)
    */
  def proveConjunction_F(α: Expression, β: Expression): Proof = Proof(
    (α & β) -> β,
    (!β) -> ((α & β) -> (!β)),
    !β,
    (α & β) -> (!β),
    ((α & β) -> β) -> (((α & β) -> (!β)) -> (!(α & β))),
    ((α & β) -> (!β)) -> (!(α & β)),
    !(α & β)
  )

  /**
    * α, β ⊢ α & β
    */
  def proveConjunctionTT(α: Expression, β: Expression): Proof = Proof(
    α -> (β -> (α & β)),
    α,
    β -> (α & β),
    β,
    α & β
  )

  /**
    * ⊢ α → α
    */
  def ident(α: Expression): Proof = Proof(
    (α -> (α -> α)) -> ((α -> ((α -> α) -> α)) -> (α -> α)),
    α -> (α -> α),
    (α -> ((α -> α) -> α)) -> (α -> α),
    α -> ((α -> α) -> α),
    α -> α
  )

  /**
    * ¬α, ¬β ⊢ ¬(α ∨ β)
    */
  def proveDisjunctionFF(α: Expression, β: Expression): Proof = {
    Proof(
      (!α) -> ((α V β) -> (!α)),
      !α,
      (α V β) -> (!α)
    ) ++
      ident(α) ++
      proveImplicationF_(β, α) ++
      Proof(
        (α -> α) -> ((β -> α) -> ((α V β) -> α)),
        (β -> α) -> ((α V β) -> α),
        (α V β) -> α,
        ((α V β) -> α) -> (((α V β) -> (!α)) -> (!(α V β))),
        ((α V β) -> (!α)) -> (!(α V β)),
        !(α V β)
      )
  }


  /**
    * α ⊢ α ∨ β
    */
  def proveDisjunctionT_(α: Expression, β: Expression): Proof = Proof(
    α -> (α V β),
    α,
    α V β
  )

  /**
    * β ⊢ α ∨ β
    */
  def proveDisjunction_T(α: Expression, β: Expression): Proof = Proof(
    β -> (α V β),
    β,
    α V β
  )

  /**
    * α ⊢ ¬¬α
    */
  def doubleNegationIntroduction(α: Expression): Proof = {
    Proof(
      α -> ((!α) -> α),
      α,
      (!α) -> α
    ) ++
    ident(!α) ++
    Proof(
      ((!α) -> α) -> (((!α) -> (!α)) -> (!(!α))),
      ((!α) -> (!α)) -> (!(!α)),
      !(!α)
    )
  }

  /**
    * α → β, ¬α → β ⊢ β
    */
  def lawOfExcludedMiddle(α: Expression, β: Expression): Proof = tertiumNonDatur(α) ++ Proof(
    (α -> β) -> (((!α) -> β) -> ((α V (!α)) -> β)),
    α -> β,
    ((!α) -> β) -> ((α V (!α)) -> β),
    (!α) -> β,
    (α V (!α)) -> β,
    β
  )

  /**
    * α → β, α ⊢ β
    */
  def modusPonens(α: Expression, β: Expression): Proof = Proof(
    α -> β,
    α,
    β
  )

  /**
    * α → β, ¬β ⊢ ¬α
    */
  def modusTollens(α: Expression, β: Expression): Proof = Proof(
    (!β) -> (α -> (!β)),               // Axiom schema #1
    !β,                                // Hypothesis #2
    α -> (!β),                         // M.P. -1, -2
    (α -> β) -> ((α -> (!β)) -> (!α)), // Axiom schema #9
    α -> β,                            // Hypothesis #1
    (α -> (!β)) -> (!α),               // M.P. -1, -2
    !α                                 // M.P. -4, -1
  )

  /**
    * ⊢ (α | ¬α)
    */
  def tertiumNonDatur(α: Expression): Proof = {
    def part(a: Expression, b: Expression): Proof = (a -> b) +: contraposition(a, b)

    part(α, α V (!α)) ++ part(!α, α V (!α)) ++ // ..., ¬(α ∨ ¬α) → ¬α, ..., ¬(α ∨ ¬α) → ¬¬α
      List(
        ((!(α V (!α))) -> (!α)) ->
          (((!(α V (!α))) -> (!(!α))) -> (!(!(α V (!α))))), // Axiom schema #9
        (!(α V (!α))) -> (!α),                              // From part(α, α | !α)
        ((!(α V (!α))) -> (!(!α))) -> (!(!(α V (!α)))),     // M.P. part1, -1
        (!(α V (!α))) -> (!(!α)),                           // From part(!α, α | !α
        !(!(α V (!α))),                                     // M.P. part2, -1
        (!(!(α V (!α)))) -> (α V (!α)),                     // Axiom schema #10
        α V (!α)                                            // M.P. -2, -1
      )
  }

  /**
    * ⊢ (α → β) → (¬β → ¬α)
    */
  def contraposition(α: Expression, β: Expression): Proof =
    deduction(deduction((Context(α -> β, !β), modusTollens(α, β))))._2

  /**
    * Transforms `Γ, α ⊢ β` into `Γ ⊢ α → β`
    */
  def deduction(derivation: Derivation): Derivation = Deduction(derivation) match {
    case Some(x) => x
    case None    => throw new AssertionError("impossible deduction")
  }
}