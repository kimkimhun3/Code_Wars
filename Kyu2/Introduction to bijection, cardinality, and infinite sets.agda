{-# OPTIONS --safe #-}
module Iso-properties where

open import Function

open import Iso
open _⇔_

-- Task 0 : Example of _⇔_ in finite sets
-- Task 0-1. Find a bijection between Bool and Bit. (provided for you as an example)
data Bool : Set where
  true false : Bool
  
data Bit : Set where
  0b 1b : Bit

Bool→Bit : Bool → Bit
Bool→Bit false = 0b
Bool→Bit true = 1b

Bit→Bool : Bit → Bool
Bit→Bool 0b = false
Bit→Bool 1b = true

Bool→Bit→Bool : ∀ (b : Bool) → Bit→Bool (Bool→Bit b) ≡ b
Bool→Bit→Bool true = refl
Bool→Bit→Bool false = refl

Bit→Bool→Bit : ∀ (b : Bit) → Bool→Bit (Bit→Bool b) ≡ b
Bit→Bool→Bit 0b = refl
Bit→Bool→Bit 1b = refl

Bool⇔Bit : Bool ⇔ Bit
Bool⇔Bit = Bijection Bool→Bit Bit→Bool Bool→Bit→Bool Bit→Bool→Bit


--------------------------------------------------------------------
-- Task 1 : General properties of ⇔
-- Task 1-1. Prove that any set has the same cardinality as itself.
⇔-refl : ∀ {A : Set} → A ⇔ A
⇔-refl = Bijection id id (λ _ → refl) λ _ → refl

-- Task 1-2. Prove that _⇔_ relation is symmetric.
⇔-sym : ∀ {A B : Set} → A ⇔ B → B ⇔ A
⇔-sym (Bijection A→B B→A A→B→A B→A→B) = Bijection B→A A→B B→A→B A→B→A

-- Task 1-3. Prove that _⇔_ relation is transitive.
⇔-trans : ∀ {A B C : Set} → A ⇔ B → B ⇔ C → A ⇔ C
⇔-trans (Bijection A→B B→A A→B→A B→A→B) (Bijection B→C C→B B→C→B C→B→C) =
  Bijection (B→C ∘ A→B) (B→A ∘ C→B)
            (λ a → trans (cong B→A (B→C→B _)) (A→B→A a))
            (λ c → trans (cong B→C (B→A→B _)) (C→B→C c))

-- Task 1-4. Prove the following statement:
--   Given two functions A→B and B→A, if A→B→A is satisfied and B→A is injective, A ⇔ B.
bijection-alt :
  ∀ {A B : Set} →
  (A→B : A → B) →
  (B→A : B → A) →
  (∀ a → B→A (A→B a) ≡ a) →
  (∀ b b' → B→A b ≡ B→A b' → b ≡ b') →
  A ⇔ B
bijection-alt A→B B→A A→B→A B→A-inj =
  Bijection A→B B→A A→B→A (λ b → B→A-inj (A→B (B→A b)) b (A→B→A (B→A b)))

--------------------------------------------------------------------
-- Task 2 : ⇔-relations between ℕ and various supersets of ℕ

-- Task 2-1. Prove that ℕ has the same cardinality as ℕ+1.
ℕ⇔ℕ+1 : ℕ ⇔ ℕ+1
ℕ⇔ℕ+1 = Bijection (λ { zero → null ; (suc x) → nat x})
                   (λ { null → zero ; (nat x) → suc x})
                   (λ { zero → refl ; (suc a) → refl})
                   (λ { null → refl ; (nat x) → refl})

sucnn : ℕ+ℕ → ℕ+ℕ
sucnn (left x) = left (suc x)
sucnn (right x) = right (suc x)


-- Task 2-2. Prove that ℕ has the same cardinality as ℕ+ℕ.
ℕ⇔ℕ+ℕ : ℕ ⇔ ℕ+ℕ
ℕ⇔ℕ+ℕ = Bijection to from tofrom fromto
  where
  to : ℕ → ℕ+ℕ
  to 0 = left 0
  to 1 = right 0
  to (suc (suc n)) = sucnn $ to n

  from : ℕ+ℕ → ℕ
  from (left 0) = 0
  from (left (suc x)) = suc $ suc $ from $ left x
  from (right 0) = 1
  from (right (suc x)) = suc $ suc $ from $ right x

  tofrom : ∀ a → from (to a) ≡ a
  tofrom 0 = refl
  tofrom 1 = refl
  tofrom (suc (suc a))
    with to a    | inspect to a
  ...  | left n  | [ p ] rewrite sym p | tofrom a = refl
  ...  | right n | [ p ] rewrite sym p | tofrom a = refl

  fromto : ∀ a → to (from a) ≡ a
  fromto (left zero) = refl
  fromto (left (suc x)) rewrite fromto (left x) = refl
  fromto (right zero) = refl
  fromto (right (suc x)) rewrite fromto (right x) = refl
