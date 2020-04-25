import java.io.*;
import gnu.getopt.Getopt;
import java.lang.*;
import java.net.*;
import java.util.*;


class client {
	
	
	/******************* ATTRIBUTES *******************/
	
	private static String _server   = null; //server IP
	private static int _port = -1;
	private static int _server_port = 3333;
	private static ServerSocket serverAddr = null;
	private static int _listening_port = 6660;
	private static String _upload_path = ".//files//uploaded_files";
	private static String _downloads_path = ".//files//downloaded_files";
	private static final int BUFFER_SIZE = 4096;
	private static final int NAME_SIZE = 256;
	//Thread in charge of sending requested files
	private static Thread listeningThread;

		
	
	/********************* METHODS ********************/
	
	/**
	 * @param user - User name to register in the system
	 * 
	 * @return ERROR CODE
	 */
	static int register(String user) 
	{
		byte ans = -1;
		try{
            Socket sc = new Socket(_server, _port);
            OutputStream ostream = sc.getOutputStream();
			ObjectOutput s = new ObjectOutputStream(ostream);

			InputStream istream = sc.getInputStream();
			ObjectInput in = new ObjectInputStream(istream);
			
			String op = "REGISTER"+'\0';
			user = user + '\0';

			//Send request
			s.writeChars(op);			
			s.writeChars(user);
			s.flush();

			//Read answer from server
            ans = in.readByte();
			ans = 0;
			sc.close();
        } catch (Exception e){
            System.err.println("Exeption"+e.toString());
            e.printStackTrace();
        }
		if (ans == 0){
			System.out.println("REGISTER OK");
		}else if(ans == 1){
			System.out.println("USERNAME IN USE");
		}else{
			System.out.println("REGISTER FAIL");
		}
		return 0;
	}
	
	/**
	 * @param user - User name to unregister from the system
	 * 
	 * @return ERROR CODE
	 */
	static int unregister(String user) 
	{
		byte ans = -1;
		try{
            Socket sc = new Socket(_server, _port);
            OutputStream ostream = sc.getOutputStream();
			ObjectOutput s = new ObjectOutputStream(ostream);
			
			DataInputStream iStream = new DataInputStream(sc.getInputStream());
			//create request
			String op = "UNREGISTER"+'\0';
			user = user + '\0';
			//send request			
			s.writeChars(op);			
			s.writeChars(user);
			s.flush();
			
			//Read answer
            ans = iStream.readByte();
			sc.close();
        } catch (Exception e){
            System.err.println("Exeption"+e.toString());
            e.printStackTrace();
        }
		if (ans == 0){
			System.out.println("UNREGISTER OK");
		}else if(ans == 1){
			System.out.println("USER DOES NOT EXIST");
		}else{
			System.out.println("UNREGISTER FAIL");
		}
		return 0;
	}
	
