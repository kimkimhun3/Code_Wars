import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringJoiner;

 public class GerrymanderSolver {

    private static final String LINE_DELIM = "\n";
    private static final Character VOTES_FOR_MY_CANDIDATE = 'O';

    public static String gerrymander(String voterRegion) {
      // First read the input data
      List<Voter> voters = new ArrayList<>(parseVoters(voterRegion));

      // Then look for gerrymandering solutions
      return exploreSolutions(voters);
    }

    /**
     * @param voterRegion Five lines of five characters each. Each character is a
     *                    voter - who either votes for the desired candidate or not.
     * @return A set of all the voters in the input region
     */
    private static Set<Voter> parseVoters(String voterRegion) {
      Set<Voter> voters = new HashSet<>();

      String[] regionLines = voterRegion.split(LINE_DELIM);
      for (int row = 0; row < regionLines.length; row++) {
        String voterRow = regionLines[row];
        for (int col = 0; col < voterRow.length(); col++) {
          Voter voter = new Voter(voterRow.charAt(col), row, col);
          voters.add(voter);
        }
      }

      return voters;
    }

    /**
     * @param winningDistricts The set of independent winning districts we're
     *                         building
     * @param currentDistrict  Current potential winning district we're building
     * @param voterPool        Our pool of voters to draw from
     * @param voterIndex       Index of the next voter to test
     */
    private static void buildWinningDistricts(Set<District> winningDistricts, District currentDistrict,
        List<Voter> voterPool, int voterIndex) {
      // A winning district is one in which we have at least 3 voters for our
      // candidate
      if (currentDistrict.size() >= 3) {
        if (currentDistrict.isContiguous()) {
          if (currentDistrict.isFull()) {
            if (currentDistrict.stream().allMatch(Voter::votesForMe)) {
              // This is a failed state where all voters vote for our candidate. If we have a
              // district of 5 voters, that would only leave 5 remaining voters for me, which
              // cannot create two more districts of 3 voters.
              return;
            }
          }
          // success - this is a valid potential won district
          winningDistricts.add(currentDistrict);
          return;
        }
        if (currentDistrict.isFull()) {
          // failure since this district is full but not contiguous
          return;
        }
      }

      // Test adding remaining voters to this district
      List<Voter> nextVoterPool = new ArrayList<>(voterPool);
      for (int nextVoterIndex = voterIndex; nextVoterIndex < voterPool.size(); nextVoterIndex++) {
        Voter nextVoter = nextVoterPool.remove(nextVoterIndex);
        // We need at least 3 voters who vote for our candidate
        if (nextVoter.votesForMe() || currentDistrict.size() >= 3) {
          District modifiedDistrict = new District(currentDistrict);
          modifiedDistrict.add(nextVoter);
          buildWinningDistricts(winningDistricts, modifiedDistrict, nextVoterPool, nextVoterIndex);
        }
        nextVoterPool.add(nextVoterIndex, nextVoter);
      }
    }

    /**
     * @param allVoters The set of voters to search for gerrymandering
     * @return A string representation of the district layout that will gerrymander
     *         these voters, or null if this configuration can not be gerrymandered.
     */
    private static String exploreSolutions(List<Voter> allVoters) {
      // 1. Find all the possible district configurations that would be won by my
      // candidate
      Set<District> winningDistrictSet = new HashSet<>();
      buildWinningDistricts(winningDistrictSet, new District(), allVoters, 0);

      try {
        List<District> winningDistricts = new ArrayList<>(winningDistrictSet);
        List<Voter> availableVoters = new ArrayList<>(allVoters);
        // 2. For our candidate to win we need to win 3 of 5 districts. So now we look
        // at all the ways we can combine 3 of the winning districts found above.
        for (int i = 0; i < winningDistricts.size(); i++) {
          District firstDistrict = winningDistricts.get(i);
          availableVoters.removeAll(firstDistrict);
          findSolution(new ArrayList<>(), firstDistrict, availableVoters, winningDistricts, i, 0, false);
          availableVoters.addAll(firstDistrict);
        }
        // If we've made it through all the winning districts and haven't found a
        // gerrymandering solution then there is no solution
      } catch (GerrymanderingFoundException sfe) {
        // Found a solution - report it and get out of here
        return buildSolutionString(sfe.gerrymanderedDistricts);
      }

      // Didn't find a solution so we return null
      return null;
    }

    /**
     * This is actually a two-part solution algorithm. First we iteratively try to
     * build up districts we know are winning solutions. Once we have 3 of these
     * winning districts, then we try to build two more contiguous districts.
     *
     * @param currentSolution      The list of districts which, together, form a
     *                             gerrymandering configuration
     * @param currentDistrict      The current district we're adding to
     * @param voterPool            Our remaining voters who have not been placed in
     *                             a district
     * @param winningDistricts     The list of all independent winning district
     *                             configurations
     * @param winningDistrictIndex Index of our last used winning district
     * @param voterIndex           Index of the last tested voter
     * @param usedTenthVote        Since we want 3 districts of 3 voters for our
     *                             candidate, that only leaves one other voter for
     *                             our candidate to place. When this flag is true,
     *                             we can't use any more voters for our candidate.
     */
    private static void findSolution(List<District> currentSolution, District currentDistrict, List<Voter> voterPool,
        List<District> winningDistricts, int winningDistrictIndex, int voterIndex, boolean usedTenthVote) {
      // Recursion terminal check
      if (currentDistrict.isFull()) {
        if (currentDistrict.isContiguous()) {
          // success - add the district to our solution set
          currentSolution.add(currentDistrict);

          // If we have fewer than 3 districts we keep exploring the winning district set
          if (currentSolution.size() < 3) {
            for (int nextWinningDistrictIndex = winningDistrictIndex; nextWinningDistrictIndex < winningDistricts
                .size(); nextWinningDistrictIndex++) {
              District nextDistrict = winningDistricts.get(nextWinningDistrictIndex);
              // If the current voter pool does NOT contain all the voters in the next
              // district, then that means the next district overlaps with existing districts
              // in our solution
              if (voterPool.containsAll(nextDistrict)) {
                List<Voter> nextVoterPool = new ArrayList<>(voterPool);
                // Update the voter pool
                nextVoterPool.removeAll(nextDistrict);
                // Recurse
                findSolution(currentSolution, nextDistrict, nextVoterPool, winningDistricts, nextWinningDistrictIndex,
                    0, usedTenthVote);
                // Restore voter pool
                nextVoterPool.addAll(nextDistrict);
              }
            }
          } else {
            if (currentSolution.size() == 3) {
              // When we have 3 districts we a set of districts that will win the region for
              // our candidate. So now we have to make 2 more districts from the remaining
              // individuals.
              List<Voter> nextVoterPool = new ArrayList<>(voterPool);
              District nextDistrict = new District();
              // All the voters have to be placed so it doesn't matter who we start with for
              // this district
              Voter first = nextVoterPool.remove(0);
              // At this point, our voter pool has had at least 9 voters for my candidate
              // removed, so we don't have to check the usedTenthVote flag any more
              nextDistrict.add(first);
              findSolution(currentSolution, nextDistrict, nextVoterPool, winningDistricts, winningDistrictIndex, 0,
                  usedTenthVote);
            } else if (currentSolution.size() == 4) {
              // We only have 5 voters left so we can just make a district from them
              District nextDistrict = new District();
              nextDistrict.addAll(voterPool);
              findSolution(currentSolution, nextDistrict, Collections.emptyList(), winningDistricts,
                  winningDistrictIndex, 0, usedTenthVote);
            } else {
              // If our solution has 5 districts, then we found a complete gerrymandering
              // configuration
              throw new GerrymanderingFoundException(currentSolution);
            }
            // If we have more than 4 districts in our solution, we need to m
          }
          // We didn't find a complete solution including this district, so we remove it
          currentSolution.remove(currentDistrict);
        }
        return;
      }

      // Iterate through our voter pool, recursively adding to the current district
      List<Voter> nextVoterPool = new ArrayList<>(voterPool);
      for (int nextVoterIndex = voterIndex; nextVoterIndex < voterPool.size(); nextVoterIndex++) {
        Voter nextVoter = nextVoterPool.remove(nextVoterIndex);
        if (!nextVoter.votesForMe() || !usedTenthVote) {
          // We can only take one more voter for our candidate beyond the 9 we know are in
          // our winning district configurations
          if (nextVoter.votesForMe()) {
            usedTenthVote = true;
          }

          District modifiedDistrict = new District(currentDistrict);
          modifiedDistrict.add(nextVoter);
          findSolution(currentSolution, modifiedDistrict, nextVoterPool, winningDistricts, winningDistrictIndex,
              nextVoterIndex, usedTenthVote);
          modifiedDistrict.remove(nextVoter);

          // Restore usedTenthVote state
          if (nextVoter.votesForMe()) {
            usedTenthVote = false;
          }
        }
        // Restore voter pool
        nextVoterPool.add(nextVoterIndex, nextVoter);
      }
    }

    /**
     * @param gerrymanderedDistricts The set of gerrymandered districts
     * @return A string representation of the given solution, converted back to a
     *         5x5 grid
     */
    private static String buildSolutionString(Collection<District> gerrymanderedDistricts) {
      int[][] region = new int[5][5];

      // Assing a district number to each voter while filling the region arrays
      int districtNumber = 1;
      for (District district : gerrymanderedDistricts) {
        for (Voter voter : district) {
          region[voter.x][voter.y] = districtNumber;
        }
        districtNumber++;
      }

      // Convert the region arrays to a string
      StringJoiner joiner = new StringJoiner("\n");
      for (int row = 0; row < region.length; row++) {
        StringBuilder rowString = new StringBuilder();
        for (int col = 0; col < region[row].length; col++) {
          rowString.append(region[row][col]);
        }
        joiner.add(rowString.toString());
      }

      return joiner.toString();
    }

    /**
     * This is a total abuse of exception handling but allows a simplification of
     * the buildDistricts recursion. If we find evidence of gerrymandering we can
     * just throw this exception and stop searching.
     */
    private static class GerrymanderingFoundException extends RuntimeException {

      private List<District> gerrymanderedDistricts;

      public GerrymanderingFoundException(List<District> currentSolution) {
        this.gerrymanderedDistricts = currentSolution;
      }
    }

    /**
     * Data structure representing an individual voter, who votes either for my
     * candidate or another
     */
    private static class Voter {

      private final Character votesFor;
      private final int x;
      private final int y;

      /**
       * @param votesFor Character representing who this voter votes for
       * @param x        Row for this voter in the overall region
       * @param y        Column for this voter in the overall region
       */
      public Voter(Character votesFor, int x, int y) {
        this.votesFor = votesFor;
        this.x = x;
        this.y = y;
      }

      /**
       * @return true iff this voter votes for my character
       */
      public boolean votesForMe() {
        return GerrymanderSolver.VOTES_FOR_MY_CANDIDATE == votesFor;
      }

      /**
       * @param other Voter to compare
       * @return The manhattan distance from this voter to the other
       */
      public int distanceFrom(Voter other) {
        return Math.abs(y - other.y) + Math.abs(x - other.x);
      }

      @Override
      public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(x);
        builder.append(".");
        builder.append(y);
        builder.append(".vote=");
        builder.append(votesFor);
        return builder.toString();
      }

    }

    /**
     * Data structure for a set of up to 5 voters. A district is valid if the voters
     * are contiguous. A district is won by the candidate with a majority of votes
     * (3 of 5)
     */
    private static class District extends HashSet<Voter> {

      // The remaining distance is how far away a new voter can be to be added to this
      // district
      // For example if the district contains an individual at 1,1 and 1,3 we have 2
      // remaining distance
      // If we have individuals at 1,1 - 1,2 and 1,3 we also have 2 remaining distance
      private static final int INITIAL_DISTRICT_DISTANCE = 4;
      private int distanceRemaining;

      public District() {
        super();
        distanceRemaining = INITIAL_DISTRICT_DISTANCE;
      }

      /**
       * Copying constructor
       */
      public District(District currentDistrict) {
        this();
        currentDistrict.forEach(this::add);
      }

      public boolean isFull() {
        return size() == 5;
      }

      public boolean isContiguous() {
        updateTotalDistance();
        return distanceRemaining <= (4 - (size() - 1)) && distanceRemaining >= 0;
      }

      private void updateTotalDistance() {
        distanceRemaining = INITIAL_DISTRICT_DISTANCE;
        if (size() < 2) {
          // If we have 0 or 1 individuals in this district we haven't consumed any
          // distance yet
          return;
        }

        List<Integer> distances = new ArrayList<>();
        List<Voter> copy = new ArrayList<>(this);
        Set<Voter> placed = new HashSet<>();
        placed.add(copy.remove(0));
        final int count = copy.size();
        // Iteratively add the closest individual from our pool of voters
        for (int i = 0; i < count; i++) {
          Voter closest = findClosest(placed, copy);
          // The distance from the closest voter to this district is how much district
          // distance we have to spend to add that voter
          distances.add(minimumDistanceFrom(placed, closest));
          placed.add(closest);
          copy.remove(closest);
        }

        // Use the distances compute above to update our distanceRemaining stat
        for (int i = 0; i < distances.size(); i++) {
          distanceRemaining -= distances.get(i);
        }
      }

      /**
       * Helper method to compute the minimum distance from one voter to a set of
       * another
       */
      private Integer minimumDistanceFrom(Set<Voter> placed, Voter other) {
        if (placed.isEmpty()) {
          // First placed citizen doesn't affect distance
          return 0;
        }
        int dist = Integer.MAX_VALUE;
        for (Voter voter : placed) {
          dist = Math.min(voter.distanceFrom(other), dist);
          // Can't get closer than this
          if (dist == 1)
            break;
        }
        return dist;
      }

      /**
       * @return The Voter in copy closest to the set of placed voters
       */
      private Voter findClosest(Set<Voter> placed, List<Voter> copy) {
        int minDist = Integer.MAX_VALUE;
        Voter closest = null;
        for (Voter v : copy) {
          int dist = minimumDistanceFrom(placed, v);
          if (dist < minDist) {
            minDist = dist;
            closest = v;
          }
        }
        return closest;
      }
    }
  }
