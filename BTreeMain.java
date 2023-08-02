import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.Random;

/**
 * Main Application.
 */
public class BTreeMain {

    public static void main(String[] args) {

        /** Read the input file -- input.txt */
        Scanner scan = null;
        try {
            scan = new Scanner(new File("src/input.txt"));
        } catch (FileNotFoundException e) {
            System.out.println("File not found.");
        }

        /** Read the minimum degree of B+Tree first */

        int degree = scan.nextInt();

        BTree bTree = new BTree(degree);

        /** Reading the database student.csv into B+Tree Node */
        List<Student> studentsDB = getStudents();

        for (Student s : studentsDB) {
            bTree.insert(s);
        }

        /** Start reading the operations now from input file */
        try {
            while (scan.hasNextLine()) {
                Scanner s2 = new Scanner(scan.nextLine());

                while (s2.hasNext()) {

                    String operation = s2.next();

                    switch (operation) {
                        case "insert": {

                            long studentId = Long.parseLong(s2.next());
                            String studentName = s2.next() + " " + s2.next();
                            String major = s2.next();
                            String level = s2.next();
                            int age = Integer.parseInt(s2.next());

                            /** TODO: Write a logic to generate recordID */

                            long recordID = new Random().nextLong(); // creates a random long
                            if (s2.hasNext()) {
                                recordID = Long.parseLong(s2.next()); // replaces long with input recordIDif it
                                // exists
                            }

                            Student s = new Student(studentId, age, studentName, major, level, recordID);
                            bTree.insert(s);

                            break;
                        }
                        case "delete": {
                            long studentId = Long.parseLong(s2.next());
                            boolean result = bTree.delete(studentId);
                            if (result)
                                System.out.println("Student deleted successfully.");
                            else
                                System.out.println("Student deletion failed.");

                            break;
                        }
                        case "search": {
                            long studentId = Long.parseLong(s2.next());
                            long recordID = bTree.search(studentId);
                            if (recordID != -1)
                                System.out.println("Student exists in the database at " + recordID);
                            else
                                System.out.println("Student does not exist.");
                            break;
                        }
                        case "print": {
                            List<Long> listOfRecordID = new ArrayList<>();
                            listOfRecordID = bTree.print();
                            System.out.println("List of recordIDs in B+Tree " + listOfRecordID.toString());
                        }
                        default:
                            System.out.println("Wrong Operation");
                            break;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static List<Student> getStudents() {

        /**
         * TODO: Extract the students information from "Students.csv" return the list<Students>
         */

        List<Student> studentList = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader("src/Student.csv"))) {
            String line; // will store current row of csv as string
            while ((line = br.readLine()) != null) {
                String[] values = line.split(","); // gets all values in current row
                studentList.add(new Student(Long.parseLong(values[0]), Integer.parseInt(values[4]),
                        values[1], values[2], values[3], Long.parseLong(values[5]))); // creates a student and
                // adds to arraylist
            }
        } catch (FileNotFoundException e) {
            System.out.println("File not found.");
        } catch (IOException e) {
            System.out.println("File not found.");
        }

        return studentList;
    }
}