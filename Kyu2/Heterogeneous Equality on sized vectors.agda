{-# OPTIONS --safe #-}
module ++-Identity where

open import Data.Nat
open import Relation.Binary.HeterogeneousEquality
open import Data.Vec
open import Relation.Binary.PropositionalEquality renaming (refl to prefl; cong to pcong)

plus0 : ∀ n → n + 0 ≡ n
plus0 zero = prefl
plus0 (suc n) = pcong suc (plus0 n)

++-identityʳ : ∀ {n} {A : Set} (xs : Vec A n) → xs ++ [] ≅ xs
++-identityʳ {zero} {A} [] = refl
++-identityʳ {suc n} {A} (x ∷ xs) = icong (Vec A) (plus0 n) (x ∷_) (++-identityʳ xs)