    	/**
	 * @param user - User name to connect to the system
	 * 
	 * @return ERROR CODE
	 */
	static int connect(String user) 
	{
				 		
		//1. obtain the IP and a free port
		//2. New ServerSocket()
		try {			
			serverAddr = new ServerSocket(_listening_port);
		} catch(Exception exception){
			System.err.println("Error creating socket");
		}	
		//Create thread to listen the request		
		listeningThread = new Thread(){
			public void run(){								
				while(true){ //while(connected)
					try {
						//Accept connection from other user
						Socket sc = serverAddr.accept();

						//Read request
						InputStream iStream = sc.getInputStream();
						ObjectInput in = new ObjectInputStream(iStream);						

						//Read the name of the file requested
						String req_file = in.readLine();
						
						//Full path of the requested file
						String _file_path = _upload_path + req_file;

						//Prepare message to write the file
						OutputStream oStream = sc.getOutputStream();
						ObjectOutput out = new ObjectOutputStream(oStream);

						//Reading the file to send
						InputStream fileInput = new FileInputStream(_file_path);

						byte [] buffer = new byte[BUFFER_SIZE];
						
						int b_read = 0;
						while((b_read = fileInput.read(buffer))!= -1){
							//send file
							oStream.write(buffer, 0, b_read);
						}						
						out.flush();
						fileInput.close();

						sc.close();
					} catch (Exception e) {
						System.err.println("Exception " + e.toString());
						e.printStackTrace();
					}
				}				
			}
		};
		//4. Send request to server
		
		String op = "CONNECT"+'\0';
		user = user + '\0';
		byte ans = -1;
		try {
			//Establish connection with the server
			Socket server_sc = new Socket(_server, _server_port);
			DataOutputStream server_oStream = new DataOutputStream(server_sc.getOutputStream());

			DataInputStream server_iStream = new DataInputStream(server_sc.getInputStream());
			//Send req
			server_oStream.writeChars(op);
			server_oStream.writeChars(user);
			server_oStream.flush();		

			//5. Read answer from server
			ans = server_iStream.readByte();

			server_sc.close();
			
			} catch (Exception e) {
			
		}
				
		switch(ans){
			case 0:
				System.out.println("CONNECT OK");
				break;
			case 1:
				System.out.println("CONNECT FAIL, USER DOES NOT EXISTS");
				break;
			case 2:
				System.out.println("USER ALREADY CONNECTED");				
				break;
			default:
				System.out.println("CONNECT FAIL");				
				break;
		}

		return 0;		
	}

	
	 /**
	 * @param user - User name to disconnect from the system
	 * 
	 * @return ERROR CODE
	 */
	static int disconnect(String user) 
	{
		byte  ans = -1;
		try {
			//1. close serverSocket
			serverAddr.close();
			//2. destroy thread
			listeningThread.join();

			//Connect to server
			Socket sc = new Socket(_server, _server_port);			
			//write request
			OutputStream oStream = sc.getOutputStream();
			ObjectOutput out = new ObjectOutputStream(oStream);
			String op = "DISCONNECT"+'\0';
			user = user + '\0'; //user to disconnect

			out.writeChars(op);
			out.writeChars(user);
			out.flush();

			//Read ans from server
			InputStream iStream = sc.getInputStream();
			ObjectInput in = new ObjectInputStream(iStream);
			ans = in.readByte();

			sc.close();

		} catch (Exception e) {
			System.err.println("Communication error with the server");
			e.printStackTrace();
		}
		switch(ans){
			case 0:
				System.out.println("DISCONNECT OK");
				break;
			case 1:
				System.out.println("DISCONNECT FAIL / USER DOES NOT EXISTS");
				break;
			case 2:
				System.out.println("DISCONNECT FAIL / USER NOT CONNECTED");				
				break;
			default:
				System.out.println("DISCONNECT FAIL");				
				break;
		}		
		return 0;
	}

	 /**
	 * @param file_name    - file name
	 * @param description - descrition
	 * 
	 * @return ERROR CODE
	 */
	static int publish(String file_name, String description) 
	{
		if (file_name.length() > 256){
			System.out.println("PUBLISH FAIL, FILE NAME TOO LONG");
			return 0;
		}
		if (description.length() > 256){
			System.out.println("PUBLISH FAIL, DESCRIPTION NAME TOO LONG");
			return 0;
		}

		byte  ans = -1;
		try {
			//1. close serverSocket
			serverAddr.close();
			//2. destroy thread
			listeningThread.join();

			//Connect to server
			Socket sc = new Socket(_server, _server_port);			
			//write request
			OutputStream oStream = sc.getOutputStream();
			ObjectOutput out = new ObjectOutputStream(oStream);
			String op = "PUBLISH"+'\0';
			file_name = file_name + '\0'; //user to disconnect

			out.writeChars(op);
			out.writeChars(file_name);
			out.flush();

			//Read ans from server
			InputStream iStream = sc.getInputStream();
			ObjectInput in = new ObjectInputStream(iStream);
			ans = in.readByte();

			sc.close();

		} catch (Exception e) {
			System.err.println("Communication error with the server");
			e.printStackTrace();
		}
		switch(ans){
			case 0:
				System.out.println("PUBLISH OK");
				break;
			case 1:
				System.out.println("PUBLISH FAIL, USER DOES NOT EXISTS");
				break;
			case 2:
				System.out.println("PUBLISH FAIL, USER NOT CONNECTED");				
				break;
			case 3:
				System.out.println("PUBLISH FAIL, CONTENT ALREDAY PUBLISHED");				
				break;	
			default:
				System.out.println("PUBLISH FAIL");				
				break;
		}		
		return 0;
	}

