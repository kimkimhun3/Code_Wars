{-# LANGUAGE RankNTypes, ScopedTypeVariables, TypeApplications #-}
module YonedaLemma where
import YonedaLemmaPreloaded
import Data.Functor.Identity
import Data.Functor.Contravariant
import Data.Void

-- Hom(a, b) ≡ all arrows/morphisms from object `a` to object `b`
-- in given category.
-- Hom(a, -) covariant functor:
type Hom a = (->) a

-- natural transformation from functor f to functor g:
type Nat f g = forall x. f x -> g x

-- in order to witness isomorphism
-- we should provide `to` and `from` such, that
-- to . from ≡ id[f a]
-- from . to ≡ id[Nat (Hom a) f]
to :: Functor f => Nat (Hom a) f -> f a
to f = f id

from :: Functor f => f a -> Nat (Hom a) f
from = flip fmap 


-- Hom(-, a) contravariant functor:
type CoHom a = Op a
{- NOTE:
Op a b = Op { getOp :: b -> a }

class Contravariant f where
  contramap :: (b -> a) -> f a -> f b
-}

to' :: Contravariant f => Nat (CoHom a) f -> f a
to' f = f (Op id)

from' :: Contravariant f => f a -> Nat (CoHom a) f
from' = flip (contramap . getOp)


-- now we will try to count the natural transformations

{- in Preloaded we have:
newtype Count x = Count { getCount :: Int } deriving (Show, Eq)
coerce :: Count a -> Count b
class Countable where count :: Count c
class Factor where factor :: Countable c => Count (f c)
instance (Factor f, Countable c) => Countable (f c) where count = factor
-}
-- | NOTE: from here onwards you should imagine `forall x` inside `Count (...)`,
-- | i. e., not `Count ((Numbers -> x) -> Maybe x)`, but `Count (forall x. (Numbers -> x) -> Maybe x)`
-- | we are unable to write it because GHC doesn't yet support impredicative polymorphism (see issue: https://www.codewars.com/kata/yoneda-lemma/discuss/haskell#5b0f4afd3aa7cf7eb100000e)

count1 :: forall f c x. (Functor f, Factor f, Countable c) => Count ((c -> x) -> f x)
count1 = coerce $ count @(f c)

count2 :: forall f c x. (Contravariant f, Factor f, Countable c) => Count ((x -> c) -> f x)
count2 = coerce $ count @(f c)
-- | TIP: you could use types `f`, `c` in RHS of count1 and count2
-- | (because of ScopedTypeVariables pragma and explicit forall)

-- and now i encourage you to count something on fingers ;)
data Numbers = One | Two | Three deriving (Show, Eq)

instance Countable Numbers where
  count = Count 3

challenge1 :: Count ((Numbers -> x) -> Maybe x)
challenge1 = Count $ 3 + 1
-- Maybe x has count of (count x + 1), result is the same as Count (Maybe Numbers)

--         :: Count ((Maybe Numbers -> x) -> Identity x)
challenge2 :: Count ((Maybe Numbers -> x) -> x)
challenge2 = coerce challenge1
-- Identity x has count of (count x), result is the same as Count (Identity (Maybe Numbers))
-- This is the same as Count (Maybe Numbers), which is the result of challenge1

challenge3 :: Count ((Numbers -> x) -> (Bool -> x))
challenge3 = Count $ 3^2
-- Count (a -> b) is b^a, so Count (Bool -> c) is (count c)^2, 
-- so we have Count (Bool -> Numbers) which is 3^2

{- Void is a data type without constructors, its declaration:
data Void
Predicate x = Predicate { getPredicate :: x -> Bool }
-- as you might have noticed, Predicate is Contravariant
-}
challenge4 :: Count ((x -> Void) -> Predicate x)
challenge4 = Count $ 2^0
-- Count (c -> Bool) is 2^(count c), 
-- so we have Count (Void -> Numbers) which is 2^0
-- (also Count (Void -> x) = 1 because Void is the initial object (up to isomorphism))

-- challenge5 :: Count (forall x. (x -> (forall y. (Bool -> y) -> (Numbers -> y))) -> (x -> Numbers))
challenge5 :: Count ((x -> ((Bool -> y) -> (Numbers -> y))) -> (x -> Numbers))
challenge5 = Count $ 3^(2^3)
--   Count ((x -> ((Bool -> y) -> (Numbers -> y))) -> (x -> Numbers))
-- = Count (((Bool -> y) -> (Numbers -> y)) -> Numbers)
-- = 3 ^ Count ((Bool -> y) -> (Numbers -> y))
-- = 3 ^ Count (Numbers -> Bool)
-- = 3 ^ (2 ^ 3)
