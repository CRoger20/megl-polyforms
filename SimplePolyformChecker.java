import megl.polyforms.*;
import java.util.Arrays;
/**
Given p, q, rings, tiles, searches for a polyform up to the given 
number of tiles in the {p,q} tessellation contained within the 
given number of rings of the origin. It will then print information 
about the polyform with most holes and fewest tiles that it found.

It is not recommended to use rings higher than 2 or {p,q} that are 
too large as this will cause it to take a very long time to complete.
*/
public class SimplePolyformChecker {
    public static void main(String[] args){
        int p, q, rings, tiles;
        try{
            p = Integer.parseInt(args[0]);
            q = Integer.parseInt(args[1]);
            rings = Integer.parseInt(args[2]);
            tiles = Integer.parseInt(args[3]);
        }
        catch(NumberFormatException|ArrayIndexOutOfBoundsException e){
            System.err.println("You must enter four int arguments: p, q, rings, tiles");
            return;
        }
        Tessellation plane = new Tessellation(p, q, rings);
        Polyform animal = new Polyform(PolyformFinder.findHoleyestAnimal(plane, tiles));
        Tile[] foundTiles = new Tile[1];
        foundTiles = animal.getAnimal().toArray(foundTiles);
        Arrays.sort(foundTiles);
        
        System.out.println(String.format("In the {%d,%d} tessellation with %d rings, the best result with up to %d tiles had %d holes.", p, q, rings, tiles, Polyform.numHoles(plane, animal.getAnimal())));
        System.out.print(String.format("A minimal animal that met this result had %d tiles: ", foundTiles.length));
        for(int i = 0; i < foundTiles.length -1; i++)
            System.out.print(foundTiles[i] + ", ");
        System.out.println(foundTiles[foundTiles.length -1]);
    }
}