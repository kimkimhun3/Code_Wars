import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Stack;
import java.util.HashMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;



class InvalidBond      extends RuntimeException { InvalidBond      (String msg) { super(msg); } }
class LockedMolecule   extends RuntimeException { LockedMolecule   (String msg) { super(msg); } }
class UnlockedMolecule extends RuntimeException { UnlockedMolecule (String msg) { super(msg); } }
class EmptyMolecule    extends RuntimeException { EmptyMolecule    (String msg) { super(msg); } }



class Atom implements Comparable<Atom> {
    
    final protected static String[] ATOMS    = {"C","H","O",  "B","Br", "Cl","F", "Mg", "N","P",  "S"};        // Ordered consistently with the raw formula
    final private static   int[]    VALENCES = { 4,  1,  2,    3,   1,    1,  1,    2,   3,  3,    2 };
    final private static   double[] WEIGHTS  = {12,  1, 16, 10.8,  80, 35.5, 19,  24.3, 14, 31, 32.1 };
    
    final private static Map<String,Double>  A_WEIGHT  = IntStream.range(0, VALENCES.length).boxed().collect(Collectors.toMap(n -> ATOMS[n], n -> WEIGHTS[n]));
    final private static Map<String,Integer> VALENCE   = IntStream.range(0, VALENCES.length).boxed().collect(Collectors.toMap(n -> ATOMS[n], n -> VALENCES[n]));
    final private static Map<String,Integer> STR_ORDER = IntStream.range(0, VALENCES.length).boxed().collect(Collectors.toMap(n -> ATOMS[n], n -> ATOMS[n].equals("H") ? ATOMS.length : n));
    
    
    public int id;
    public String element;
    protected boolean isLocked;
    protected List<Atom> bonds;              // Atoms bonded to the current instance
    
    
    public Atom(String elt, int id_) { 
        bonds = new ArrayList<Atom>();
        isLocked = false;
        element = elt; 
        id = id_;
    }
    
    
    @Override public int hashCode() { return id; }
    
    @Override public boolean equals(Object other) {
        if (other != null && other instanceof Atom) {
            Atom that = (Atom) other;
            return id == that.id;
        }
        return false; 
    }
    
    @Override public int compareTo(Atom other) {
        int diff = STR_ORDER.get(element) - STR_ORDER.get(other.element);
        return diff != 0 ? diff : id - other.id;
    }
    
    @Override public String toString() {
        if (!isLocked) Collections.sort(bonds);                                            // Sort the bonds list so that all atoms are in the correct order for the toString operation.
                                                                                           //   Sorting only needed if the Molecule/Atom isn't locked.
        String s = bonds.stream().map( b -> b.element + (b.isH() ? "" : b.id) ).collect(Collectors.joining(","));
        return String.format("Atom(%s.%d%s)", element, id, s.isEmpty() ? "" : ": " + s);
    }
    
    public boolean isH()         { return element.equals("H");   }
    public int     getValence()  { return VALENCE.get(element);  }
    public double  getAW()       { return A_WEIGHT.get(element); }
    
    public void cut(Atom other)  { bonds.remove(other); }
    
    public void unlock(int newId){
        id = newId;
        isLocked = false;
        bonds = bonds.stream().filter( at -> !at.isH() ).collect(Collectors.toList());      // Remove all the hydrogens while unlocking the atom instance
    }
    
    public void bondTo(Atom other) {
        if (bonds.size() >= VALENCE.get(element)) throw new InvalidBond("Invalid bond creation: the atom would exceed its valence number (current: " + this + ", tried to bond to: " + other + ")");
        if (this.equals(other))                   throw new InvalidBond("Invalid bond creation: an atom cannot bond to itself: " + this);
        bonds.add(other);
    }
    
    public void mutate(String elt) {
        if (bonds.size() > VALENCE.get(elt))     throw new InvalidBond(String.format("Cannot mutate %s to %s: too many bonds on the current atom.", this, elt));
        element = elt;
    }
}



class Molecule {
    
    
    /* ****************************
     *   PROPERTIES / CONSTRUCTOR
     * ****************************/
    private String  name      = "",
                    formula   = "";
    private boolean isMutable = true;
    private double  mw        = 0;
    private List<Stack<Atom>> branches = new ArrayList<Stack<Atom>>();
    private Stack<Atom>       atoms    = new Stack<Atom>();
    
    
    public Molecule(String... s) { if (s.length != 0) name = s[0]; }
    
    
    
    /* *********************
     *        GETTERS
     * *********************/
    public List<Atom> getAtoms()            { return atoms; }
    public String     getName()             { return name;  }
    public String     getFormula()          { raiseIfIsMutableIs(true); return formula; }
    public double     getMolecularWeight()  { raiseIfIsMutableIs(true); return mw; }
    
    

    /* *********************
     *      UTILITIES
     * *********************/
    private void raiseIfIsMutableIs(boolean shouldNotBe) {
        if (isMutable == shouldNotBe) {
            if (isMutable) throw new UnlockedMolecule("This molecule isn't finished yet: not reliable information"); 
            else           throw new LockedMolecule(  "This molecule is locked, create a new one"); 
        }
    }
    
    @Override public String toString()       { return name + ", " + formula; }
    
    private Atom getInBranch(int nc, int nb) { return branches.get(nb-1).get(nc-1); }

    private Atom createAtom(String elt) {
        atoms.push(new Atom(elt, atoms.size()+1));                      // Create a new atom with the right current id number and add it to the stack of atoms
        return atoms.peek();                                            // Return the last created atom
    }

