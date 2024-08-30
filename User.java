public class User {
    private double money;
    private Node transactions;
    private Bank[] banks;
    private int counter;


    //implementing a linked list to chain the user's transactions
    private class Node {
        private Object statement;
        private Node next;

        public Node(Object statement, Node next) {
            this.statement = statement;
            this.next = next;
        }
    }

    // Sets the Transaction Linked List to a new null node
    public User(double change) {
        money = change;
        transactions = new Node(null,null);
        banks = new Bank[3];
        counter = 0;
    }

    // The getters are below

    public double getMoney() {
        return money;
    }

    public void changeMoney(double val) {
        money += val;
    }

    public int getNumBanks() {
        return counter;
    }

    public Bank getBank(int i) {
        return banks[i];
    }

    public String getBankNames() {
        String s = "[";
        for (int i = 0; i < counter; i++) {
            if (banks[i] != null) {
                if (i == counter - 1) {
                    s += banks[i].getName() + "]";
                    return s;
                }
                s += banks[i].getName() + ", ";
            }
        }
        s+="There are currently no bank accounts in your name]";
        return s;
    }

    public void setTransactions(double[] arr) {
        Node trav = transactions;
        int counter = 0;
        while (counter != arr.length - 1) {
            trav.statement = arr[counter];
            trav.next = new Node(null,null);
            trav = trav.next;
            counter++;
        }
        trav.statement = arr[counter];
    }

    //add an additional bank into the bank array and increments where the counter is looking
    public void addBank(Bank bank) {
        if (bank == null) {
            return;
        }
        banks[counter] = bank;
        changeMoney(-bank.getBankDeposit());
        counter++;
    }

    //for uploading sessions we use this method instead so that money doesn't unnecesarrily lose money from the bank
    public void uploadAddBank(Bank bank) {
        if (bank == null) {
            return;
        }
        banks[counter] = bank;
        counter++;
    }

    public void removeBank(String name) {
        if (name == null) {
            return;
        }
        int start = 0;
        for (int i = 0; i < banks.length; i++) {
            if (banks[i].getName().equals(name)) {
                changeMoney(banks[i].getBankDeposit());
                start = i;
                break;
            }
        }
        banks[start].lowerBankNum();
        while (start < banks.length - 1) {
            banks[start] = banks[start + 1];
            start++;
        }
        banks[banks.length - 1] = null;
        counter--;
    }

    public void deposit(double val, Bank bank) {
        // if the interface had no error-checking, this would be in play
        // but the program needs to be flexible for invalid inputs from
        // the client
        /*
        if (money - val < 0) {
            throw new IllegalArgumentException("You can't give money you don't have!");
        }
        */
        bank.changeFundsUp(val);
        money-= val;
        addTransaction(-val);
        
    }

    public void withdrawal(double val, Bank bank) {
        /*taking advantage of the error checking the bank classes have
        so I don't have to error check again*/
        bank.changeFundsDown(val);
        money+= val;
        addTransaction(-val);
    }

    // Adds a transaction to the user's linked list
    private void addTransaction(double statement) {
        if (transactions.statement == null) {
            transactions.statement = statement;
            return;
        }
        Node trav = transactions;
        while (trav.next != null) {
            trav = trav.next;
        }
        trav.next = new Node(statement, null);
    }

    private String convertTransaction(Object[] arr) {
        String s = "[";
        for (int i = 0; i < arr.length; i++) {
            if (i == arr.length - 1) {
                s += arr[i] + "]";
            }
            else {
                s += arr[i] + ", ";
            }
        }
        return s;
    }

    public void wipeTransactions() {
        transactions.statement = null;
        transactions.next = null;
    }

    public String getTransactions() {
        Object[] arr;
        String s;
        if (transactions.statement == null) {
            //there hasn't been any transactions made
            arr = new Object[1];
            arr[0] = "You haven't made any transactions yet";
            s = convertTransaction(arr);
            return s;
        }
        Node trav = transactions;
        //IGNORE THIS FOR RIGHT NOW  set counter to -1 to account for the "dummy node" in the constructor
        int counter = 0;
        while (trav != null) {
            counter++;
            trav = trav.next;
        }
        arr = new Object[counter];
        trav = transactions;
        counter = 0;
        while (trav != null) {
            arr[counter] = trav.statement;
            counter++;
            trav = trav.next;
        }
        s = convertTransaction(arr);
        return s;
    }

    // The toString method for when the client wants an overview of their account
    public String toString() {
        String s = "";
        s+= "The money you have on you is $" + getMoney() + "\n";
        s+= "You're list of every transactions is: " + getTransactions() + "\n";
        s+= "Here are the the specific transactions from each bank you have:" + "\n";
        if (counter == 0) {
            s+= "[You don't have any banks right now]";
            return s;
        }
        for (int i = 0; i < banks.length; i++) {
            if (banks[i] != null) {
                s+= banks[i].getName() + ": " + banks[i].getTransactions() + "\n";
            }
        }
        return s;
    }


}