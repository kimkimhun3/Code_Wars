Require Import Arith.


Theorem invert : forall a b : nat, a + a = b + b -> a = b.
Proof. 
  intros a.
  induction a as [|a' IHa].
  - intros b H.
    simpl in H. 
    destruct b eqn:Eb.
    + reflexivity. 
    + discriminate H.
  - intros b H. 
    destruct b eqn: Eb.
    + discriminate H.
    + rewrite <- plus_n_Sm in H. rewrite <- plus_n_Sm in H.
      rewrite plus_comm in H. rewrite (plus_comm (S n) n) in H.
      rewrite <- plus_n_Sm in H. rewrite <- plus_n_Sm in H.
      injection H.
      intros Hyp.
      apply IHa in Hyp.
      rewrite Hyp.
      reflexivity. 
Qed.
