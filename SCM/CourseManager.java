package SCM;

import java.io.*;
import java.util.ArrayList;
import java.util.Scanner;

public class CourseManager {

    private ArrayList<Course> courseList = new ArrayList<>();
    private Scanner scanner = new Scanner(System.in);

    public void showMenu() {

        System.out.println("\n==============================");
        System.out.println(" Victor's Student Course Management System");
        System.out.println("==============================");
        System.out.println("1. Add Course");
        System.out.println("2. View Courses");
        System.out.println("3. Search Course");
        System.out.println("4. Compute Total Units");
        System.out.println("5. Save Courses");
        System.out.println("6. Load Courses");
        System.out.println("7. Exit");
        System.out.print("Enter your choice: ");

        try {

            int choice = Integer.parseInt(scanner.nextLine());

            switch (choice) {

                case 1:
                    addCourse();
                    break;

                case 2:
                    viewCourses();
                    break;

                case 3:
                    searchCourse();
                    break;

                case 4:
                    displayTotalUnits();
                    break;

                case 5:
                    saveCourses();
                    break;

                case 6:
                    loadCourses();
                    break;

                case 7:
                    System.out.println("Goodbye!");
                    return;

                default:
                    System.out.println("Invalid choice. Please enter a number between 1 and 7.");
            }

        } catch (Exception e) {
            System.out.println("Invalid input. Please enter a number.");
        }

        showMenu();
    }

    public void addCourse() {

        try {

            System.out.print("Enter Course Code: ");
            String code = scanner.nextLine().trim().toUpperCase();

            if(code.isEmpty()){
                throw new IllegalArgumentException("Course code cannot be empty.");
            }

            System.out.print("Enter Course Title: ");
            String title = scanner.nextLine().trim();

            if(title.isEmpty()){
                throw new IllegalArgumentException("Course title cannot be empty.");
            }

            System.out.print("Enter Course Unit: ");
            int unit = Integer.parseInt(scanner.nextLine());

            if(unit<=0){
                throw new IllegalArgumentException("Unit must be a positive number.");
            }

            courseList.add(new Course(code,title,unit));

            System.out.println("Course added successfully.");

        }catch(Exception e){

            System.out.println("Could not add course: "+e.getMessage());

        }

    }

    public void viewCourses(){

        if(courseList.isEmpty()){

            System.out.println("No courses have been added yet.");
            return;

        }

        System.out.println("\n----------------------------------------------");
        System.out.printf("%-10s %-35s %s%n",
                "CODE","TITLE","UNIT");
        System.out.println("----------------------------------------------");

        for(Course course : courseList){

            System.out.println(course);

        }

    }

    public void searchCourse(){

        System.out.print("Enter the Course Code to search for: ");

        String code = scanner.nextLine().trim().toUpperCase();

        Course foundCourse = findCourseByCode(code,0);

        if(foundCourse==null){

            System.out.println("No course found with code \""+code+"\".");

        }else{

            System.out.println("\nCourse found:");
            System.out.println(foundCourse);

        }

    }

    private Course findCourseByCode(String code, int index){

        if(index>=courseList.size())
            return null;

        if(courseList.get(index).getCourseCode().equalsIgnoreCase(code))
            return courseList.get(index);

        return findCourseByCode(code,index+1);

    }

    public void displayTotalUnits(){

        int totalUnits=0;

        for(Course course : courseList){

            totalUnits+=course.getUnit();

        }

        System.out.println("Total Units Across All Courses: "+totalUnits);

    }

    public void saveCourses(){

        try{

            BufferedWriter writer=new BufferedWriter(new FileWriter("courses.txt"));

            for(Course course : courseList){

                writer.write(course.getCourseCode()+","
                        +course.getCourseTitle()+","
                        +course.getUnit());

                writer.newLine();

            }

            writer.close();

            System.out.println("Courses saved successfully to courses.txt.");

        }catch(IOException e){

            System.out.println("Error saving courses to file.");

        }

    }

    public void loadCourses(){

        try{

            BufferedReader reader=new BufferedReader(new FileReader("courses.txt"));

            courseList.clear();

            String line;

            while((line=reader.readLine())!=null){

                String[] fields=line.split(",");

                if(fields.length==3){

                    courseList.add(

                            new Course(

                                    fields[0],

                                    fields[1],

                                    Integer.parseInt(fields[2])

                            )

                    );

                }

            }

            reader.close();

            System.out.println("Courses loaded successfully from courses.txt.");

        }catch(IOException e){

            System.out.println("No saved course file found.");

        }

    }

}
