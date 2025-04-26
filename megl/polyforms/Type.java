package megl.polyforms;
/**
Denotes whether a tile in the tesselation is part of a hole, part of an animal, or neither. 
EMPTY is used to distinguish from null, as not all tiles created by Tessellation.java are used in polyforms.
*/
public enum Type {HOLE, ANIMAL, EMPTY}