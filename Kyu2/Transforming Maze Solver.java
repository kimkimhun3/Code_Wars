import java.util.*;
import java.awt.Point;
import java.util.stream.*;


public class MazeSolver {
    
    final static private String[]  DIRS   = {"N",        "W",        "S",       "E"};
    final static private int[][]   CONFIG = {{8,-1,0,0}, {4,0,-1,1}, {2,1,0,2}, {1,0,1,3}};    // array of {step, dx, dy, i for "DIRS[i]"}
    final static private int[][][] SWARM  = IntStream                                          // array of arrays of {step, revStep, dx, dy, i for "DIRS[i]"}
          .range(0,16)
          .mapToObj( n -> Arrays.stream(CONFIG)
                                .filter( c -> (n & c[0]) == 0 )
                                .map(    c -> new int[] {c[0], getRevStep(c[0]), c[1], c[2], c[3]})
                                .toArray(int[][]::new) )
          .toArray(int[][][]::new);
    
    private static int getRevStep(int n) { return ((n<<2) + (n>>2)) & 15; }
    
    private int[][] maze;
    private int     lX,lY;
    private Point   ball, end;
    
    
    
    public MazeSolver(int[][] maze) {
        
        lX = maze.length;
        lY = maze[0].length;
        
        int[][] maz = Arrays.copyOf(maze, maze.length);
        for (int x=0 ; x<lX ; x++) {
            maz[x] = Arrays.copyOf(maz[x], lY);
            for (int y=0 ; y<lY ; y++) {
                if (maz[x][y] == -1) { ball = new Point(x,y); maz[x][y] = 0; }
                if (maz[x][y] == -2) { end  = new Point(x,y); maz[x][y] = 0; }
            }
        }
        this.maze = maz;
    }
    
    
    public List<String> solve() {
        
        Set<Point> bag = new HashSet<>(Arrays.asList(ball));
        
        List<List<Stack<StringBuilder>>> paths =
              IntStream.range(0,lX)
                       .mapToObj( x -> IntStream.range(0,lY).mapToObj( y -> new Stack<StringBuilder>()).collect(Collectors.toList()) )
                       .collect(Collectors.toList());
        
        int lastModifiedBagGen = -1;
        for (int nIter=0 ; true ; nIter++) {
            
            bag.stream().forEach( p -> paths.get(p.x).get(p.y).add(new StringBuilder()) );
            
            int oldBagSize = bag.size();
            while (true) {
                Set<Point> addToBag = new HashSet<>();
                
                for (Point p: bag) {
                    for (int[] swarmer: SWARM[maze[p.x][p.y]]) {
                        int step    = swarmer[0],
                            revStep = swarmer[1],
                            dx      = swarmer[2],
                            dy      = swarmer[3],
                            dir     = swarmer[4],
                            
                            a = p.x+dx,
                            b = p.y+dy;
                        
                        if (0 <= a && a < lX && 0 <= b && b < lY 
                                && paths.get(a).get(b).size() <= nIter 
                                && (revStep & maze[a][b]) == 0) {
                            
                            addToBag.add(new Point(a,b));
                            paths.get(a).set(b, paths.get(p.x).get(p.y).stream()
                                                                       .map( sb -> new StringBuilder(""+sb) )
                                                                       .collect(Stack::new, Stack::add, Stack::addAll) );
                            paths.get(a).get(b).peek().append(DIRS[dir]);
                            
                            if (end.equals(new Point(a,b))) {
                                return paths.get(a).get(b).stream().map(StringBuilder::toString).collect(Collectors.toList());
                            }
                        }
                    }
                }
                if (addToBag.isEmpty()) break;
                bag.addAll(addToBag);
            }
            
            if      (oldBagSize != bag.size())        lastModifiedBagGen = nIter;
            else if (nIter - lastModifiedBagGen > 4)  return null;
            
            for (int x=0 ; x<lX ; x++) for (int y=0 ; y<lY ; y++) {
                maze[x][y] = ((maze[x][y] << 1) + (maze[x][y] >> 3)) & 15;
            }
        }
    }
}
