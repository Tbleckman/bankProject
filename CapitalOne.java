public class CapitalOne implements Bank{
    private double deposit;
    private static int numCapitalOne = 0;
    private Node bank_transactions;

    // Note that I'm setting up my own Node attribute so that I don't 
    // Have to worry about constantly resizing an array for any given transactions
    // This will be further explained later

    private class Node {
        private Object statement;
        private Node next;
        public Node(Object statement, Node next) {
            this.statement = statement;
            this.next = next;
        }
    }

    public CapitalOne(double deposit) {
        /* Old idea that I had in the beginning of the project
        if i wanted to bring it back this code would be useful

        if (numCapitalOne == 1) {
            throw new IllegalArgumentException("Can't have more than one Capital One!");
        }
        */
        this.deposit = deposit;
        numCapitalOne++;
        bank_transactions = new Node(null, null);
    }

    // The getters and interace methods are below

    public double getBankDeposit() {
        return deposit;
    }

    public static int getNumCapitalOne() {
        return numCapitalOne;
    }

    public String getName() {
        return "Capital One";
    }

    public void lowerBankNum() {
        numCapitalOne--;
    }

    public void changeFundsUp(double val) {
        deposit += val;
        addTransaction(val);
    }

    public void changeFundsDown(double val) {
        if (deposit - val < 0) {
            System.out.println("You're gonna need a loan (get more money)...");
            System.out.println();
            return;
        }
        deposit-= val;
        addTransaction(-val);
    }

    public void setTransactions(double[] arr) {
        Node trav = bank_transactions;
        int counter = 0;
        while (counter != arr.length - 1) {
            trav.statement = arr[counter];
            trav.next = new Node(null,null);
            trav = trav.next;
            counter++;
        }
        trav.statement = arr[counter];
    }


    // Chains another node to the current list
    // With these bank transactions, we don't have a limit of transactions that can happen
    // Therefore, instead of wasting memory, for our purposes it's more efficient to hold
    // data through linked lists, hence why I'm using it
    private void addTransaction(double val) {
        if (bank_transactions.statement == null) {
            bank_transactions.statement = val;
            return;
        }
        Node trav = bank_transactions;
        while (trav.next != null) {
            trav = trav.next;
        }
        trav.next = new Node(val, null);
    }

    //Private helper method to turn a bank transaction object into a string
    private String convertTransactions(Object[] arr) {
        String s = "[";
        for (int i = 0; i <arr.length; i++) {
            if (i == arr.length - 1) {
                s += arr[i] + "]";
                return s;
            }
            s += arr[i] + ", ";
        }
        return s;
    }


    // To simply erase the transaction history of a given bank object
    // You can simply change the head of the linked list to null, resetting it
    public void wipeTransactions() {
        bank_transactions = new Node(null, null);
    }



    // Gets the transaction history of a given bank object
    // By converting the linked list into an array and then
    // Utilizes the private convertTransactions() method
    // To turn the array into string to then be returned
    public String getTransactions() {
        Object[] arr;
        int counter = 0;
        if (bank_transactions.statement == null) {
            arr = new Object[1];
            arr[0] = "You haven't made any transactions yet";
            String s = convertTransactions(arr);
            return s;
        }

        Node trav = bank_transactions;
        while (trav != null) {
            counter++;
            trav = trav.next;
        }
        arr = new Object[counter];
        trav = bank_transactions;
        counter = 0;
        while (trav != null) {
            arr[counter] = trav.statement;
            counter++;
            trav = trav.next;
        }
        String s = convertTransactions(arr);
        return s;
    }
}
