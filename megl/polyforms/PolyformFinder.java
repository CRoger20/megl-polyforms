package megl.polyforms;

import java.util.HashSet;

/**
Contains static methods for finding a polyform with the most holes and fewest tiles,
as well as utilities to that end.
*/
public abstract class PolyformFinder {

    /**
    Stores every combination of tiles reached by the current searching method.
    */
    static HashSet<HashSet<Tile>> reachedCombinations;
    
    /**
    Iteratively makes every polyform with the given number of tiles (or possibly fewer) to find the optimal one.
    @param plane the Tessellation that is being searched
    @param tiles the number of tiles that you want in your polyforms
    @return the polyform with the most holes, and if tied, fewest tiles
    */
    public static HashSet<Tile> findHoleyestAnimal(Tessellation plane, int tiles){
        if(plane.tessellation.size() == tiles)
            throw new IllegalArgumentException("this many tiles will fill the entire plane, so it will never have a hole");
        HashSet<Tile> animal = new HashSet<Tile>(tiles);
        animal.add(plane.tessellation.get(1));
        reachedCombinations = new HashSet<HashSet<Tile>>();
        HashSet<Tile> temp = extendAnimal(plane, tiles, animal, plane.tessellation.get(1));
        reachedCombinations = new HashSet<HashSet<Tile>>();
        return temp;
    }
    
    /**
    The recursive helper for findHoleyestAnimal. Each layer makes every possible animal that is formed
    by extending from the specified Tile.
    
    TODO: modify this to account for polyforms that "turn arround", e.g. a shape like /|\
    
    @param plane the Tessellation that is being worked in
    @param tiles the target number of tiles
    @param animal the set of tiles in the tessellation that is already part of the animal
    @param expansionPoint the tile that was last added to the animal
    @return an animal with the most holes and fewest tiles
    */
    @SuppressWarnings("unchecked")
    private static HashSet<Tile> extendAnimal(Tessellation plane, int tiles, HashSet<Tile> animal, Tile expansionPoint){
        if(animal.size() == tiles || expansionPoint == null){
            return animal;
        }
        
        HashSet<Tile>[] options = (HashSet<Tile>[]) new HashSet[plane.p];
        
        HashSet<Tile> tempAnimal;
        
        for(int i = 0; i < options.length; i++){
            if(expansionPoint.adjacent[i] == null)
                continue;
            tempAnimal = (HashSet<Tile>) animal.clone();
            tempAnimal.add(expansionPoint.adjacent[i]);
            if(tempAnimal.size() > animal.size())
                if(!reachedCombinations.contains(tempAnimal)){
                    reachedCombinations.add(tempAnimal);
                    options[i] = extendAnimal(plane, tiles, tempAnimal, expansionPoint.adjacent[i]);
                }
        }
        
        int bestIndex = -1;
        int bestHoles = Polyform.numHoles(plane, animal);
        int tempHoles;
        for(int i = 0; i < options.length; i++){
            if(options[i] == null)
                continue;
            tempHoles = Polyform.numHoles(plane, options[i]);
            
            if(bestHoles < tempHoles){
                bestIndex = i;
                bestHoles = tempHoles;
            }
            else if(bestHoles == tempHoles){
                if(bestIndex == -1){
                    if(animal.size() > options[i].size()){
                        bestIndex = i;
                        bestHoles = tempHoles;
                    }
                }
                else {
                    if(options[bestIndex].size() > options[i].size()){
                        bestIndex = i;
                        bestHoles = tempHoles;
                    }
                }
            }
        }
        
        return (bestIndex == -1 ? animal : options[bestIndex]);
    }
    
