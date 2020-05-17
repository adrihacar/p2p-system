import java.util.Arrays;


public class Test {

    public static final String _server = "localhost";
    public static final int _port = 1234;

    public static final String user1 = "usuarioA";
    public static final String user2 = "usuarioB";
    public static final String nonExistent = "empty";
    public static final String file_1 = "pizza.txt";
    public static final String file_2 = "cola.txt";

    /*
        Test ID: 1
        Objective: Verify FR 1.1, FR 1.2, FR 1.3, NFR 1
        Procedure: Register two valid users, 1 invalid (max name length exceeded)
        Expected output: 0 0 1 2
    */
    public static int test_1(){
        //Auxiliary variables
        char[] bname = new char[257];
        String name = String.valueOf(bname);

        //Variables to assert the test result
        int[] expected_out = {0, 0, 1, 2};
        int[] server_answers = new int[expected_out.length];
        

        server_answers[0] = Client.register(user1);
        server_answers[1] = Client.register(user2);
        server_answers[2] = Client.register(user2);
        server_answers[3] = Client.register(name);

        if(Arrays.equals(server_answers, expected_out)){
            System.out.println("TEST 1: SUCCESS");
            return 0;
        }else{
            System.out.println("TEST 1: FAIL");
            return 1;
        }
        
    }

    /*
        Test ID: 2
        Objective: Verify FR 3.1, FR 3.2, FR 3.3
        Procedure: Connect a non-existent user and the smae user twice
        Expected output: 1 0 2
    */
    public static int test_2(){

        //Variables to assert the test result
        int[] expected_out = {1, 0, 2};
        int[] server_answers = new int[expected_out.length];

        server_answers[0] = Client.connect(nonExistent);
        server_answers[1] = Client.connect(user1);
        server_answers[2] = Client.connect(user1);

        if(Arrays.equals(server_answers, expected_out)){
            System.out.println("TEST 2: SUCCESS");
            return 0;
        }else{
            System.out.println("TEST 2: FAIL");
            return 1;
        }
    }


    /*
        Test ID: 3
        Objective: Verify FR 4.1, FR 4.4, FR 4.5
        Procedure: Create a file with no name, and three more, two of them with the same name
        Expected output: 0 3 0
    */
    public static int test_3(){
        //Variables to assert the test result
        int[] expected_out = {0, 3, 0};
        int[] server_answers = new int[expected_out.length];

        server_answers[0] = Client.publish(file_1, "Cheesy");
        server_answers[1] = Client.publish(file_1, "Carbonara");
        server_answers[2] = Client.publish(file_2, "bubbles");


        if(Arrays.equals(server_answers, expected_out)){
            System.out.println("TEST 3: SUCCESS");
            return 0;
        }else{
            System.out.println("TEST 3: FAIL");
            return 1;
        }
    }



    /*
        Test ID: 4
        Objective: Verify FR 6.1
        Procedure: List the users currently connected (only usuarioA)
        Expected output: 0
    */
    public static int test_4(){
        //Variables to assert the test result
        int[] expected_out = {0};
        int[] server_answers = new int[expected_out.length];

        server_answers[0] = Client.list_users();

        if(Arrays.equals(server_answers, expected_out)){
            System.out.println("TEST 4: SUCCESS");
            return 0;
        }else{
            System.out.println("TEST 4: FAIL");
            return 1;
        }
    }

    /*
        Test ID: 5
        Objective: Verify FR 5.1, FR 5.3, FR 7.1
        Procedure: list content, eliminate file and list content again
        Expected output: 0 0 3 0
    */
    public static int test_5(){
        //Variables to assert the test result
        int[] expected_out = {0, 0, 3, 0};
        int[] server_answers = new int[expected_out.length];

        server_answers[0] = Client.list_content(user1);
        server_answers[1] = Client.delete(file_2); 
        server_answers[2] = Client.delete(file_2);
        server_answers[3] = Client.list_content(user1);

        if(Arrays.equals(server_answers, expected_out)){
            System.out.println("TEST 5: SUCCESS");
            return 0;
        }else{
            System.out.println("TEST 5: FAIL");
            return 1;
        }
    }

    /*
        Test ID: 6
        Objective: Verify FR 7.4
        Procedure: list content of a non-existent user
        Expected output: 3
    */
    public static int test_6(){
        //Variables to assert the test result
        int[] expected_out = {3};
        int[] server_answers = new int[expected_out.length];

        server_answers[0] = Client.list_content(nonExistent);

        if(Arrays.equals(server_answers, expected_out)){
            System.out.println("TEST 6: SUCCESS");
            return 0;
        }else{
            System.out.println("TEST 6: FAIL");
            return 1;
        }
    }

    public static void main (String[] args){
        Client._port = _port;
        Client._server = _server;

        int passed = 0;
        final int total = 6;
        
        System.out.println("Executing Test Plan");
        if(test_1() == 0) passed++;
        if(test_2() == 0) passed++;
        if(test_3() == 0) passed++;
        if(test_4() == 0) passed++;
        if(test_5() == 0) passed++;
        if(test_6() == 0) passed++;
        System.out.println(passed + "/" + total + " test passed");

    }

}