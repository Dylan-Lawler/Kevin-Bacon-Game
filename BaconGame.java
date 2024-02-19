import java.io.*;
import java.util.*;

/**
 * Uses the graph library to create a interactive interface to play a game revolving around popular actors
 * @author Rebecca Liu and Dylan Lawler, Spring 2021
 */

public class BaconGame extends GraphLibrary{
    // instance maps for each file
    public Map<String, String> actorMap;
    public Map<String, String> movieMap;
    public Map<String, Set<String>> actorMovieMap;

    // instance maps that combines all file data into one
    public Map<String, Set<String>> actorMovie2Map;

    // graph of the actors and their connected movies
    public Graph<String, Set<String>> graph;

    // bfs graph of given center
    public Graph<String, Set<String>> pathgraph;

    // universe center
    public String center;


    /**
     *
     * @param actorFile file of actor ids to names
     * @param movieFile file of movie ids to names
     * @param actorMovieFile file of actor ids to movie ids
     */
    public BaconGame(String actorFile, String movieFile, String actorMovieFile){
        // initalizes all of the instance variables
        this.actorMap = new TreeMap<>();
        this.movieMap = new TreeMap<>();
        this.actorMovieMap = new TreeMap<>();
        this.actorMovie2Map = new TreeMap<>();
        this.graph = new AdjacencyMapGraph<>();
        this.center = "Kevin Bacon";

        // reads the actorfile into a map of keys as ids and values as names
        try{
            try{
                BufferedReader actorInput = new BufferedReader(new FileReader(actorFile));
                String line;
                while ((line = actorInput.readLine())!= null) {
                    String[] splitLine = line.split("\\|");
                    this.actorMap.put(splitLine[0], splitLine[1]);
                }
                actorInput.close();
            }

            // catches nonexistent file exception
            catch(FileNotFoundException e){
                //catch exception if file not found
                System.out.println("File does not exist");
            }}
        catch(IOException e){
            //catch IO exception
            System.out.println("IO exception" );
        }

        // reads the moviefile into a map of keys as ids and values as names
        try{
            try{
                BufferedReader movieInput = new BufferedReader(new FileReader(movieFile));
                String line;
                while ((line = movieInput.readLine())!= null) {
                    String[] splitLine = line.split("\\|");
                    this.movieMap.put(splitLine[0], splitLine[1]);
                }
                movieInput.close();
            }
            catch(FileNotFoundException e){
                //catch exception if file not found
                System.out.println("File does not exist" + e.getMessage());
            }}
        catch(IOException e){
            //catch IO exception
            System.out.println("IO exception" );
        }

        // reads the actor to movie file into a map of keys as actor ids and values as a set of movie ids
        try{
            try{
                BufferedReader actorMovieInput = new BufferedReader(new FileReader(actorMovieFile));
                String line;
                while ((line = actorMovieInput.readLine())!= null) {
                    String[] splitLine = line.split("\\|");
                    if (!actorMovieMap.containsKey(splitLine[1])){
                        this.actorMovieMap.put(splitLine[1], new HashSet<>());
                    }
                    this.actorMovieMap.get(splitLine[1]).add(splitLine[0]);
                }
                actorMovieInput.close();
            }
            catch(FileNotFoundException e){
                //catch exception if file not found
                System.out.println("File does not exist" + e.getMessage());
            }}
        catch(IOException e){
            //catch IO exception
            System.out.println("IO exception" );
        }

        // loops through all three file maps to get a map of actor names as keys and their respective sets of movie names as values
        for(String actorID: actorMovieMap.keySet()){
            Set<String> movieset = new HashSet<>();
            actorMovie2Map.put(actorMap.get(actorID), movieset);
            for(String movieID: actorMovieMap.get(actorID)){
                actorMovie2Map.get(actorMap.get(actorID)).add(movieMap.get(movieID));
            }
        }
    }

    /**
     * forms the graph from the actor to movie name map
     */
    public void createGraph(){
        // loops through the actors names from the actor to movie name map and inserts the actor into the tree
        for(String actor: actorMovie2Map.keySet()){
            graph.insertVertex(actor);
        }
        // loops through the graph twice to compare two different actors
        for(String actor1: graph.vertices()){
            for(String actor2: graph.vertices()){
                // as long as they're not the same
                if(!actor1.equals(actor2)){
                    // makes a set of the common movies between the two actors
                    Set<String> movieset = new HashSet();
                    // loops through the sets of movies each actor has been in
                    for(String movie1: actorMovie2Map.get(actor1)){
                        for(String movie2: actorMovie2Map.get(actor2)){
                            // if they have the same movie add it to the common movie set
                            if(movie1.equals(movie2)){
                                movieset.add(movie1);
                            }
                        }
                    }
                    // if the common movies set isn't empty, add it as an edge label between the two actors
                    if(!movieset.isEmpty()){
                        graph.insertUndirected(actor1, actor2, movieset);

                    }

                }
            }
        }
        // makes a shortest path graph from the given center and the graph that was just built
        pathgraph = BaconGame.bfs(graph, center);

    }