	 /**
	 * @param file_name    - file name
	 * 
	 * @return ERROR CODE
	 */
	static int delete(String file_name)
	{
		byte  ans = -1;
		try {
			//1. close serverSocket
			serverAddr.close();
			//2. destroy thread
			listeningThread.join();

			//Connect to server
			Socket sc = new Socket(_server, _server_port);			
			//write request
			OutputStream oStream = sc.getOutputStream();
			ObjectOutput out = new ObjectOutputStream(oStream);
			String op = "DELETE"+'\0';
			file_name = file_name + '\0'; //user to disconnect

			out.writeChars(op);
			out.writeChars(file_name);
			out.flush();

			//Read ans from server
			InputStream iStream = sc.getInputStream();
			ObjectInput in = new ObjectInputStream(iStream);
			ans = in.readByte();

			sc.close();

		} catch (Exception e) {
			System.err.println("Communication error with the server");
			e.printStackTrace();
		}
		switch(ans){
			case 0:
				System.out.println("DELETE OK");
				break;
			case 1:
				System.out.println("DELETE FAIL, USER DOES NOT EXIST");
				break;
			case 2:
				System.out.println("DELETE FAIL, USER NOT CONNECTED");				
				break;
			case 3:
				System.out.println("DELETE FAIL, CONTENT NOT PUBLISHED");				
				break;
			case 4:
				System.out.println("DELETE FAIL");				
				break;
		}		
		return 0;
	}

	 /**
	 * @return ERROR CODE
	 */
	static int list_users()
	{
		byte ans = -1;
		try{			 
			//Connecto to the server
			Socket sc = new Socket(_server, _server_port);

			//Create request message
			String op = "LIST_USERS" + '\0';

			OutputStream oStream = sc.getOutputStream();
			DataOutputStream out = new DataOutputStream(oStream);

			InputStream iStream = sc.getInputStream();
			//DataInputStream in = new DataInputStream(iStream);
			ObjectInputStream in = new ObjectInputStream(iStream);

			//Send request
			out.writeChars(op);
			
			//Read answer
			ans = in.readByte();

			switch (ans) {
				case 0: //Everything ok
					System.out.println("LIST_USERS OK\n");
					int numUsers = Integer.parseInt((String)in.readObject());
					//Print the connected users
					for(int i = 0; i < numUsers; i++){
						//For each connected user 3 string are sent indicatng the name, IP and port
						for (int j = 0; j < 3; j++){
							String s = (String) in.readObject();
						System.out.println(s + '\t');
						}						
					}						
					break;
				case 1:
					System.out.println("LIST_USERS FAIL, USER DOES NOT EXIST");
				case 2:
					System.out.println("LIST_USERS FAIL, USER NOT CONNECTED");
					break;
				default:
					System.out.println("LIST_USERS FAIL");
					break;
			}
			//Close the connection with the server
			sc.close();
		} catch(Exception e){
			System.err.println("Communication error with the server");
			e.printStackTrace();
		}
		return 0;
	}


	 /**
	 * @param user_name    - user name
	 * 
	 * @return ERROR CODE
	 */
	static int list_content(String user_name)
	{
		byte ans = -1;
		try{			 
			//Connecto to the server
			Socket sc = new Socket(_server, _server_port);

			//Create request message			
			 String op = "LIST_CONTENT" + '\0';
			 user_name = user_name + '\0';

			OutputStream oStream = sc.getOutputStream();
			DataOutputStream out = new DataOutputStream(oStream);

			InputStream iStream = sc.getInputStream();
			//DataInputStream in = new DataInputStream(iStream);
			ObjectInputStream in = new ObjectInputStream(iStream);

			//Send request
			out.writeChars(op); //operation code
			out.writeChars(user_name); //User name
			out.flush();
			
			//Read answer
			ans = in.readByte();

			switch (ans) {
				case 0: //Everything ok
					System.out.println("LIST_CONTENT OK\n");
					int numFiles = Integer.parseInt((String)in.readObject());
					//Print the connected users
					for(int i = 0; i < numFiles; i++){
						//For each file its name is sended
						for (int j = 0; j < 3; j++){
							byte fileName[] = new byte[NAME_SIZE];
							in.read(fileName, 0, NAME_SIZE);
							String s = String.valueOf(fileName);
						System.out.println(s + '\n');
						}						
					}						
					break;
				case 1:
					System.out.println("LIST_CONTENT FAIL, USER DOES NOT EXIST");
				case 2:
					System.out.println("LIST_CONTENT FAIL, USER NOT CONNECTED");
					break;
				case 3:
					System.out.println("LIST_CONTENT FAIL, REMOTE USER DOES NOT EXIST");
					break;					
				default:
					System.out.println("LIST_CONTENT FAIL");
					break;
			}
			//Close the connection with the server
			sc.close();
		} catch(Exception e){
			System.err.println("Communication error with the server");
			e.printStackTrace();
		}
		return 0;
	}

