import database.AccountDatabaseAdapter;
import database.DatabaseConfig;
import service.RiskServiceClient;

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

        try {
            RiskServiceClient riskClient = new RiskServiceClient();

            String result = riskClient.analyzeTransaction(
                    1800.0,
                    "withdrawal",
                    7
            );

            System.out.println("Risk service response:");
            System.out.println(result);

        } catch (Exception e) {
            System.err.println("Risk service test failed:");
            e.printStackTrace();
        }

        Interface.choice_wrapper();
    }
}
