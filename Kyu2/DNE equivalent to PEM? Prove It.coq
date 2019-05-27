Definition axiom_pem := forall (P : Prop), P \/ ~P.

Definition axiom_dne := forall (P : Prop), ~ ~P -> P.

Theorem excluded_middle_irrefutable: forall (P:Prop),
  ~ ~ (P \/ ~ P).
Proof.
  unfold not.
  intros P f.
  apply f.
  right. 
  intro P'.
  apply f.
  left.
  exact P'.
Qed.

Theorem from : axiom_dne -> axiom_pem.
Proof.
  unfold axiom_dne. unfold axiom_pem.
  intros H P.
  assert (~ ~ (P \/ ~P)). { apply excluded_middle_irrefutable. }
  apply H in H0. exact H0.
Qed.


Theorem to : axiom_pem -> axiom_dne.
Proof.
  unfold axiom_pem. unfold axiom_dne.
  intros H P.
  unfold not at 1.
  intros H0.
  specialize H with (P := P).
  destruct H as [H|H].
  - exact H.
  - apply H0 in H.
    destruct H.
Qed.