	 /**
	 * @param user_name    - user name
	 * @param remote_file_name    - remote file name
	 * @param local_file_name  - local file name
	 * 
	 * @return ERROR CODE
	 */
	static int get_file(String user_name, String remote_file_name, String local_file_name)
	{
		
		byte ans = -1;
		try{
				
			Socket sc_server = new Socket(_server, _server_port);
			
			String op = "GET_FILE" + '\0';
			user_name = user_name + '\0';

			DataOutputStream out = new DataOutputStream(sc_server.getOutputStream());
			out.writeChars(op);
			out.writeChars(user_name);
			out.flush();

			ObjectInputStream in = new ObjectInputStream(sc_server.getInputStream());
			String clientAddr = (String) in.readObject();
			int clientPort = in.readInt();

			sc_server.close();

			Socket sc_P2P = new Socket(clientAddr, clientPort);
			String file_name = remote_file_name + '\0';
			out = new DataOutputStream(sc_P2P.getOutputStream());
			out.writeChars(file_name);
			out.flush();

			//Read answer from the other client
			ans = in.readByte();

			switch (ans) {
				case 0:
					OutputStream fStream = new FileOutputStream(local_file_name);
					ObjectOutputStream objOut = new ObjectOutputStream(fStream);
					

					System.out.println("GET_FILE OK");
					//??How to send a whole file
					File f = new File(_downloads_path + local_file_name);					
					f = (File) in.readObject();

					objOut.writeObject(f);
					objOut.flush();
					break;
				case 1:
					System.out.println("GET_FILE FAIL / FILE NOT EXIST");
					break;				
				default:
					System.out.println("GET_FILE FAIL");
					break;
			}
			sc_P2P.close();
			
		}catch(Exception e){
			System.err.println(e);
			e.printStackTrace();
		}

		//Stablish connection with server

		//Create req
		//Open socket(user, _listening_port)
		//Write req = file name
		//Read the contents of the file
		//Create a new file with the same name in downloaded_files folder
		//Copy the contents

		//Read answer from server
		

		switch(ans){
			case 0:
				System.out.println("GET_FILE OK\n");
				break;
			case 1:
				System.out.println("GET_FILE FAIL / FILE NOT EXISTS");
				break;
			default:
				System.out.println("GET_FILE FAIL");
				break;		
		}
		//System.out.println("GET_FILE " + user_name + " "  + remote_file_name + " " + local_file_name);
		return 0;
	}

	
	/**
	 * @brief Command interpreter for the client. It calls the protocol functions.
	 */
	static void shell() 
	{
		boolean exit = false;
		String input;
		String [] line;
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

		while (!exit) {
			try {
				System.out.print("c> ");
				input = in.readLine();
				line = input.split("\\s");

				if (line.length > 0) {
					/*********** REGISTER *************/
					if (line[0].equals("REGISTER")) {
						if  (line.length == 2) {
							register(line[1]); // userName = line[1]
						} else {
							System.out.println("Syntax error. Usage: REGISTER <userName>");
						}
					} 
					
					/********** UNREGISTER ************/
					else if (line[0].equals("UNREGISTER")) {
						if  (line.length == 2) {
							unregister(line[1]); // userName = line[1]
						} else {
							System.out.println("Syntax error. Usage: UNREGISTER <userName>");
						}
                    			} 
                    			
                    			/************ CONNECT *************/
                    			else if (line[0].equals("CONNECT")) {
						if  (line.length == 2) {
							connect(line[1]); // userName = line[1]
						} else {
							System.out.println("Syntax error. Usage: CONNECT <userName>");
                    				}
                    			} 
                    
                    			/********** DISCONNECT ************/
                    			else if (line[0].equals("DISCONNECT")) {
						if  (line.length == 2) {
							disconnect(line[1]); // userName = line[1]
						} else {
							System.out.println("Syntax error. Usage: DISCONNECT <userName>");
                    				}
                    			} 
                    
                    			/************** PUBLISH **************/
                    			else if (line[0].equals("PUBLISH")) {
						if  (line.length >= 3) {
							// Remove first two words
							//String description = input.substring(input.indexOf(' ')+1).substring(input.indexOf(' ')+1);
							String description = input.substring(input.indexOf(' ')+1);
							description = description.substring(description.indexOf(' ')+1);
							publish(line[1], description); // file_name = line[1]
						} else {
							System.out.println("Syntax error. Usage: PUBLISH <file_name> <description>");
                    				}
                    			} 

                    			/************ DELETE *************/
                    			else if (line[0].equals("DELETE")) {
						if  (line.length == 2) {
							delete(line[1]); // userName = line[1]
						} else {
							System.out.println("Syntax error. Usage: DELETE <file name>");
                    				}
                    			} 
                    
                    			/************** LIST_USERS **************/
                    			else if (line[0].equals("LIST_USERS")) {
						if  (line.length == 1) {
							// Remove first two words
							list_users(); 
						} else {
							System.out.println("Syntax error. Usage: LIST_USERS ");
                    				}
                    			} 
                    
                    			/************ LIST_CONTENT *************/
                    			else if (line[0].equals("LIST_CONTENT")) {
						if  (line.length == 2) {
							list_content(line[1]); // userName = line[1]
						} else {
							System.out.println("Syntax error. Usage: LIST_CONTENT <user name>");
                    				}
                    			} 
                    
                    			/************** GET_FILE **************/
                    			else if (line[0].equals("GET_FILE")) {
						if  (line.length == 4) {
							get_file(line[1], line[2], line[3]); 
						} else {
							System.out.println("Syntax error. Usage: GET_FILE <user> <remote_file_name> <local_file_name>");
                    				}
                    			} 

                    
                    			/************** QUIT **************/
                    			else if (line[0].equals("QUIT")){
						if (line.length == 1) {
							exit = true;
						} else {
							System.out.println("Syntax error. Use: QUIT");
						}
					} 
					
					/************* UNKNOWN ************/
					else {						
						System.out.println("Error: command '" + line[0] + "' not valid.");
					}
				}				
			} catch (java.io.IOException e) {
				System.out.println("Exception: " + e);
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * @brief Prints program usage
	 */
	static void usage() 
	{
		System.out.println("Usage: java -cp . client -s <server> -p <port>");
	}
	
	/**
	 * @brief Parses program execution arguments 
	 */ 
	static boolean parseArguments(String [] argv) 
	{
		Getopt g = new Getopt("client", argv, "ds:p:");

		int c;
		String arg;

		while ((c = g.getopt()) != -1) {
			switch(c) {
				//case 'd':
				//	_debug = true;
				//	break;
				case 's':
					_server = g.getOptarg();
					break;
				case 'p':
					arg = g.getOptarg();
					_port = Integer.parseInt(arg);
					break;
				case '?':
					System.out.print("getopt() returned " + c + "\n");
					break; // getopt() already printed an error
				default:
					System.out.print("getopt() returned " + c + "\n");
			}
		}
		
		if (_server == null)
			return false;
		
		if ((_port < 1024) || (_port > 65535)) {
			System.out.println("Error: Port must be in the range 1024 <= port <= 65535");
			return false;
		}

		return true;
	}
	
	
	
	/********************* MAIN **********************/
	
	public static void main(String[] argv) 
	{
		if(!parseArguments(argv)) {
			usage();
			return;
		}

		// Write code here
		
		shell();
		System.out.println("+++ FINISHED +++");
	}
}
