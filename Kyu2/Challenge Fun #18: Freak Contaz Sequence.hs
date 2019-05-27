module FreakContazSequence (freakContazSequence) where

data Nat = Nat Int Int deriving (Show, Eq)

start :: Nat
start = Nat 1 1

mult :: Nat -> Int -> Nat
(Nat a b) `mult` c = Nat (a * c) (b * c)

division :: Nat -> Int -> Nat
(Nat a b) `division` c = Nat (div a c) (div b c)

addition :: Nat -> Int -> Nat
(Nat a b) `addition` c = Nat a (b + c)

subtraction :: Nat -> Int -> Nat
(Nat a b) `subtraction` c = Nat a (b - c)

eval :: Nat -> Int
eval (Nat a b) = if b == 1 then a + b else b

substitution :: Nat -> Nat -> Nat
(Nat a b) `substitution` (Nat c d) = Nat (a * c) (b * c + d)

inverse = inverse' 1

inverse' :: Int -> Int -> Int -> Int 
inverse' a b c = if mod (a * b) c == 1 then a else inverse' (a + 1) b c

r x@(Nat a b) = if b == 1 then Nat a (a + b) else x

next :: Nat -> Char -> Nat 
next x 'D' = x `mult` 3
next x@(Nat a b) 'U' = r $((y `mult` 3) `subtraction` 2) `division` 4
    where a' = mod a 4
          b' = mod b 4
          k  = mod (6 - b') 4
          c  = mod (k * inverse a' 4) 4
          y  = substitution (Nat 4 c) x
next x@(Nat a b) 'd' = r $ ((y `mult` 3) `addition` 1) `division` 2
    where a' = mod a 2
          b' = mod b 2
          k  = mod (3 - b') 2
          c  = mod (k * inverse a' 2) 2
          y  = substitution (Nat 2 c) x

longeval :: Nat -> [Char] -> Nat
longeval x [] = x
longeval x (y:ys) = longeval (next x y) ys

freakContazSequence :: String -> Int
freakContazSequence ys = let xs = reverse ys in eval $ longeval start xs
