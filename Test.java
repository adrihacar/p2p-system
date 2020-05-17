import java.util.Arrays;


public class Test {

    public static final String _server = "localhost";
    public static final int _port = 12344;

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
        server_answers[1] = Client.register(name);

        for (int i = 0; i < server_answers.length; i++){
            System.out.println();
        }

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
        Expected output: 2 0 1
    */
    public static int test_2(){

        //Variables to assert the test result
        int[] expected_out = {2, 0, 1};
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
        Expected output: 4 0 3 0
    */
    public static int test_3(){
        //Variables to assert the test result
        int[] expected_out = {4, 0, 3, 0};
        int[] server_answers = new int[expected_out.length];

        server_answers[0] = Client.publish ( "", nonExistent);
        server_answers[1] = Client.publish(file_1, "Cheesy");
        server_answers[2] = Client.publish(file_1, "Carbonara");
        server_answers[3] = Client.publish(file_2, "bubbles");


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

    /*
        Test ID: 7
        Objective: Verify FR 9.1, FR 9.2
        Procedure: get a non existent file and a published file from another client 
        Expected output: 1 0
    */
    public static int test_7(){
        //Variables to assert the test result
        int[] expected_out = {1, 0};
        int[] server_answers = new int[expected_out.length];

        server_answers[0] = Client.get_file(user1, nonExistent, "test7.txt");
        server_answers[1] = Client.get_file(user1, file_1, "test7.txt");

        if(Arrays.equals(server_answers, expected_out)){
            System.out.println("TEST 7: SUCCESS");
            return 0;
        }else{
            System.out.println("TEST 7: FAIL");
            return 1;
        }
    }

    /*
        Test ID: 8
        Objective: Verify FR 8.1, FR 8.2, FR 8.3
        Procedure: disconnect a non existent user, and the same user twice
        Expected output: 1 2 0
    */
    public static int test_8(){
        //Variables to assert the test result
        int[] expected_out = {1, 2, 0};
        int[] server_answers = new int[expected_out.length];

        server_answers[0] = Client.disconnect(nonExistent);
        server_answers[1] = Client.disconnect(user1);
        server_answers[2] = Client.disconnect(user1);

        if(Arrays.equals(server_answers, expected_out)){
            System.out.println("TEST 8: SUCCESS");
            return 0;
        }else{
            System.out.println("TEST 8: FAIL");
            return 1;
        }
    }

    /*
        Test ID: 9
        Objective: Verify FR 4.3
        Procedure: Try to publish a file while being disconnected
        Expected output: 2
    */
    public static int test_9(){
        //Variables to assert the test result
        int[] expected_out = {2};
        int[] server_answers = new int[expected_out.length];

        server_answers[0] = Client.publish(file_2, "description");

        if(Arrays.equals(server_answers, expected_out)){
            System.out.println("TEST 9: SUCCESS");
            return 0;
        }else{
            System.out.println("TEST 9: FAIL");
            return 1;
        }
    }

    /*
        Test ID: 10
        Objective: Verify FR 5.3
        Procedure: Try to delete a file while being disconnected
        Expected output: 2
    */
    public static int test_10(){
        //Variables to assert the test result
        int[] expected_out = {2};
        int[] server_answers = new int[expected_out.length];

        server_answers[0] = Client.delete(file_1);

        if(Arrays.equals(server_answers, expected_out)){
            System.out.println("TEST 10: SUCCESS");
            return 0;
        }else{
            System.out.println("TEST 10: FAIL");
            return 1;
        }
    }

    /*
        Test ID: 11
        Objective: Verify FR 6.3
        Procedure: Try to list users while being disconnected
        Expected output: 2
    */
    public static int test_11(){
        //Variables to assert the test result
        int[] expected_out = {2};
        int[] server_answers = new int[expected_out.length];

        server_answers[0] = Client.list_users();

        if(Arrays.equals(server_answers, expected_out)){
            System.out.println("TEST 11: SUCCESS");
            return 0;
        }else{
            System.out.println("TEST 11: FAIL");
            return 1;
        }
    }

    /*
        Test ID: 12
        Objective: Verify FR 7.3
        Procedure: Try to list content while being disconnected
        Expected output: 2
    */
    public static int test_12(){
        //Variables to assert the test result
        int[] expected_out = {2};
        int[] server_answers = new int[expected_out.length];

        server_answers[0] = Client.list_content(user1);

        if(Arrays.equals(server_answers, expected_out)){
            System.out.println("TEST 12: SUCCESS");
            return 0;
        }else{
            System.out.println("TEST 12: FAIL");
            return 1;
        }
    }

    /*
        Test ID: 13
        Objective: Verify FR 9.3
        Procedure: try to get file while being disconnected
        Expected output: 2
    */
    public static int test_13(){
        //Variables to assert the test result
        int[] expected_out = {2};
        int[] server_answers = new int[expected_out.length];

        server_answers[0] = Client.get_file(user1, file_1, "test13.txt");
        if(Arrays.equals(server_answers, expected_out)){
            System.out.println("TEST 13: SUCCESS");
            return 0;
        }else{
            System.out.println("TEST 13: FAIL");
            return 1;
        }
    }

    /*
        Test ID: 14
        Objective: Verify FR 2.1, FR 2.2
        Procedure: Unregister all registered users and a non existent one
        Expected output: 0 0 1
    */
    public static int test_14(){
        //Variables to assert the test result
        int[] expected_out = {0, 0, 1};
        int[] server_answers = new int[expected_out.length];

        server_answers[0] = Client.unregister(user1); 
        server_answers[1] = Client.unregister(user2);
        server_answers[2] = Client.unregister(nonExistent);

        if(Arrays.equals(server_answers, expected_out)){
            System.out.println("TEST 14: SUCCESS");
            return 0;
        }else{
            System.out.println("TEST 14: FAIL");
            return 1;
        }
    }

    /*
        Test ID: 15
        Objective: Verify FR 4.2
        Procedure: Publish a file while being unregister
        Expected output: 1
    */
    public static int test_15(){
        //Variables to assert the test result
        int[] expected_out = {1};
        int[] server_answers = new int[expected_out.length];

        server_answers[0] = Client.publish(file_1, "description");

        if(Arrays.equals(server_answers, expected_out)){
            System.out.println("TEST 15: SUCCESS");
            return 0;
        }else{
            System.out.println("TEST 15: FAIL");
            return 1;
        }
    }

    /*
        Test ID: 16
        Objective: Verify FR 5.2
        Procedure: Try to delete a file while being unregister
        Expected output: 1
    */
    public static int test_16(){
        //Variables to assert the test result
        int[] expected_out = {1};
        int[] server_answers = new int[expected_out.length];

        server_answers[0] = Client.delete(file_1);

        if(Arrays.equals(server_answers, expected_out)){
            System.out.println("TEST 16: SUCCESS");
            return 0;
        }else{
            System.out.println("TEST 16: FAIL");
            return 1;
        }
    }

    /*
        Test ID: 17
        Objective: Verify FR 6.2
        Procedure: Try to list users while being unregistered
        Expected output: 1
    */
    public static int test_17(){
        //Variables to assert the test result
        int[] expected_out = {1};
        int[] server_answers = new int[expected_out.length];

        server_answers[0] = Client.list_users();

        if(Arrays.equals(server_answers, expected_out)){
            System.out.println("TEST 17: SUCCESS");
            return 0;
        }else{
            System.out.println("TEST 17: FAIL");
            return 1;
        }
    }

    /*
        Test ID: 18
        Objective: Verify FR 7.2
        Procedure: Try to list content of a unregister user, while being unregistered
        Expected output: 1
    */
    public static int test_18(){
        //Variables to assert the test result
        int[] expected_out = {1};
        int[] server_answers = new int[expected_out.length];

        server_answers[0] = Client.list_content(user1);

        if(Arrays.equals(server_answers, expected_out)){
            System.out.println("TEST 18: SUCCESS");
            return 0;
        }else{
            System.out.println("TEST 18: FAIL");
            return 1;
        }
    }

    public static void main (String[] args){
        Client._port = _port;
        Client._server = _server;

        int passed = 0;
        final int total = 18;
        
        System.out.println("Executing Test Plan");
        if(test_1() == 0) passed++;
        if(test_2() == 0) passed++;
        if(test_3() == 0) passed++;
        if(test_4() == 0) passed++;
        if(test_5() == 0) passed++;
        if(test_6() == 0) passed++;
        if(test_7() == 0) passed++;
        if(test_8() == 0) passed++;
        if(test_9() == 0) passed++;
        if(test_10() == 0) passed++;
        if(test_11() == 0) passed++;
        if(test_12() == 0) passed++;
        if(test_13() == 0) passed++;
        if(test_14() == 0) passed++;
        if(test_15() == 0) passed++;
        if(test_16() == 0) passed++;
        if(test_17() == 0) passed++;
        if(test_18() == 0) passed++;
        System.out.println(passed + "/" + total + " test passed");

    }

}