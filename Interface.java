//Not importing .* in order for space efficiency
import java.util.Scanner;
import java.util.InputMismatchException;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.File;
import java.io.IOException;

public class Interface {
    static Scanner input = new Scanner(System.in);
    static Bank userBankm;


    //Wrapper class for the main test file to implement
    public static void choice_wrapper() {
        choice();
    }


    // Method to write the data of this program into a presentable text file
    public static void saveSession(User user) {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter("bankProject.txt"));
            writer.write("Money: $" + user.getMoney() + "\n");
            writer.write("Total Transactions: " + user.getTransactions() + "\n");
            writer.write("Bank Transactions:" + "\n");
            for (int i = 0; i < 3; i++) {
                if (user.getBank(i) == null) {
                    break;
                }
                writer.write(user.getBank(i).getName() + ": $" + user.getBank(i).getBankDeposit() + "\n");
                writer.write(user.getBank(i).getTransactions() + "\n");
            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Turns the presentable text file made from saveSession back into data for the program for someone to start where they left off
    public static User uploadSession() {
        try {
            File file = new File("bankProject.txt");
            try (Scanner scan = new Scanner(file)) {
                String token = scan.next();
                token = scan.next();
                token = token.substring(1);
                double change = Double.parseDouble(token);

                User user = new User(change);

                
                scan.next();
                scan.next();
                token = scan.nextLine();

                String[] tempArr;
                double[] tranHolder;
                int i;

                // This portion turns the total transactions array into actual data
                if (token.charAt(2) != 'Y') {
                    tempArr = token.split(",");
                    tempArr[0] = tempArr[0].substring(2);
                    tempArr[tempArr.length - 1] = tempArr[tempArr.length - 1].substring(0, tempArr[tempArr.length - 1].length() - 1);

                    tranHolder = new double[tempArr.length];
                    for (i = 0; i < tranHolder.length; i++) {
                        tranHolder[i] = Double.parseDouble(tempArr[i]);
                    }
                    user.setTransactions(tranHolder);
                } 

                Bank bank;
                int counter = 0;
                int indexDecider;
                scan.nextLine();

                //Goes through the information of the given banks
                while (scan.hasNext()) {
                    token = scan.nextLine();
                    bank = bankDecider(token);
                    user.uploadAddBank(bank);

                    token = scan.nextLine();
                    indexDecider = token.indexOf('[');
                    token = token.substring(indexDecider);

                    if (token.charAt(1) != 'Y') {
                        tempArr = token.split(",");
                        tempArr[0] = tempArr[0].substring(1);
                        tempArr[tempArr.length - 1] = tempArr[tempArr.length - 1].substring(0, tempArr[tempArr.length - 1].length() - 1);

                        tranHolder = new double[tempArr.length];
                        for (i = 0; i < tranHolder.length; i++) {
                            tranHolder[i] = Double.parseDouble(tempArr[i]);
                        }
                        user.getBank(counter).setTransactions(tranHolder);
                    }
                    counter++;
                }


                scan.close();
                return user;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    //Helper method for uploadSession to determine what bank is being fed into the scanner to appropriately convert to the right data
    private static Bank bankDecider(String input) {
        Scanner bscan = new Scanner(input);
        String val;
        double fval;
        Bank bank;
        input = bscan.next();


        if (input.equals("BNY")) {
            bscan.next();
            val = bscan.next();
            val = val.substring(1);
            fval = Double.parseDouble(val);
            bank = new BNYMellon(fval);
        }
        else if (input.equals("Chase")) {
            val = bscan.next();
            val = val.substring(1);
            fval = Double.parseDouble(val);
            bank = new Chase(fval);
        }
        else {
            bscan.next();
            val = bscan.next();
            val = val.substring(1);
            fval = Double.parseDouble(val);
            bank = new CapitalOne(fval);
        }
        bscan.close();
        return bank;
    }


    // The "wrapped" class that implements the bulk of the Terminal User Interface
    private static void choice() {
        System.out.println("Weldome! To start your session, have you already done a past session you would like to continue?");
        System.out.println("Y for yes or N for no");

        String iochoice;
        User user;
        while (true) {
            iochoice = input.next().toUpperCase();
            try {
                if (iochoice.equals("Y") || iochoice.equals("N")) {
                    break;
                }
            } catch (InputMismatchException e) {
                input.next();
            }
        }

        //Tests to see if the bankProject exists so the program doesn't crash
        File file = new File("bankProject.txt");
        if (iochoice.equals("Y") && file.exists()) {
            System.out.println("Okay, uploading the data now");

            user = uploadSession();
        }


        else {
            if (iochoice.equals("Y") && file.exists() == false) {
                System.out.println("It appears that you don't have a proper 'bankProject.txt' file, so that action cannot be performed");
            }
            System.out.println("Welcome! To start your session, how much money do you have in all?");
            System.out.println();
            user = userDecision();

            System.out.println("Fantastic! Now that your bank account is setup, please select a bank...");
            userBankm = bankDecision(user);

            System.out.println("Great! Now that your bank and personal information (financial) is known, here's your options this session:");
            System.out.println();
        }

        //Options for the user to choose from
        while (true) {
            System.out.println("-------------------------------------------------------------------");
            System.out.println("1. See the funds in your bank");
            System.out.println("2. See your own funds");
            System.out.println("3. Deposit funds into your bank account");
            System.out.println("4. Withdrawal funds from your bank account");
            System.out.println("5. See the statements that you've made this session");
            System.out.println("6. See the overview of your account");
            System.out.println("7. Add an additional bank account");
            System.out.println("8. Remove a bank account");
            System.out.println("9. Change the bank account you're using");
            System.out.println("10. See the personal statements of your current active bank account");
            System.out.println("11. Wipe your personal transaction record");
            System.out.println("12. Wipe your current bank's current record");
            System.out.println("13. Add more money to your 'pocket change'");
            System.out.println("14. Remove money from your 'pocket change'");
            System.out.println("15. End your session");
            System.out.println("-------------------------------------------------------------------");

            System.out.println();

            System.out.println("Which option would you like to choose?");
            int choice;
            while(true) {
                try {
                    choice = input.nextInt();
                    if (choice >= 1 && choice <= 15) {
                        break;
                    }
                } catch (InputMismatchException err ) {
                    input.next();
                }


                System.out.println("That's not a valid input");
                System.out.println("Try again");
                System.out.println();

                System.out.println("-------------------------------------------------------------------");
                System.out.println("1. See the funds in your current bank");
                System.out.println("2. See your personal funds");
                System.out.println("3. Deposit funds into your bank account");
                System.out.println("4. Withdrawal funds from your bank account");
                System.out.println("5. See the statements that you've made this session");
                System.out.println("6. See the overview of your account");
                System.out.println("7. Add an additional bank account");
                System.out.println("8. Remove a bank account");
                System.out.println("9. Change the bank account you're using");
                System.out.println("10. See the personal statements of your current active bank account");
                System.out.println("11. Wipe your personal transaction record");
                System.out.println("12. Wipe your current bank's current record");
                System.out.println("13. Add more money to your 'pocket change'");
                System.out.println("14. Remove money from your 'pocket change'");
                System.out.println("15. End your session");
                System.out.println("-------------------------------------------------------------------");
                System.out.println();

                System.out.println("Which option would you like to choose?");
            }

            if (choice == 15) {
                System.out.println("Before you ago, here is your final overview:");
                System.out.println(user);
                System.out.println();

                System.out.println("Would you like to save your session to continue down the line?");
                System.out.println("Y for yes or N for no");
                
                
                while (true) {
                    try {
                        iochoice = input.next().toUpperCase();

                        if (iochoice.equals("Y") || iochoice.equals("YES") || iochoice.equals("N")
                        || iochoice.equals("NO")) {
                            break;
                        }
                    } catch (InputMismatchException e) {
                        input.next();
                    }
                }

                if (iochoice.equals("Y") || iochoice.equals("YES")) {
                    
                    if ((user.getMoney() + "").indexOf('E') != -1) {
                        System.out.println("Note that you some of your data exceeds the values to properly upload");
                        System.out.println("This may be because some of your values are now interpreted with exponentials");
                        System.out.println("To clarify, this file can be safely downloaded, but cannot be uploaded");
                    }
                    for (int i = 0; i < 3; i++) {
                        if (user.getBank(i) == null) {
                            break;
                        }
                        if ((user.getBank(i).getBankDeposit() + "").indexOf('E') != -1) {
                            System.out.println("Note that you some of your data exceeds the values to properly upload");
                            System.out.println("This may be because some of your values are now interpreted with exponentials");
                            System.out.println("To clarify, this file can be safely downloaded, but cannot be uploaded");
                            break;
                        }
                    }


                    System.out.println("Okay, your session will be created in a txt file now, hold on");
                    saveSession(user);
                    System.out.println("The file should be called 'session.txt'");
                }

                System.out.println();
                System.out.println("Have an excellent day!");
                input.close();
                return;
            }
            options(choice, userBankm, user);
        }

    }

    // A conditional statement is choosen based off of the input that the user gives
    // To see what each choice does look to the options provided previously, starting at line 230
    private static void options(int choice, Bank userBank, User user) {
        if (choice == 1) {
            if (userBank == null) {
                System.out.println("It appears that you don't have a bank account");
                System.out.println("Therefore, this feature cannot work");
                System.out.println();
                return;
            }
            System.out.println("There are $" + userBank.getBankDeposit() + " in " + userBank.getName());
            System.out.println();
            System.out.println("What else would you like to do?");
            System.out.println();
        }
        else if (choice == 2) {
            System.out.println("You currently have $" + user.getMoney() + " as 'pocket' change");
            System.out.println();
            System.out.println("What else would you like to do?");
            System.out.println();
        }
        else if (choice == 3) {
            if (userBank == null) {
                System.out.println("It appears that you don't have a bank account");
                System.out.println("Therefore, this feature cannot work");
                System.out.println();
                return;
            }
            if (user.getMoney() == 0) {
                System.out.println("It appears you don't have any money available to deposite");
                System.out.println("Therefore, this request won't work");
                return;
            }

            System.out.println("Choose the amount to deposit:");
            double val = -1;
            while (val < 0 || user.getMoney() - val < 0) {
                try {
                    val = input.nextDouble();
                    if (val < 0) {
                        System.out.println("Can't deposit negative money");
                        System.out.println("Try again");
                        System.out.println();
                    }
                    else if (user.getMoney() - val < 0) {
                        System.out.println("You don't have that much money");
                        System.out.println("Try again");
                        System.out.println();
                    }
                } catch (InputMismatchException err) {
                    System.out.println("You've entered an invalid input");
                    System.out.println("Please try again");
                    input.next();
                }
            }
            user.deposit(val, userBank);
            System.out.println();
            System.out.println("Your account now has $" + userBank.getBankDeposit());
            System.out.println();
            System.out.println("What else would you like to do?");
            System.out.println();
        }
        else if (choice == 4) {
            if (userBank == null) {
                System.out.println("It appears that you don't have a bank account");
                System.out.println("Therefore, this feature cannot work");
                System.out.println();
                return;
            }
            if (userBank.getBankDeposit() == 0) {
                System.out.println("It appears that your bank doesn't have money");
                System.out.println("Therefore, this request won't work");
                return;
            }

            System.out.println("Choose the amount to withdrawal:");
            double val = -1;
            while (val < 0 || userBank.getBankDeposit() - val < 0) {
                try {
                    val = input.nextDouble();
                    if (val < 0) {
                        System.out.println("Can't withdrawal negative money");
                        System.out.println("Try again");
                        val = input.nextDouble();
                    }
                    else if (userBank.getBankDeposit() - val < 0) {
                        System.out.println("Your current bank doesn't have that much money to withdrawal from");
                        System.out.println("Try again");
                        val = input.nextDouble();
                    }
                } catch (InputMismatchException err) {
                    System.out.println("You've entered an invalid input");
                    System.out.println("Please try again");
                    input.next();
                }
            }
            user.withdrawal(val, userBank);
            System.out.println();
            System.out.println("Your account now has $" + userBank.getBankDeposit());
            System.out.println();
            System.out.println("What else would you like to do?");
            System.out.println();
        }
        else if (choice == 5) {
            System.out.println("Sure, here are your total transactions:");
            System.out.println(user.getTransactions());
            System.out.println();
            System.out.println("What else would you like to do?");
            System.out.println();
        }
        else if (choice == 6) {
            System.out.println(user);
            System.out.println();
            System.out.println("What else would you like to do?");
            System.out.println();
        }
        else if (choice == 7) {
            System.out.println("Of course, select an additional bank");
            System.out.println("Here are the bank accounts you currently have:");
            System.out.println(user.getBankNames());

            if (user.getNumBanks() == 4) {
                System.out.println("It appears you have the max amount of banks");
                System.out.println("Therefore, you cannot add another bank");
                return;
            }

            System.out.println("Please select a bank you don't currently have");
            System.out.println();

            Bank bank = bankDecision(user);
            if (bank == null) {
                System.out.println("You've choosen 'Q'");
                System.out.println("Therefore, this process will now be cancelled");
                System.out.println("Note that if you don't have an account most options won't work");
                System.out.println();
                return;
            }

            System.out.println("Do 1 to switch your current account to the new account and 2 to keep using your current account");


            int option = 0;
            while (option != 1 && option != 2) {
                try {
                option = input.nextInt();

                if (option != 1 && option != 2) {
                    System.out.println("You didn't enter a valid response");
                    System.out.println("Select 1 to switch your current account to the new account and 2 to keep using your current account");
                    System.out.println();
                }

                } catch (InputMismatchException err) {
                    System.out.println("You've entered an option that isn't valid");
                    System.out.println("Please try again");
                    input.next();
                }
            }
            if (option == 2 && userBank == null) {
                System.out.println("Alright, your current bank status will still be set to none then");
                return;
            }
            
            if (option == 1) {
                userBankm = bank;
                userBank = bank;
            }
            
            if (option == 2) {
                System.out.println("Okay nothing will change then");
            }
            

            System.out.println("You're now using a " + userBank.getName() + " bank account");
            System.out.println("You now have $" + user.getMoney() + " free to spend");
            System.out.println();
            System.out.println("What else would you like to do?");
            System.out.println();
        }
        else if (choice == 8) {
            System.out.println("You have choosen the option to delete an account");
            System.out.println("Here are the banks that you currently have:");
            System.out.println(user.getBankNames());

            if (userBank == null) {
                System.out.println("It appears that you don't have a bank account");
                System.out.println("Therefore, this feature cannot work");
                System.out.println();
                return;
            }

            if (user.getNumBanks() == 0) {
                System.out.println("It appears that you don't have any bank account available anymore");
                System.out.println("Therefore, you must make a new account if you are to proceed with any of the choices you can make");
                System.out.println();
                bankDecision(user);
            }

            System.out.println("Which would you like to delete?");
            System.out.println("B for BNY Mellon");
            System.out.println("CH for Chase");
            System.out.println("CA for Capital One");
            System.out.println("Q to back out of the process");
            System.out.println();
            String toDelete = "z";


            while (toDelete.equals("B") == false && toDelete.equals("BNY MELLON") == false && toDelete.equals("CH") == false 
            && toDelete.equals("CHASE") == false && toDelete.equals("CA") == false && toDelete.equals("CAPITAL ONE") == false && toDelete.equals("Q") == false) {
                try {
                    toDelete = input.next().toUpperCase();

                    if (toDelete.equals("B") == false && toDelete.equals("BNY MELLON") == false && toDelete.equals("CH") == false 
                    && toDelete.equals("CHASE") == false && toDelete.equals("CA") == false && toDelete.equals("CAPITAL ONE") == false && toDelete.equals("Q") == false) {
                        System.out.println("It appears you didn't enter a valid response, please try again");
                        System.out.println("B for BNY Mellon");
                        System.out.println("CH for Chase");
                        System.out.println("CA for Capital One");
                        System.out.println("Q to back out of the process");
                        System.out.println();
                    }

                } catch (InputMismatchException err) {
                    System.out.println("Please enter a valid input");
                    input.next();
                }
            }
            if (toDelete.equals("B") || toDelete.equals("BNY MELLON")) {
                toDelete = "BNY Mellon";
            }
            else if (toDelete.equals("Q")) {
                System.out.println("Alright, you're now backing out of this process");
                return;
            }
            else if (toDelete.equals("CH") || toDelete.equals("Chase")) {
                toDelete = "Chase";
            }
            else {
                toDelete = "Capital One";
            }

            if (toDelete.equals(userBank.getName())) {
                System.out.println("The account that you're removing is the one you're currently using");
                System.out.println("Are you sure you want to delete your current account?");
                System.out.println();
                System.out.println("Y for yes N for no");
                System.out.println();
                String option = "";
                while (!option.equals("Y") && !option.equals("N")) {
                    try {
                        option = input.next().toUpperCase();

                        if (!option.equals("Y") && !option.equals("N")) {
                            System.out.println("It appears you've choosen an invalid response");
                            System.out.println("Please try again");
                            System.out.println("Y for yes N for no");
                            System.out.println();
                        }
                    } catch (InputMismatchException err) {
                        System.out.println("Please enter a valid input");
                        input.next();
                    }
                }
                System.out.println("Alright, your current account will proceed to be deleted");
                user.removeBank(toDelete);
                if (user.getNumBanks() == 0) {
                    System.out.println("Note that you don't have a bank account anymore");
                    System.out.println("Most features won't be available to you until you create a new bank account");
                    System.out.println("You now have " + user.getMoney() + " (got money back from removed bank)");
                    System.out.println();
                    System.out.println("What else would you like to do?");
                    System.out.println();
                    userBankm = null;
                    return;
                }

                System.out.println("Since you've deleted your new account, you must select a new current account");
                
                System.out.println("Here are the bank accounts you currently have:");
                System.out.println(user.getBankNames());
                System.out.println();
                System.out.println("Pick 1 for the first option, 2 for the second option, and 3 for the third option");
                int options;
                while (true) {
                    try {
                        options = input.nextInt();
                        if (options < 1 || options > 3) {
                            System.out.println("You did not pick an actual option");
                            System.out.println("Please try again");
                        }
                        else if (user.getBank(options - 1) == null) {
                            System.out.println("You have choosen an option in which there isn't a bank that's set up");
                            System.out.println("Please try again");
                            System.out.println("Pick 1 for the first option, 2 for the second option, and 3 for the third option");
                        }
                        else {
                            break;
                        }
                    } catch (InputMismatchException err) {
                        System.out.println("Please enter a valid option");
                        input.next();
                    }
                }
                userBankm = user.getBank(options - 1);
            }
            else {
                user.removeBank(toDelete);
            }
            System.out.println();
            System.out.println("The banks that you currently have are:");
            System.out.println(user.getBankNames());
            System.out.println("You now have " + user.getMoney() + " (got money back from removed bank)");
            System.out.println();
            System.out.println("What else would you like to do?");
            System.out.println();
        }
        else if (choice == 9) {
            System.out.println("You have choosen to switch the bank account your using");

            if (userBank == null) {
                System.out.println("It appears that you don't have a bank account");
                System.out.println("Therefore, this feature cannot work");
                System.out.println();
                return;
            }

            if (user.getNumBanks() == 0) {
                System.out.println("It appears that you have deleted your last bank for there are no available banks");
                System.out.println("Therefore, this choice cannot work");
                return;
            }

            System.out.println("Here are the bank accounts you currently have:");
            System.out.println(user.getBankNames());
            System.out.println();
            System.out.println("Pick 1 for the first option, 2 for the second option, and 3 for the third option");
            int option = -1;
            while (true) {
                try {
                    option = input.nextInt();

                    if (option < 1 || option > 3) {
                        System.out.println("You did not pick an actual option");
                        System.out.println("Please try again");
                    }
                    if (user.getBank(option) == null) {
                        System.out.println("You have choosen an option in which there isn't a bank that's set up");
                        System.out.println("Please try again");
                    }
                    else {
                        break;
                    }
                } catch (InputMismatchException err) {
                    System.out.println("Please enter a valid input");
                    input.next();
                }
            }
            userBank = user.getBank(option - 1);
            System.out.println("Your curent bank is now " + userBank.getName());
            System.out.println();
            System.out.println("What else would you like to do?");
            System.out.println();
        }
        else if (choice == 10) {
            if (userBank == null) {
                System.out.println("It appears that you don't have an account to use");
                System.out.println("Therefore, this option won't work");
                System.out.println();
                return;
            }
            System.out.println("Of course, here are the transactions with your current account " + userBank.getName() + ":");
            System.out.println();
            System.out.println(userBank.getTransactions());
            System.out.println();
            System.out.println("What else would you like to do?");
            System.out.println();
        }
        else if (choice == 11) {
            System.out.println("You've choosen the option to wipe your total transactions");
            System.out.println("Is this what you'd like to do?");
            System.out.println();
            System.out.println("Put Y for yes or N for no");
            String options = "";
            System.out.println();

            while (!options.equals("Y") && !options.equals("YES") && !options.equals("N")
            && !options.equals("NO")) {
                try {
                    options = input.next().toUpperCase();

                    if (!options.equals("Y") && !options.equals("YES") && !options.equals("N")
                    && !options.equals("NO")) {
                        System.out.println("It appears you've entered an invalid input");
                        System.out.println("Please try again");
                    }
                } catch (InputMismatchException err) {
                    System.out.println("Please enter a valid input");
                    input.next();
                }
            }
            if (options.equals("Y") || options.equals("YES")) {
                System.out.println("Alright, your total transactions will now be wiped...");
                user.wipeTransactions();
                System.out.println("Your transactions have been fully wiped");
            }
            else {
                System.out.println("Alright, the process has been cancelled");
            }
        }
        else if (choice == 12) {
            if (userBank == null) {
                System.out.println("It appears that you don't have an available bank account");
                System.out.println("Therefore, this option won't work");
                System.out.println();
                return;
            }
            System.out.println("You've choosen to wipe the transactions of your current bank");
            System.out.println("Is this what you'd like to do?");
            System.out.println();
            System.out.println("Put Y for yes or N for no");
            String options = "";
            System.out.println();

            while (!options.equals("Y") && !options.equals("YES") && !options.equals("N")
            && !options.equals("NO")) {
                try {
                    options = input.next().toUpperCase();

                    if (!options.equals("Y") && !options.equals("YES") && !options.equals("N")
                    && !options.equals("NO")) {
                        System.out.println("It appears you've entered an invalid input");
                        System.out.println("Please try again");
                    }
                } catch (InputMismatchException err) {
                    System.out.println("Please enter a valid input");
                    input.next();
                }
            }
            if (options.equals("Y") || options.equals("YES")) {
                System.out.println("Alright, your current banks transactions will now be wiped...");
                userBank.wipeTransactions();
                System.out.println("Your bank " + userBank.getName() + " transactions have been fully wiped");
            }
            else {
                System.out.println("Alright, the process has been cancelled");
            }
        }
        else if (choice == 13) {
            System.out.println("You've choosen the option to add more money to your 'pocket change'");
            System.out.println("To confirm, please select Y for yes or N for no");
            String options = "";

            while (!options.equals("N") && !options.equals("NO") && !options.equals("Y") && !options.equals("YES")) {
                try {
                    options = input.next().toUpperCase();
                    
                    if (!options.equals("N") && !options.equals("NO") && !options.equals("Y") && !options.equals("YES")) {
                        System.out.println("Please enter a valid input");
                        System.out.println("Please select Y for yes or N for no");
                    }
                } catch (InputMismatchException err) {
                    System.out.println("Please enter a valid input");
                    input.next();
                }
            }

            if (options.equals("N") || options.equals("NO")) {
                System.out.println("Alright, we'll now cancel the request");
                System.out.println();
                return;
            }
            else {
                System.out.println("Alright, how much money do you want to add to your personal funds?");
                double vals = -1;

                while (vals < 0) {
                    try {
                        vals = input.nextDouble();

                        if (vals < 0) {
                            System.out.println("You can't add negative money to your 'pocket change'");
                            System.out.println("Please try again");
                        }
                    } catch (InputMismatchException err) {
                        System.out.println("Please enter a valid input");
                        input.next();
                    }
                }

                System.out.println("Alright, now adding your money to 'pocket change'");
                user.changeMoney(vals);
                System.out.println();
                System.out.println("Your 'pocket' change is now $" + user.getMoney());
                System.out.println();
            }
        }
        else {
            System.out.println("You've choosen the option to remove money to your 'pocket change'");
            System.out.println("To confirm, please select Y for yes or N for no");
            String options = "";

            while (!options.equals("N") && !options.equals("NO") && !options.equals("Y") && !options.equals("YES")) {
                try {
                    options = input.next().toUpperCase();

                    if (!options.equals("N") && !options.equals("NO") && !options.equals("Y") && !options.equals("YES")) {
                        System.out.println("Please enter a valid input");
                        System.out.println("Please select Y for yes or N for no");
                    }
                } catch (InputMismatchException err) {
                    System.out.println("Please enter a valid input");
                    input.next();
                }
            }

            if (options.equals("N") || options.equals("NO")) {
                System.out.println("Alright, we'll now cancel the request");
                System.out.println();
                return;
            }
            else {
                System.out.println("Alright, how much money do you want to remove from your personal funds?");
                System.out.println();
                double vals = -1;

                while (vals < 0) {
                    try {
                        vals = input.nextDouble();

                        if (vals < 0) {
                            System.out.println("You can't remove negative money to your 'pocket change'");
                            System.out.println("Please try again");
                        }
                    } catch (InputMismatchException err) {
                        System.out.println("Please enter a valid input");
                        input.next();
                    }
                }

                System.out.println("Alright, now removing that amount of money from your 'pocket change'");
                user.changeMoney(-vals);
                System.out.println();
                System.out.println("Your 'pocket' change is now $" + user.getMoney());
                System.out.println();
            }
        }
    }

    // Helper method to setup the User object for a given session
    private static User userDecision() {
        System.out.println("Enter the total amount of money you have:");
        double change;
        while (true) {
            try {
                change = input.nextDouble();
                if (change < 0) {
                    System.out.println("You can't have negative money...");
                    System.out.println("Try again");
                    System.out.println();
                }
                else {
                    break;
                }
            } catch (InputMismatchException err){
                System.out.println("You've entered an invalid input");
                System.out.println("Please try again");
                input.next();
            }
        }
            
        return new User(change);
    }

    // Helper method to setup the funds for one's bank account
    private static double bankFundsChoice(User user) {
        double bankFunds;
        while (true) {
            try {
                bankFunds = input.nextDouble();
                if (bankFunds < 0) {
                    System.out.println("You can't have negative money in your account...");
                    System.out.println("Try again");
                    System.out.println();
                }
                else if (user.getMoney() - bankFunds < 0) {
                    System.out.println("You don't have enough money to put that much in...");
                    System.out.println("Try again");
                    System.out.println();
                }
                else {
                    break;
                }
            } catch (InputMismatchException err) {
                System.out.println("You've entered an invalid input");
                System.out.println("Please try again");
                input.next();
            }
        }
        return bankFunds;
    }

    // Helper method to properly setup a bank account for a user
    private static Bank bankDecision(User user) {
        if (user.getNumBanks() > 3) {
            System.out.println("It appears you have made a bank account for each of the banks...");
            System.out.println("Therefore, you are unable to create a new account unless you delete a bank account");
            return null;
        }

        System.out.println("B for BNY Mellon");
        System.out.println("CH for Chase");
        System.out.println("CA for Capital One");
        System.out.println("Q to back out of this process");
        System.out.println("Note that if you haven no bank accounts available, option Q will not work");
        System.out.println();
        String choice = input.next().toUpperCase();
        while (true) {
            while(choice.equals("B") == false && choice.equals("CH") == false && choice.equals("CA") == false && choice.equals("Q") == false) {
                System.out.println("Your input was none of the choices given");
                System.out.println("Try again...");
                System.out.println();
                System.out.println("B for BNY Mellon");
                System.out.println("CH for Chase");
                System.out.println("CA for Capital One");
                System.out.println("Q to back out of this process");
                System.out.println("Note that if you haven no bank accounts available, option Q will not work");
                System.out.println();
                choice = input.next().toUpperCase();
            }
            if (choice.equals("B") && BNYMellon.getNumBNY() >= 1) {
                System.out.println("It seems you've tried to make a new account for a bank that you've already made an account in");
                System.out.println("Please select a different bank or exit this process");
                System.out.println();
                System.out.println("B for BNY Mellon");
                System.out.println("CH for Chase");
                System.out.println("CA for Capital One");
                System.out.println("Q to back out of this process");
                System.out.println("Note that if you haven no bank accounts available, option Q will not work");
                choice = input.next().toUpperCase();
            }
            else if (choice.equals("CH") && Chase.getNumChase() >= 1) {
                System.out.println("It seems you've tried to make a new account for a bank that you've already made an account in");
                System.out.println("Please select a different bank or exit this process");
                System.out.println();
                System.out.println("B for BNY Mellon");
                System.out.println("CH for Chase");
                System.out.println("CA for Capital One");
                System.out.println("Q to back out of this process");
                System.out.println("Note that if you haven no bank accounts available, option Q will not work");
                choice = input.next().toUpperCase();
            }
            else if (choice.equals("CA") && CapitalOne.getNumCapitalOne() >= 1) {
                System.out.println("It seems you've tried to make a new account for a bank that you've already made an account in");
                System.out.println("Please select a different bank or exit this process");
                System.out.println();
                System.out.println("B for BNY Mellon");
                System.out.println("CH for Chase");
                System.out.println("CA for Capital One");
                System.out.println("Q to back out of this process");
                System.out.println("Note that if you haven no bank accounts available, option Q will not work");
                choice = input.next().toUpperCase();
            }
            else {
                break;
            }
        }
        if (choice.equals("B") ) {
            System.out.println("Great Choice! What's the amount of money your account will receive?");
            double bankFunds = bankFundsChoice(user);

            Bank bank = new BNYMellon(bankFunds);
            user.addBank(bank);
            return bank;
        }

        else if (choice.equals("CH")) {
            System.out.println("Great Choice! What's the amount of money your account will receive?");
            double bankFunds = bankFundsChoice(user);
           
            Bank bank = new Chase(bankFunds);
            user.addBank(bank);
            return bank;
        }

        else if (choice.equals("Q")) {
            return null;
        }

        else {
            System.out.println("Great Choice! What's the amount of money your account will receive?");
            double bankFunds = bankFundsChoice(user);
            
            Bank bank = new CapitalOne(bankFunds);
            user.addBank(bank);
            return bank;
        }
    }
}