    private void createBond(Atom a1, Atom a2) {
        
        boolean wasBonded = false;                                      // Bonds need to bidirectional, so keep track allows to know if a first bond has been created between the current atoms
        for (Atom[] inp: new Atom[][] {{a1, a2}, {a2, a1}}) {           // Will link the two atoms in both directions
            Atom a = inp[0], b = inp[1];
            try {
                a.bondTo(b);
                wasBonded = true;
            } catch (InvalidBond eIB) {
                if (wasBonded) a1.cut(a2);                              // "wasBonded = true": error while bonding in the second direction, so have to remove the bond created in the "first" direction
                throw eIB;
            }
        }
    }
    
    
    
    /* **********************
     *    REQUIRED METHODS
     * **********************/
    public Molecule brancher(int...nCbranches) { 
    raiseIfIsMutableIs(false);
        
        for (int nC: nCbranches){
            Stack<Atom> lst = new Stack<Atom>();                        // Prepare the new branch
            branches.add(lst);
            
            Atom at = null, prev = null;                                // Keep track of the current atom and the previous one
            for (int i = 0 ; i < nC ; i++) {
                at = createAtom("C");                                   // Create the new carbon atom (adding it to the list/stack of all the atoms)
                if (prev != null) createBond(prev, at);                 // If the previous atom exists, bond it to the current one
                lst.push(at);                                           // Add the new atom to the branch stack
                prev = at;                                              // "one step forward"
            }
        }
        return this;
    }
    
    
    public Molecule bounder(T...args) {
    raiseIfIsMutableIs(false);
        
        for (T t: args) 
            createBond(getInBranch(t.c1, t.b1), getInBranch(t.c2, t.b2));
        return this;
    }
    
    
    public Molecule mutate(T...args) { 
    raiseIfIsMutableIs(false);
        
        for (T t: args)
            getInBranch(t.nc, t.nb).mutate(t.elt);
        return this;
    }
    
    
    public Molecule add(T...args) { 
    raiseIfIsMutableIs(false);
        
        for (T t: args) {
            try {
                createBond( getInBranch(t.nc, t.nb), createAtom(t.elt));
            } catch (InvalidBond eIB) {
                atoms.pop();                                            // Remove the current new atom from the stack of atoms
                throw eIB;
            }
        }
        return this;
    }

    
    public Molecule addChaining(int nc, int nb, String...elts) {
    raiseIfIsMutableIs(false);
        
        Atom last  = atoms.peek(),                                      // Last atom present in the stack before addition of the chain (needed to know where to stop while removing invalid chain)
             start = getInBranch(nc, nb),                               // Atom on which the chain is added (needed to cut the bond with the first atom of the chain if invalid)
             at    = start,                                             // "at" will keep track of the last added atom in the chain
             newAt = null;                                              // Current instance to add at this level in the chain
        
        try {
            for (String elt: elts) {                                    // Add atoms to the chain while no error encountered
                newAt = createAtom(elt);
                createBond(at, newAt);
                at = newAt;                                             // "one step forward"
            }
        } catch (InvalidBond eIB) {
            while (!atoms.peek().equals(last)) newAt = atoms.pop();     // If error, remove all the atoms of the chain, keeping track of the last removed atom ("newAt", here)
            start.cut(newAt);                                           // Cut the bond in the "start" instance
            throw eIB;                                                  // "Overthrow" the InvalidBond exception
        }
        return this;
    }
    
        
    public Molecule closer() {
    raiseIfIsMutableIs(false);
        
        for (Atom at: new ArrayList<Atom>(atoms)) {
            int nHtoAdd = at.getValence()-at.bonds.size();               // Number of missing hydrogens on the current atom
            for (int nH = 0 ; nH < nHtoAdd ; nH++)
                createBond(at, createAtom("H"));                         // Add all missing hydrogens
            at.bonds.sort(null);                                         // Sort the atoms bonded to the current "at" instance, accordingly to the toString() required order
            at.isLocked = true;                                          // Lock the current "at" atom (forbid the sorting step in Atom.toString() to gain a bit of performances)
        }
        
        final Map<String,Long> counter = atoms.stream()                  // Count all the atoms of the molecule
                                              .map( at -> at.element)
                                              .collect(Collectors.groupingBy(s->s, HashMap<String,Long>::new, Collectors.counting()));
        formula = Arrays.stream(Atom.ATOMS)                              // Define the raw formula...
                        .filter( s -> counter.containsKey(s) )           // ... With only atoms appearing in the current molecule...
                        .map( sym -> sym + (counter.get(sym) == 1 ? "" : counter.get(sym)) )    // ... and not displaying "ones"
                        .collect(Collectors.joining());
        mw = atoms.stream().mapToDouble( at -> at.getAW() ).sum();       // Compute molecular weight
        
        isMutable = false;                                               // Lock the molecule
        return this;
    }
    
    
    public Molecule unlock() {
        isMutable = true;                                                // Make the molecule mutable again
        
        Stack<Atom> newStk = new Stack<Atom>();
        int i = 1;
        for (Atom at: atoms) {                                           // Remove hydrogens, unlocking the atoms and redefining their id number
            if (!at.isH()) {
                newStk.push(at);
                at.unlock(i++);
            }
        }
        atoms = newStk;                                                  // Reassign the atoms Stack
        
        List<Stack<Atom>> newBranches = new ArrayList<Stack<Atom>>();    // Clean the branches structure from hydrogens and possible empty branches
        for (Stack<Atom> b: branches) {
            Stack<Atom> newB = b.stream().filter( at -> !at.isH() ).collect(Stack::new, Stack::push, Stack::addAll);
            if (!newB.isEmpty()) newBranches.add(newB);
        }
        if (newBranches.isEmpty()) throw new EmptyMolecule("");          // If no branches left, throw an exception
        branches = newBranches;                                          // Reassign the branches
        
        return this;
    }
    
}
