import controllers.LibraryController;

public class Main {

    public static void main(String[] args) {
        // Instantiate the controller layer engine
        LibraryController app = new LibraryController();
        
        // Start processing user inputs via the central application loop
        app.start();
    }

}
