This project contains files for representing tiles in tessellations and polyforms. It also contains a display program for polyforms and a program to search for the optimal polyform in a tessellation with up to some number of tiles.

To use the supplied programs, simply download and compile the program you want to use and the megl.polyforms package. Alternatively, the polyforms.jar file contains the code already compiled with java 13.0.2 and can be used to run the display program.

Front-end files that are meant to be run from the command line are found in the root folder. Back-end files are found in /megl/polyforms. A summary of them can be found below, with additional information supplied as javadoc comments in each file.

Front-end files:
   PolyformDisplayRunner.java - Creates a visual display for navigating a tessellation and polyforms within it. In this display, rings of tiles are displayed as rows. Tiles that are in the same row are in the same ring of the tessellation, with left to right being the direction of increasing ringPos. Tiles that are red are part of the polyform, while tiles that are gray are not. Two tiles are kept track of at the bottom of the screen. One of them, the center tile, controls which tiles are displayed on screen. The other one, the highlight center, is outlined in a blue-green color. Tiles that are adjacent to the highlight center are outlined in blue.

   SimplePolyformChecker.java - Given p, q, rings, tiles, searches for a polyform up to the given number of tiles in the {p,q} tessellation contained within the given number of rings of the origin. It will then print information about the polyform with most holes and fewest tiles that it found.


Back-end files:
   Tessellation.java - Constructs a subset of the {p,q} tessellation with every vertex and edge necessary to form as many rings of tiles as specified. To use it, create an instance and call makeTessellation().

   Tile.java - Represents a tile of a tessellation. These tiles behave like vertices of the dual of the tessellation they are in.

   QThreeTile.java - Represents a Tile of a tessellation when q = 3

   MegaTile.java - Represents an arbitrary collection of tiles, similar to a polyform except these are treated as one tile. Used by the display program to condense large blocks of tiles that are all of the same type.

   Polyform.java - Represents an arbitrary collection of tiles in a tessellation. Has methods for testing if the polyform is connected and finding how many holes it has, as well as for reading text files into polyforms or writing polyforms into text files.

   PolyformFinder.java - Contains methods for brute-force testing of polyforms to find ones with the most holes and fewest tiles.

   Type.java - An enum specifying the state of a tile

   PolyformDisplayer - Creates a visual display of a tessellation using swing.




This project was made as part of the Visualizing Holey Hyperbolic Polyforms project of the Mason Experimental Geometry Lab at George Mason University in Fall 2024 and Spring 2025. All code contained within was written by Cooper Roger with help from Aiden Roger, Adithya Prabha, Summer Eldridge, and Dr. Ros Toala.
See https://megl.science.gmu.edu/spring-2025/
