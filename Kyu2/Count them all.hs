{-# LANGUAGE FlexibleInstances, UndecidableInstances #-}
{-# LANGUAGE ScopedTypeVariables #-}
module Counting where
import Counting.Preloaded
import Control.Monad (liftM2)
import Data.Function (fix)
import Data.List (find)
import Data.Maybe (fromJust)
import Data.Proxy
import Data.Void
import Data.Functor.Const
import Data.Functor.Identity
import Data.Functor.Sum
import Data.Functor.Product
import Data.Functor.Compose

import Debug.Trace (trace)

{- in Preloaded:
data Nat = Z | S Nat deriving (Show, Eq, Ord)
instance Num Nat -- so (2 :: Nat) == S (S Z)
instance Enum Nat -- so ([0..3] :: [Nat]) == [Z, S Z, S (S Z)]
instance Real Nat
instance Integral Nat -- so (2 ^ 3 :: Nat) == S (S (S (S (S (S (S (S Z)))))))
-}

newtype Count x = Count { getCount :: Nat } deriving (Show, Eq, Ord)

-- | helper functions
mapC :: (Nat -> Nat) -> Count a -> Count b
mapC f = Count . f . getCount

liftC2 :: (Nat -> Nat -> Nat) -> Count a -> Count b -> Count c
liftC2 f (Count a) (Count b) = Count (f a b)

coerceC :: Count a -> Count b
coerceC = Count . getCount

-- | Countable
class Countable c where
  count :: Count c
  -- if you are using `Proxy` implement `count` from `count'` and vice versa
  -- count' :: Proxy c -> Count c
  -- count' = error "from count"

instance Countable Void where count = Count 0
instance Countable () where count = Count 1
instance Countable Bool where count = Count 2
instance Countable Nat where count = Count (fix S)

-- | Factor
class Factor f where
  factor :: Count c -> Count (f c)
  -- factor' :: Proxy f -> Count c -> Count (f c) -- optional

instance (Factor f, Countable c) => Countable (f c) where
  count = factor $ count

instance Factor Maybe where factor = mapC S
instance Factor Identity where factor = mapC id
instance Factor Proxy where factor _ = Count 1
instance Factor Count where factor _ = coerceC (count :: Count Nat)
instance Factor [] where factor (Count a) = if a == 0 then Count 1 else coerceC (count :: Count Nat)
instance Countable c => Factor (Const c) where factor _ = coerceC (count :: Count c)
instance Countable c => Factor (Either c) where factor = liftC2 (+) (count :: Count c)
instance Countable c => Factor ((,) c) where factor = liftC2 (*) (count :: Count c)
instance Countable c => Factor ((->) c) where factor = liftC2 (flip (^)) (count :: Count c)
instance (Factor f, Factor g) => Factor (Sum f g) where factor (n :: Count c) = liftC2 (+) (factor n :: Count (f c)) (factor n :: Count (g c))
instance (Factor f, Factor g) => Factor (Product f g) where factor (n :: Count c) = liftC2 (*) (factor n :: Count (f c)) (factor n :: Count (g c))
instance (Factor f, Factor g) => Factor (Compose f g) where factor (n :: Count c) = coerceC (factor (factor n :: Count (g c)) :: Count (f (g c)))

-- | Listable
class Countable a => Listable a where
  list :: [a]
  -- list' :: Proxy a -> [a] -- optional
-- Data.List.genericLength (list :: [a]) `shouldBe` getCount (count :: Count a)

instance Listable Void where list = []
instance Listable () where list = [()]
instance Listable Bool where list = [True, False]
instance Listable Nat where list = [0..]

instance Listable c => Listable (Maybe c) where list = Nothing : (fmap Just (list :: [c]))
instance Listable c => Listable [c] where list = let rec e = e ++ rec (liftM2 (:) (list :: [c]) e) in rec [[]]
instance (Listable a, Listable b) => Listable (Either a b) where list = (map Left (list :: [a])) ++ (map Right (list :: [b]))
instance (Listable a, Listable b) => Listable (a, b) where list = liftM2 (,) (list :: [a]) (list :: [b])
instance (Eq a, Listable a, Listable b) => Listable (a -> b) where list = map (\r -> (\v -> snd $ fromJust $ find (\e -> fst e == v) (zip (list :: [a]) r))) $ foldr (\_ r -> (liftM2 (:) (list :: [b]) r)) [[]] (list :: [a])