    /**
    WIP way to search for optimal structure, like a breadth first search
    */
    public static HashSet<Tile> getHoleyest(Tessellation plane, int maxTiles){
        HashSet<Tile> base = new HashSet<Tile>(1);
        base.add(plane.tessellation.get(1));
        
        reachedCombinations = new HashSet<HashSet<Tile>>();
        reachedCombinations.add(base);
        
        HashSet<HashSet<Tile>> nextCombinations = new HashSet<>();
        HashSet<Tile> best = base;
        for(int i = 1; i < maxTiles; i++){
            for(HashSet<Tile> animal : reachedCombinations){
                getAllExpansions(animal, nextCombinations);
            }
            reachedCombinations = nextCombinations;
            nextCombinations = new HashSet<>();
            best = findBest(best, reachedCombinations, plane);
        }
        
        reachedCombinations = null;        
        return best;
    }
    
    /**
    Helper for GetHoleyest. Finds every polyform that can be created by adding one tile to the polyform.
    */
    private static void getAllExpansions(HashSet<Tile> base, HashSet<HashSet<Tile>> expansions){
        HashSet<Tile> expanded;
        for(Tile tile : base){
            for(int i = 0; i < tile.adjacent.length; i++){
                if(tile.adjacent[i] == null)
                    continue;
                expanded = new HashSet<>(base.size() + 1, 1);
                expanded.addAll(base);
                expanded.add(tile.adjacent[i]);
                if(expanded.size() > base.size())
                    expansions.add(expanded);
            }
        }
    }
    
    /**
    Helper for GetHoleyest. Returns the polyform from the input that has the most holes, and if there is a tie, fewest tiles.
    @param best the baseline best polyform. If there is a tie in holes and polyforms, the oldest best is kept
    @param others the other polyforms that will be searched
    @param plane the tessellation that the polyforms reside in
    @return the polyform with most holes and fewest tiles
    */
    private static HashSet<Tile> findBest(HashSet<Tile> best, HashSet<HashSet<Tile>> others, Tessellation plane){
        int bestHoles = Polyform.numHoles(plane, best);
        int contenderHoles;
        for(HashSet<Tile> contender : others){
            contenderHoles = Polyform.numHoles(plane, contender);
            if(contenderHoles > bestHoles || (contenderHoles == bestHoles && contender.size() < best.size())){
                bestHoles = contenderHoles;
                best = contender;
            }
        }
        return best;
    }
    
    /**
    Uses the Floyd-Warshall algorithm to find 
    the tile best suited for being translted to (0,0) in the tessellation
    //TODO: maybe force tiles to be properly flagged as holes before this is called to allow passage across holes
    */
    public static Tile findCenter(Tessellation plane){
        Integer[][] distance = new Integer[plane.tessellation.size()][plane.tessellation.size()];
        for(int i = 0; i < distance.length; i++){ //initializes distances
            for(int j = 0; j < distance.length; j++){ //non-adjacent tiles
                distance[i][j] = Integer.MAX_VALUE;
            }
            for(Tile t : plane.tessellation.get(i).adjacent){ //adjacent tiles
                if(t == null || t.type == Type.EMPTY) 
                    continue;
                distance[i][t.getAbsoluteIndex(plane.ringSizes)] = 1;
            }
            distance[i][i] = 0; //itself
        }
        
        for(int i = 0; i < distance.length; i++){ //computes true values for minimum distances
            for(int j = 0; j < distance.length; j++){
                for(int k = 0; k < distance.length; k++){
                    if(distance[j][k] > distance[j][i] + distance[i][k]){
                        distance[j][k] = distance[j][i] + distance[i][k];
                    }
                }
            }
        }
        
        int minEccentricity = Integer.MAX_VALUE;
        int temp;
        Tile center = null;
        for(int i = 0; i < distance.length; i++){ //stores the first tile with minimal eccentricity
            temp = Integer.MAX_VALUE;
            for(int j = 0; j < distance[i].length; j++){
                if(temp > distance[i][j]){
                    temp = distance[i][j];
                }
            }
            if(minEccentricity > temp){
                minEccentricity = temp;
                center = plane.tessellation.get(i);
            } 
        }
        
        return center;
    }
} 