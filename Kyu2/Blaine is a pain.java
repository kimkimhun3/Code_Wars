import java.awt.Point;
import java.util.*;
import java.util.stream.*;

public class Dinglemouse {

    public static int trainCrash(final String track, final String aTrain, final int aTrainPos, final String bTrain, final int bTrainPos, final int limit) {
        return new BlaimHim(track, aTrain, aTrainPos, bTrain, bTrainPos, limit).crash();
  }
}


class BlaimHim {
    
    final private static Map<Character, Point[][]> MOVES = new HashMap<Character,Point[][]>() {{            // Only for the corners
        put('/',  new Point[][] {new Point[] {new Point( 0,-1), new Point( 1,-1), new Point(1, 0)},         // Coming from up-right, goes down-left...
                                 new Point[] {new Point(-1, 0), new Point(-1, 1), new Point(0, 1)}});       // ...coming from down-left, goes up-right. Elt[0] when x-y > 0, elt[1] otherwise
        put('\\', new Point[][] {new Point[] {new Point( 0, 1), new Point( 1, 1), new Point(1, 0)},         // Coming from up-left, goes down-right...
                                 new Point[] {new Point(-1, 0), new Point(-1,-1), new Point(0,-1)}});       // ...coming from down-right, goes up-left. Elt[0] when x+y > 0, elt[1] otherwise
    }};
    final private static Map<Character, char[][]> TARGET_CHAR = new HashMap<Character,char[][]>() {{        // Corresponding target character, when leaving the "turn"
        put('/',  new char[][] {new char[] {'-', '/',  '|'}, new char[] {'|', '/',  '-'}});
        put('\\', new char[][] {new char[] {'-', '\\', '|'}, new char[] {'|', '\\', '-'}});
    }};
    
    private List<Train> trains;
    private Track[]     tracks;
    private int         limit;
    
    
    public BlaimHim(final String track, final String aTrain, final int aPos, final String bTrain, final int bPos, final int limit) {
        this.limit = limit;
        trackBuilder(track);                                                    // Build the tracks array
        trains = Arrays.asList(new Train(aPos, aTrain, tracks),                 // Build the trains list
                               new Train(bPos, bTrain, tracks));
    }
    
    
    private void trackBuilder(String track) {
        
        Stream.Builder<Track> sBuild = Stream.builder();                        // Accumulate the Track instances that represent the track path
        Map<Point,Track> toLink = new HashMap<>();                              // Prepare the archive to link the Track instances together
        char[][] arr = Arrays.stream(track.split("\n",-1))                      // Convert the track string to a char array to facilitate the path following
                              .map( s -> s.toCharArray() )
                              .toArray(char[][]::new);
        
        Point move = new Point(0,1),                                            // Starting moving direction
              pos  = new Point(0, IntStream.range(0,arr[0].length)              // Define the starting position
                                           .filter( n -> arr[0][n] != ' ' && arr[0][n] != '\n' )
                                           .findFirst()
                                           .getAsInt()),
              start = new Point(pos);
        
        for (int p = 0 ; true ; p++) {
            char  c = arr[pos.x][pos.y];
            Track t = new Track(pos.x, pos.y, c);
            
            if ("+XS".contains(""+c)) {
                if (toLink.containsKey(pos)) t = toLink.get(pos);               // If the current position has already been archived, will push a reference of the original Track object at this position in the array
                else                         toLink.put(pos, t);                // Archive the Track instance
            }
            sBuild.accept(t);                                                   // Push the Track instance in the builder
            
            if (p != 0 && (c == '/' || c == '\\')) {                            // Might need an update for the direction, here
                int   idx        = getIndexRelativeToMove(c, move);
                final Point   pp = new Point(pos);
                final Point[] m  = MOVES.get(c)[idx];
                final char[]  tc = TARGET_CHAR.get(c)[idx];
                move = IntStream.range(0,3)
                                .filter( i ->    0 <= pp.x+m[i].x && pp.x+m[i].x < arr.length
                                              && 0 <= pp.y+m[i].y && pp.y+m[i].y < arr[pp.x+m[i].x].length
                                              && ("XS"+tc[i]).contains( ""+arr[pp.x+m[i].x][pp.y+m[i].y] ) )
                                .mapToObj( i -> m[i] )
                                .findFirst()
                                .get();
            }
            pos = new Point(pos.x+move.x, pos.y+move.y);
            if (pos.equals(start)) break;
        }
        tracks = sBuild.build().toArray(Track[]::new);                          // Actually build the tracks array
    }
        
    private int getIndexRelativeToMove(char c, Point m) {                       // Determine the index to use in the constant map MOVES
        return c == '/' ? ( m.x-m.y > 0 ? 0:1 )
                        : ( m.x+m.y > 0 ? 0:1 );
    }
    
    
    public int crash() {
        for (int round = 0; round <= limit ; round++) {
            if (trains.stream().anyMatch( t -> t.checkEatItSelf()) || trains.get(0).checkCrashWith(trains.get(1)) )
                return round;
            trains.stream().forEach( t -> t.move() );
        }
        return -1;
    }
}


class Track extends Point {
    
    protected Track   linkedTo = null;
    protected boolean isStation;
    protected char    c;
    
    public Track(int x, int y, char c) {
        super(x,y);
        isStation = c=='S';
        this.c = c;
    }
    @Override public String toString() { return ""+c; }
}


class Train {
    
    protected Track[]    tracks;
    protected boolean    isXpress;
    protected int        pos, dir, len, delay = 0;
    protected char       c;
    protected Set<Track> occupy;
    
    public Train(int pos, String s, Track[] tracks) {
        this.pos      = pos;
        this.dir      = Character.isUpperCase(s.charAt(0)) ? -1 : 1;
        this.len      = s.length();
        this.isXpress = Character.toLowerCase(s.charAt(0)) == 'x';
        this.tracks   = tracks;
        this.c        = s.charAt(0);
        updateOccupy();
    }
    
    private void updateOccupy() {
        occupy = IntStream.range(0,len)
                          .map( x -> (pos - dir*x + tracks.length) % tracks.length )
                          .mapToObj( i -> tracks[i] )
                          .collect(Collectors.toSet());
    }
    public void move() {
        if (delay != 0) delay--;
        else {          pos   = (pos + dir + tracks.length) % tracks.length;
                        delay = tracks[pos].isStation && !isXpress ? len-1 : 0;
        }
        updateOccupy();
    }
    public boolean checkEatItSelf()        { return occupy.size() != len; }
    public boolean checkCrashWith(Train o) { return occupy.stream().anyMatch( t -> o.occupy.contains(t) ); }  
}
