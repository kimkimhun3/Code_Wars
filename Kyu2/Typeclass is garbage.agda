{-# OPTIONS --safe #-}

module AdHoc where

open import Data.Char
open import Data.String hiding (length)
open import Data.List
open import Data.Integer as I
open import Data.Nat
open import Agda.Builtin.Nat as N
open import Data.Bool as B
open import Agda.Builtin.String
open import Agda.Builtin.Char

record Eq {a} (T : Set a) : Set a where
  field
    _===_ : T → T → Bool

open Eq ⦃ ... ⦄ public

_=/=_ : ∀ {a} {T : Set a} ⦃ _ : Eq T ⦄ → T → T → Bool
a =/= b = not (a === b)

instance
  eqNat : Eq ℕ
  _===_ ⦃ eqNat ⦄ = N._==_

instance
  eqInt : Eq ℤ
  _===_ ⦃ eqInt ⦄ = eqIntDef
    where
      eqIntDef : ℤ → ℤ → Bool
      eqIntDef (+ a) (+ b) = a N.== b
      eqIntDef -[1+ a ] -[1+ b ] = a N.== b
      eqIntDef _ _ = false

instance
  eqChar : Eq Char
  _===_ ⦃ eqChar ⦄ a b = primCharToNat a N.== primCharToNat b

instance
  eqBool : Eq Bool
  _===_ ⦃ eqBool ⦄ a b = not (a xor b)

instance
  eqString : Eq String
  _===_ ⦃ eqString ⦄ = primStringEquality

instance
  eqList : ∀ {a} {T : Set a} ⦃ _ : Eq T ⦄ → Eq (List T)
  _===_ ⦃ eqList ⦄ [] [] = true
  _===_ ⦃ eqList ⦄ (x ∷ xs) (y ∷ ys) = (x === y) ∧ (xs === ys)
  _===_ ⦃ eqList ⦄ _ _ = false

-- write something
-- _===_ : A → A → Bool
-- _=/=_ : A → A → Bool