    /**
     * Allows for user interaction in the console and utilizes the other methods to get information
     * about the actors
     */
    public void gameInterface(){
        // initializes the scanner to allow for user interaction
        Scanner in = new Scanner(System.in);

        // the line in which the user will type, initialized as empty
        String line = "";

        // a list of all the possible commands for the game as well as the declaration of Kevin Bacon as the starting center
        System.out.println("Commands:\n" +
                "c <#>: list top (positive number) or bottom (negative) <#> centers of the universe, sorted by average separation\n" +
                "d <low> <high>: list actors sorted by degree, with degree between low and high\n" +
                "i: list actors with infinite separation from the current center\n" +
                "p <name>: find path from <name> to current center of the universe\n" +
                "s <low> <high>: list actors sorted by non-infinite separation from the current center, with separation between low and high\n" +
                "u <name>: make <name> the center of the universe\n" +
                "q: quit game\n" + '\n' + center + " is now the center of the acting universe, connected to " + (pathgraph.numVertices() - 1) + "/"+ actorMap.size() + " actors with average separation " + averageSeparation(pathgraph, center));

        // while the user doesn't press q to quit the game
        while(!line.equals("q")){

            // prints out the center of the universe
            System.out.print(center +" game>" + '\n');

            // updates the user line
            line = in.nextLine();

            //splits the line to separate the user command and the command details
            String[] lineSplit = line.split(" ");

            // c gets a given number of best or worst centers by their average separation
            if (lineSplit[0].equals("c")){
               try {
                   // makes a priority queue to compare average separations
                   // if the inout is positive, returns the roots with the highest average separations
                   if (Integer.valueOf(lineSplit[1]) > 0) {
                       PriorityQueue<String> avgCenters = new PriorityQueue<String>(new Comparator<String>() {
                           @Override
                           public int compare(String o1, String o2) {
                               if (averageSeparation(bfs(graph, o1), o1) < averageSeparation(bfs(graph, o2), o2)) {
                                   return 1;
                               }
                               if (averageSeparation(bfs(graph, o1), o1) > averageSeparation(bfs(graph, o2), o2)) {
                                   return -1;
                               } else {
                                   return 0;
                               }
                           }
                       });

                       // makes an array list of the desired number of centers and adds to it by removing from the priority queue
                       ArrayList<String> topCenter = new ArrayList<>();
                       for (String vertex : actorMovie2Map.keySet()) {
                           avgCenters.add(vertex);
                       }
                       for (int i = 0; i < Integer.valueOf(lineSplit[1]); i++) {
                           topCenter.add(avgCenters.remove());
                       }
                       // makes the list of string so we can print out the preface sentence
                       String empty = "";
                       empty += topCenter;
                       System.out.println("best centers by average separation: " + empty);
                   }

                   // does the same as if the input is positive but returns the centers with the lowest average separations
                   if (Integer.valueOf(lineSplit[1]) < 0) {
                       PriorityQueue<String> avgCenters = new PriorityQueue<String>(new Comparator<String>() {
                           @Override
                           public int compare(String o1, String o2) {
                               if (averageSeparation(bfs(graph, o1), o1) > averageSeparation(bfs(graph, o2), o2)) {
                                   return 1;
                               }
                               if (averageSeparation(bfs(graph, o1), o1) < averageSeparation(bfs(graph, o2), o2)) {
                                   return -1;
                               } else {
                                   return 0;
                               }
                           }
                       });
                       ArrayList<String> topCenter = new ArrayList<>();
                       for (String vertex : actorMovie2Map.keySet()) {
                           avgCenters.add(vertex);
                       }
                       for (int i = 0; i > Integer.valueOf(lineSplit[1]); i--) {
                           topCenter.add(avgCenters.remove());
                       }
                       String empty = "";
                       empty += topCenter;
                       System.out.println("worst centers by average separation: " + empty);
                   }
               }
               // makes sure the user only inputs numbers after the c command
               catch(NumberFormatException e){
                   System.out.println("centers amount must be a number.");
               }
            }

            // d command sorts actors by their out degree from a given low and high
            else if(lineSplit[0].equals("d")){

                try{

                    // low is the first input, high is the second
                int low = Integer.valueOf(lineSplit[1]);
                int high = Integer.valueOf(lineSplit[2]);

                // makes the list of the actors and adds to it if the degree is between the given high and low
                List<String> actors = new ArrayList<String>();
                for(String actor: pathgraph.vertices()){
                    if(low <= pathgraph.outDegree(actor) && pathgraph.outDegree(actor)<= high){
                        actors.add(actor);
                    }
                }
                System.out.println(actors);

            }
                // makes sure the user is only putting numbers as the high and low inputs
                catch(NumberFormatException e){
                    System.out.println("high and low must be numbers.");
                }
            }

            // i input gets the actors infinitely separated from the root
            else if(lineSplit[0].equals("i")){
                //  calls missing vertices between the actor graph and the path graph of the center
                System.out.println(BaconGame.missingVertices(graph, pathgraph));

            }

            // p input finds the shortest path between a given actor and the center of the universe
            else if (lineSplit[0].equals("p")){

                // makes a string of the entire second half of the input just in case it's not one word
                String actorValid = "";
                for (int j = 1; j<lineSplit.length ; j ++) {
                    if (j != lineSplit.length - 1) {
                        actorValid += lineSplit[j] + " ";
                    }
                    else {
                        actorValid += lineSplit[j];
                    }
                }

                // makes sure the user is inputting an actor that exists in the tree
                if(!actorMovie2Map.containsKey(actorValid)){
                    System.out.println("invalid actor");
                }

                // the user given actor doesn't have a path to the center of the universe
            if(!graph.hasVertex(actorValid)){
                    System.out.println("actor not connected to center of the universe.");
                }

            // tells user if the given actor is already the center
                else if(actorValid.equals(center)){
                        System.out.println("no path because this actor is the center.");
                }

                // gets the path distance from the center of the universe, declared as that actor's number
                else{ int i = 0;
                String name = line.substring(2);
                for (String step : getPath(pathgraph, name)){
                    i += 1;
                }
                System.out.println(name + "'s number is " + i);

                // makes an array list to go backwards from the given actor back to the center
                ArrayList<String> reversePath = new ArrayList<>();

                // goes through the shortest path and adds it to the list
                for (String step : getPath(pathgraph, name)){
                    for (String neighbor: pathgraph.outNeighbors(step)){
                        reversePath.add(0, step + " appeared in " + pathgraph.getLabel(step, neighbor) + " with " +  neighbor);
                    }
                }
                // loops through the list and prints out each co star statement
                for (int j = 0 ; j < reversePath.size() ; j++ ){
                    System.out.println(reversePath.get(j));
                }
            }
            }

            // s returns a list of actors sorted by their separation from the root given a low and high boundary
            else if(lineSplit[0].equals("s")){
                // first input is the lowest and second is the highest possible separation
                try{
                int low = Integer.valueOf(lineSplit[1]);
                int high = Integer.valueOf(lineSplit[2]);

                // initializes an actor list
                List<String> actorlist = new ArrayList<>();
                // loops through the path graph and adds to the list only if the actors separation is in between the bounds
                for(String actor: pathgraph.vertices()){
                    if(low<= getPath(pathgraph, actor).size() && getPath(pathgraph, actor).size() <= high){
                        actorlist.add(actor);
                    }
                }
                System.out.println(actorlist);

            }
                // makes sure the user inputs numbers as the high and low bounds
                catch(NumberFormatException e){
                    System.out.println("high and low must be numbers.");
                }
            }

            // u input sets the center of the universe to the given actor
           else if(lineSplit[0].equals("u")){

                // makes a string of the entire second half of the input just in case it's not one word
                String actorValid = "";
                for (int j = 1; j<lineSplit.length ; j ++) {
                    if (j != lineSplit.length - 1) {
                        actorValid += lineSplit[j] + " ";
                    }
                    else {
                        actorValid += lineSplit[j];
                    }
                }

                // if the map doesn't have the user given actor throw an exception
                if(!actorMovie2Map.containsKey(actorValid)){
                    System.out.println("invalid actor");
                }

                // throw an exception if the user inputs the center of the universe
                else if(actorValid.equals(center)){
                    System.out.println("this actor is already the center.");
                }

                // changes the center instance variable to the user input and recalls bfs on the new center
                else{
                center = line.substring(2);
                pathgraph = BaconGame.bfs(graph, center);
                System.out.println(center + " is now the center of the acting universe, connected to " + (pathgraph.numVertices() - 1)+ "/" + actorMap.size() + " actors with average separation " + averageSeparation(pathgraph, center));
                }
            }
           // quits the game by ending the scanner loop
            else if(lineSplit[0].equals("q")){
                System.out.println("thanks for playing.");
            }
            // if the command wasn't one of the given ones, throw an error
            else {
                System.out.println("invalid command.");
            }
        }
    }


// tests the game on the given test file
    public static void main(String[] args) {
        BaconGame test = new BaconGame("PS4/actorsTest.txt", "PS4/moviesTest.txt", "PS4/movie-actorsTest.txt");
        test.createGraph();
        test.gameInterface();

    }


}


