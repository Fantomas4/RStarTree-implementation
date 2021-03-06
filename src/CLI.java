import queries.LocationQueryResult;
import queries.SequentialNNQuery;
import queries.SequentialRangeQuery;
import tree.RStarTree;
import utils.FileHandler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;
import java.util.Scanner;

public class CLI {

    public static void run()
    {
        Scanner scanner = new Scanner(System.in);
        String input;
        ArrayList<LocationQueryResult> queryResults;
        RStarTree rStarTree = new RStarTree();
        do {
            System.out.println("Options:");
            System.out.println("1) K - Nearest Neighbour Tree Query");
            System.out.println("2) K - Nearest Neighbour Sequential Query");
            System.out.println("3) Range Tree Query");
            System.out.println("4) Range Sequential Query");
            System.out.println("0) Exit");

            System.out.print("Select option: ");
            input = scanner.nextLine().trim().toLowerCase(Locale.ROOT);
            while (!input.equals("0") && !input.equals("1") && !input.equals("2") && !input.equals("3") && !input.equals("4"))
            {
                System.out.println("Select one of the three available options: 0, 1, 2, 3 or 4");
                System.out.print("Select option: ");
                input = scanner.nextLine().trim().toLowerCase(Locale.ROOT);
            }
            if (input.equals("0"))
            {
                break;
            }
            System.out.println();
            double[] centerCoordinates = new double[FileHandler.DIMENSIONS];
            System.out.println("Enter center point coordinates: ");
            for (int i = 0; i < FileHandler.DIMENSIONS; ++i)
            {
                System.out.print("Enter center point coordinates for dimension " + (i + 1) + ": ");
                centerCoordinates[i] = scanner.nextDouble();
            }
            System.out.println("Your center point is: " + Arrays.toString(centerCoordinates));

            long startingTime, endingTime;
            int k;
            double r;
            switch (input)
            {
                case "1":
                    System.out.println("K - Nearest Neighbour Query selected");
                    System.out.print("Enter k (number of nearest neighbours):");
                    k = scanner.nextInt();
                    while (k <= 0)
                    {
                        System.out.println("K must be a positive integer");
                        System.out.print("Enter k (number of nearest neighbours):");
                        k = scanner.nextInt();
                    }

                    System.out.println("R-Star Tree");
                    startingTime = System.nanoTime();
                    queryResults = rStarTree.executeNNQuery(centerCoordinates, k);
                    endingTime = System.nanoTime();
                    for (LocationQueryResult result : queryResults) {
                        System.out.println(result);
                    }
                    System.out.println("Time taken: " + (double)(endingTime - startingTime) / 1000000 + "ms");

                    break;
                case "2":
                    System.out.println("K - Nearest Neighbour Query selected");
                    System.out.print("Enter k (number of nearest neighbours):");
                    k = scanner.nextInt();
                    while (k <= 0)
                    {
                        System.out.println("K must be a positive integer");
                        System.out.print("Enter k (number of nearest neighbours):");
                        k = scanner.nextInt();
                    }

                    System.out.println("Sequential");
                    startingTime = System.nanoTime();
                    queryResults = new SequentialNNQuery(centerCoordinates, k).execute();
                    endingTime = System.nanoTime();
                    for (LocationQueryResult result : queryResults) {
                        System.out.println(result);
                    }
                    System.out.println("Time taken: " + (double)(endingTime - startingTime) / 1000000 + "ms\n\n\n");
                    break;
                case "3":
                    System.out.println("Range Query selected");
                    System.out.print("Enter radius for range query:");
                    r = scanner.nextDouble();
                    while (r <= 0)
                    {
                        System.out.println("Radius must be a positive value");
                        System.out.print("Enter positive radius:");
                        r = scanner.nextDouble();
                    }

                    System.out.println("R-Star Tree");
                    startingTime = System.nanoTime();
                    queryResults = rStarTree.executeRangeQuery(centerCoordinates, r);
                    endingTime = System.nanoTime();
                    for (LocationQueryResult result : queryResults) {
                        System.out.println(result);
                    }
                    System.out.println("Time taken: " + (double)(endingTime - startingTime) / 1000000 + "ms");

                    break;
                case "4":
                    System.out.println("Range Query selected");
                    System.out.print("Enter radius for range query:");
                    r = scanner.nextDouble();
                    while (r <= 0)
                    {
                        System.out.println("Radius must be a positive value");
                        System.out.print("Enter positive radius:");
                        r = scanner.nextDouble();
                    }

                    System.out.println("Sequential");
                    startingTime = System.nanoTime();
                    queryResults = new SequentialRangeQuery(centerCoordinates, r).execute();
                    endingTime = System.nanoTime();
                    for (LocationQueryResult result : queryResults) {
                        System.out.println(result);
                    }
                    System.out.println("Time taken: " + (double)(endingTime - startingTime) / 1000000 + "ms\n\n\nS");
                    break;
            }
            scanner.nextLine(); // Flushing scanner buffer
        } while (true);

    }

    public static void main(String[] args) {
        run();
        System.out.println("DONE!");
    }

}

