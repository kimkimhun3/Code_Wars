{-# OPTIONS --cubical --safe --without-K #-}
module JustBeInjective where

open import Cubical.Core.Everything renaming (_≡_ to _==_)
open import Cubical.Data.Unit
open import Cubical.Data.Empty
open import Maybe

just-neq-nothing : ∀ {A : Set} {a : A} → just a == nothing → ⊥
just-neq-nothing {A = A} {a = a} x = transport (cong f x) a
  where
    f : maybe A → Set
    f (just x) = A
    f nothing = ⊥

just-injective : ∀ {A : Set} (a b : A) → just a == just b → a == b
just-injective {A = A} a b p i = f (p i) (\j → (p (i ∧ j)))
  where
    f : (ma : maybe A) → just a == ma → A
    f (just x) _ = x
    f nothing p = ⊥-elim (just-neq-nothing p)
