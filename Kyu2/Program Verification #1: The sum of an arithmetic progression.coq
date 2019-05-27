From Coq Require Import Arith.
Require Import Preloaded.

(* Preloaded:
Require Import Arith.

Fixpoint arith_sum (n : nat) : nat :=
  match n with
  | 0 => 0
  | S m => n + arith_sum m
  end.

Definition arith_formula (n : nat) : nat := Nat.div2 (n * (n + 1)).
*)

(* I guess I messed up stupidly on how to use omega, or there's just something weird. 
In any case it's a BAD proof in terms of style or math.*)

Require Import Omega.

Lemma simple_arith (n : nat) : n * (n + 1) + 2 * (n + 1) = S n * (S n + 1).
Proof.
  replace (S n) with (n + 1).
  simpl. rewrite mult_plus_distr_r. repeat rewrite mult_plus_distr_l. omega.
  omega.
Qed.

Lemma n_mul_Sn_even (n : nat) : exists k, 2 * k = (n * (n + 1)).
Proof.
  induction n. 
  exists 0. auto.
  inversion IHn. exists (x + n + 1). rewrite <- (plus_assoc x n 1). rewrite mult_plus_distr_l.
  rewrite H. apply simple_arith.
Qed.

Theorem arith_eq (n : nat) : arith_formula n = arith_sum n.
Proof.
  induction n; auto.
  simpl. rewrite <- IHn. unfold arith_formula. rewrite plus_n_Sm.
  assert (Hn: exists k : nat, 2 * k = n * (n + 1)). exact (n_mul_Sn_even n). inversion Hn.
  assert (HSn : 2 * (x + n + 1) = S n * (S n + 1)).
  rewrite <- (plus_assoc x n 1). rewrite mult_plus_distr_l.
  rewrite H.
  replace (S n) with (n + 1).
  simpl. rewrite mult_plus_distr_r. repeat rewrite mult_plus_distr_l. omega.
  omega.
  rewrite <- H. rewrite <- HSn. repeat rewrite Nat.div2_double. omega.
Qed.
