import database.AccountDatabaseAdapter;
import database.DatabaseConfig;

public class RUNME {
    public static void main(String[] Args) {
        System.out.println("Banking System Starting...");
        System.out.println("Testing database connection");

        if (!DatabaseConfig.testConnection()) {
            System.out.println("Database connection failed");
            System.out.println("Program will execute, but will not save any changes");
        }
        else {
            System.out.println("Database connected successfully! \n");
        }

        Interface.choice_wrapper();
    }
}
