
import java.io.*;
import gnu.getopt.Getopt;
import java.net.*;
import java.util.*;

import client.UpperServiceService;
import client.UpperService;

class Client implements Runnable {

	/******************* ATTRIBUTES *******************/
	protected static String _user = null;
	protected static String _server = null; // server IP
	protected static int _port = -1;
	protected static ServerSocket clientP2P = null;
	//Port where the client will listen for files requests
	protected static int _listening_port = 0;
	protected static String _upload_path = ".//files/Shared//";
	protected static String _downloads_path = ".//files/Downloads//";
	protected static final int MAX_FILE_SIZE = 1048576; //1MB
	protected static final int NAME_SIZE = 256;
	// Thread in charge of sending requested files
	protected static Thread p2p_client_thread;
	//Local storage of connected users. Required for GET_FILE
	protected static User[] userList;

	/********************* METHODS ********************/

	/**
	 * @param user - User name to register in the system
	 * 
	 * @return ERROR CODE
	 */

	/* For opening connection
	*
	Socket sc = new Socket(_server, _port);
	DataOutputStream dataOutputStream = new DataOutputStream(sc.getOutputStream());
	DataInputStream dataInputStream = new DataInputStream(sc.getInputStream());
	*
	*/

	/* For closing connection
	*
	dataOutputStream.close();
	dataInputStream.close();
	sc.close();
	*
	*/

	static void write(DataOutputStream dataOutputStream, String message) {
		try {
			dataOutputStream.write(message.getBytes("ASCII"));
			dataOutputStream.writeByte('\0');
			dataOutputStream.flush(); // send null character
		} catch (Exception e) {
			System.err.println("Exeption" + e.toString());
			e.printStackTrace();
		}
	}

	static char read(DataInputStream dataInputStream){
		char c = '5';
		try {
			while (0 == dataInputStream.available()){
				
			}
			c = (char)dataInputStream.readByte();
		} catch (Exception e) {
			System.err.println("Exeption" + e.toString());
			e.printStackTrace();
		}
		return c;
	}

	static String readString(DataInputStream dataInputStream){
		String str = "";
		char ret;
		try {
			//Keeps reading until null character is read
			while (true){
				ret = read(dataInputStream);
				if(ret == '\0'){
					break;
				}else{
					str += ret;
				}	
			}

		} catch (Exception e) {
			System.err.println("Exeption" + e.toString());
			e.printStackTrace();
		}
		return str;
	}

	/*Function run by the p2p client side. Is in charge of accpting connections with other clients and sending files*/
	public void run(){
									
		while(true){ //while(connected)
			try {
				//P2P listening
				//Accept connection from other user
				Socket sc = clientP2P.accept();
	
				DataInputStream dataInputStream = new DataInputStream(sc.getInputStream());
				DataOutputStream dataOutputStream = new DataOutputStream(sc.getOutputStream());
				//Read request
				String opRequest = readString(dataInputStream);
				
				if(opRequest.equals("DISCONNECT")){
					clientP2P.close();
					//Ending thread
					return;
				}
				/*If the operation is not GET_FILE errno 2 is returned to the 
					other client adn the connection is closed
				*/
				if(!opRequest.equals("GET_FILE")){
					write(dataOutputStream, "2");
					dataInputStream.close();
					dataOutputStream.close();
					sc.close();
				
				}else {
					//Read the name of the file requested
					String req_file = readString(dataInputStream);
					
					//Full path of the requested file
					String _file_path = _upload_path + req_file;

					//Prepare message to write the file
					
					//Reading the file to send
					FileInputStream fileInput =null;
					try {
						fileInput = new FileInputStream(_file_path);
					} catch (FileNotFoundException e) {
						write(dataOutputStream, "1");
						dataInputStream.close();
						dataOutputStream.close();
						sc.close();
					}
						write(dataOutputStream, "0");

						byte [] buffer = new byte[MAX_FILE_SIZE];
					
					int b_read = 0;
					int total = 0;
					while((b_read = fileInput.read(buffer))!= -1){
						//send file
						total += b_read;
						if(total > MAX_FILE_SIZE){
							System.out.println("ERROR: the file is too big");
							fileInput.close();
							dataInputStream.close();
							dataOutputStream.close();
							sc.close();
							throw new Exception("ExcedeedMaxFileSize");
						}
					}	
					//Send the file size so that the other client can allocate space
					dataOutputStream.writeInt(total);

					//Send the bytes to the requesting client
					dataOutputStream.write(buffer, 0, total);
					dataOutputStream.flush();

					//Close file and end connection
					fileInput.close();
					dataInputStream.close();
					dataOutputStream.close();
					sc.close();
					
					
				}
				
			} catch (Exception e) {
				System.err.println("Exception " + e.toString());
				e.printStackTrace();
			}
		}				
	}//End of run() method



	static int register(String user) {
		char ans = '2';
		String op = "REGISTER";
		try{
			Socket sc = new Socket(_server, _port);
			DataOutputStream dataOutputStream = new DataOutputStream(sc.getOutputStream());
			DataInputStream dataInputStream = new DataInputStream(sc.getInputStream());
			
			write(dataOutputStream, op);
			write(dataOutputStream, user);
			ans = read(dataInputStream);

			dataOutputStream.close();
			dataInputStream.close();
			sc.close();
		} catch (Exception e){
            System.err.println("Exeption"+e.toString());
            e.printStackTrace();
        }
        switch(ans){
            case '0':
				System.out.println("REGISTER OK");
				return 0;
            case '1':
				System.out.println("USERNAME IN USE");
				return 1;
            default:
				System.out.println("REGISTER FAIL");
				return 2;
        }
	}
	
	/**
	 * @param user - User name to unregister from the system
	 * 
	 * @return ERROR CODE
	 */
	static int unregister(String user) 
	{
		char ans = '2';
		String op = "UNREGISTER";
		try{
			Socket sc = new Socket(_server, _port);
			DataOutputStream dataOutputStream = new DataOutputStream(sc.getOutputStream());
			DataInputStream dataInputStream = new DataInputStream(sc.getInputStream());
			
			write(dataOutputStream, op);
			write(dataOutputStream, user);
			ans = read(dataInputStream);

			dataOutputStream.close();
			dataInputStream.close();
			sc.close();
		} catch (Exception e){
            System.err.println("Exeption"+e.toString());
            e.printStackTrace();
        }
        switch(ans){
            case '0':
				System.out.println("UNREGISTER OK");
				return 0;
           case '1':
				System.out.println("USER DOES NOT EXIST");
				return 1;
            default:
				System.out.println("UNREGISTER FAIL");
				return 2;
        }
	}
	
    	/**
	 * @param user - User name to connect to the system
	 * 
	 * @return ERROR CODE
	 */
	static int connect(String user) 
	{
				 		
		//1. Send request to server
		String op = "CONNECT";
		char ans = '3';
		try{
			Socket sc = new Socket(_server, _port);
			DataOutputStream dataOutputStream = new DataOutputStream(sc.getOutputStream());
			DataInputStream dataInputStream = new DataInputStream(sc.getInputStream());
			
			//Automatic asignation of a free port number
			clientP2P = new ServerSocket(0);
			_listening_port = clientP2P.getLocalPort();


			//Send the request
			write(dataOutputStream, op);
			write(dataOutputStream, user);
			write(dataOutputStream, ""+_listening_port);

			//Read answer from server
			ans = read(dataInputStream);

			//Close connection with the server
			dataOutputStream.close();
			dataInputStream.close();
			sc.close();

			//Set the local variable _user to the value passed to register()
			_user = user;
		} catch (Exception e){
            System.err.println("Exeption"+e.toString());
            e.printStackTrace();
        }		
		switch(ans){
			case '0':
				System.out.println("CONNECT OK");
				//If the connection was successful, the listening thread is created
				//p2p_client_thread = new Client();
				Client p2p_client = new Client();
				p2p_client_thread = new Thread(p2p_client);
				p2p_client_thread.start();
				return 0;
			case '1':
				System.out.println("CONNECT FAIL, USER DOES NOT EXISTS");
				return 1;
			case '2':
				System.out.println("USER ALREADY CONNECTED");				
				return 2;
			default:
				System.out.println("CONNECT FAIL");				
				return 3;
		}
	}

	
	 /**
	 * @param user - User name to disconnect from the system
	 * 
	 * @return ERROR CODE
	 */
	static int disconnect(String user) 
	{
		char ans = '3';
		String op = "DISCONNECT";
		try {

			//1. Send the DISCONNECT op to the serverSocket
			Socket sc = new Socket(_server, _listening_port);
			DataOutputStream dataOutputStream = new DataOutputStream(sc.getOutputStream());
			write(dataOutputStream, op);
			sc.close();
			
			//2. destroy thread
			p2p_client_thread.join();
			
			//Connect to the server
			sc = new Socket(_server, _port);
			dataOutputStream = new DataOutputStream(sc.getOutputStream());
			DataInputStream dataInputStream = new DataInputStream(sc.getInputStream());
			
			write(dataOutputStream, op);
			write(dataOutputStream, user);
			ans = read(dataInputStream);

			dataOutputStream.close();
			dataInputStream.close();
			sc.close();
			_user = null;
			

		} catch (Exception e) {
			System.err.println("Communication error with the server");
			e.printStackTrace();
		}
		switch(ans){
			case '0':
				System.out.println("DISCONNECT OK");
				return 0;
			case '1':
				System.out.println("DISCONNECT FAIL / USER DOES NOT EXISTS");
				return 1;
			case '2':
				System.out.println("DISCONNECT FAIL / USER NOT CONNECTED");				
				return 2;
			default:
				System.out.println("DISCONNECT FAIL");				
				return 3;
		}		
	}

	 /**
	 * @param file_name    - file name
	 * @param description - descrition
	 * 
	 * @return ERROR CODE
	 */
	static int publish(String file_name, String description) 
	{
		if ((file_name.length() > NAME_SIZE) || (description.length() > NAME_SIZE)){
			System.out.println("PUBLISH FAIL");
			return 4;
		}

		if (_user == null){
			System.out.println("PUBLISH FAIL, USER NOT CONNECTED");
			return 2;
		}
		String op = "PUBLISH";
		char ans = '4';
		try{
			Socket sc = new Socket(_server, _port);
			DataOutputStream dataOutputStream = new DataOutputStream(sc.getOutputStream());
			DataInputStream dataInputStream = new DataInputStream(sc.getInputStream());
			
			//Send the request to the server
			write(dataOutputStream, op);
			write(dataOutputStream, _user);
			write(dataOutputStream, file_name);

			UpperServiceService service = new UpperServiceService();
			UpperService port = service.getUpperServicePort();
			description = port.toUpperCase(description);
			write(dataOutputStream, description);

			//Read answer from server
			ans = read(dataInputStream);

			//Close the connection
			dataOutputStream.close();
			dataInputStream.close();
			sc.close();
		} catch (Exception e){
            System.err.println("Exeption"+e.toString());
			e.printStackTrace();
        }
		switch(ans){
			case '0':
				System.out.println("PUBLISH OK");
				return 0;
			case '1':
				System.out.println("PUBLISH FAIL, USER DOES NOT EXISTS");
				return 1;
			case '2':
				System.out.println("PUBLISH FAIL, USER NOT CONNECTED");				
				return 2;
			case '3':
				System.out.println("PUBLISH FAIL, CONTENT ALREDAY PUBLISHED");				
				return 3;	
			default:
				System.out.println("PUBLISH FAIL");				
				return 4;
		}		
	}

	 /**
	 * @param file_name    - file name
	 * 
	 * @return ERROR CODE
	 */
	static int delete(String file_name)
	{
		char ans = '4';
		String op = "DELETE";
		if (_user == null){
			System.out.println("DELETE FAIL, USER NOT CONNECTED");
			return 2;
		}
		try{
			Socket sc = new Socket(_server, _port);
			DataOutputStream dataOutputStream = new DataOutputStream(sc.getOutputStream());
			DataInputStream dataInputStream = new DataInputStream(sc.getInputStream());
			
			//Send the request
			write(dataOutputStream, op);
			write(dataOutputStream, _user);
			write(dataOutputStream, file_name);

			//Read the answer from server
			ans = read(dataInputStream);

			//Close connection
			dataOutputStream.close();
			dataInputStream.close();
			sc.close();
		} catch (Exception e){
            System.err.println("Exeption"+e.toString());
			e.printStackTrace();
        }
		switch(ans){
			case '0':
				System.out.println("DELETE OK");
				return 0;
			case '1':
				System.out.println("DELETE FAIL, USER DOES NOT EXIST");
				return 1;
			case '2':
				System.out.println("DELETE FAIL, USER NOT CONNECTED");				
				return 2;
			case '3':
				System.out.println("DELETE FAIL, CONTENT NOT PUBLISHED");				
				return 3;
			default:
				System.out.println("DELETE FAIL");				
				return 4;
		}		
	}

	 /**
	 * @return ERROR CODE
	 */
	static int list_users()
	{
		char ans = '3';
		if (_user == null){
			System.out.println("LIST_USER FAIL, USER NOT CONNECTED");
			return 2;
		}
		//Create request message
		String op = "LIST_USERS";
		try{			 
			//Connecto to the server
			Socket sc = new Socket(_server, _port);
			DataOutputStream dataOutputStream = new DataOutputStream(sc.getOutputStream());
			DataInputStream dataInputStream = new DataInputStream(sc.getInputStream());

			write(dataOutputStream, op);
			write(dataOutputStream, _user);
			
			ans = read(dataInputStream);

			if (ans == '0'){//Everything ok	
				System.out.println("LIST_USERS OK");
				//read the number of users whose information will be sent
				int numUsers = Integer.parseInt(readString(dataInputStream));
				//Print the connected users
				System.out.println("USER NAME\tIP\t\tPORT\t\n");
				
				//Local storage of connected users. Required for GET_FILE
				userList = new User[numUsers];
				Arrays.fill(userList, new User());

				for(int i = 0; i < numUsers; i++){
					int words = 3;
					String ln = "";
					userList[i] = new User();
					//For each connected user 3 string are sent indicatng the name, IP and port
					while(words > 0){ 
						ln = readString(dataInputStream);
						switch(words){
							case 3:
								userList[i].set_userName(ln);
								break;
							case 2: 
								userList[i].set_ip(ln);
								break;
							case 1:
								userList[i].set_port(Integer.parseInt(ln));
								break;
						}
						words--;							
					}
					//print the information of the i-th user 
					System.out.println(i + " " + userList[i]+"\n");															
				}
			}
				
			//Close the connection with the server
			dataOutputStream.close();
			dataInputStream.close();
			sc.close();

			//If something went wrong select the message displayed according to the error number
			switch (ans) {
				case '0':
					//System.out.println("LIST_USERS OK");
					return 0;
				case '1':
					System.out.println("LIST_USERS FAIL, USER DOES NOT EXIST");
					return 1;
				case '2':
					System.out.println("LIST_USERS FAIL, USER NOT CONNECTED");
					return 2;
				default:
					System.out.println("LIST_USERS FAIL");
					return 3;
			}
		} catch(Exception e){
			System.err.println("Communication error with the server");
			e.printStackTrace();
			return 3;
		}
	}


	 /**
	 * @param user_name    - user name
	 * 
	 * @return ERROR CODE
	 */
	static int list_content(String user_name)
	{
		char ans = '4';

		if (_user == null){
			System.out.println("LIST_CONTENT FAIL, USER NOT CONNECTED");
			return 0;
		}

		try{			 
			//Connecto to the server
			Socket sc = new Socket(_server, _port);
			DataOutputStream dataOutputStream = new DataOutputStream(sc.getOutputStream());
			DataInputStream dataInputStream = new DataInputStream(sc.getInputStream());

			//Create request message			
			 String op = "LIST_CONTENT";

			//Send request
			write(dataOutputStream, op); //operation code
			write(dataOutputStream, _user); //Name of the user making the request
			write(dataOutputStream, user_name); //Name of the user whose files are requested
			
			//Read answer
			ans = read(dataInputStream);

			if(ans == '0'){
				//Everything ok
				System.out.println("LIST_CONTENT OK\n");
				int numFiles = Integer.parseInt(readString(dataInputStream));
				//Print the connected users
				System.out.println("User: " + user_name);
				System.out.println("Shared files (" + numFiles + ") :");
				//Read the null char passed after numFiles. Other wise
				//read(dataInputStream);
				for(int i = 0; i < numFiles; i++){
					//For each file its name is sended
					//String ln = "";
					String ln = readString(dataInputStream);
					//Print the i-th file name
					System.out.println(ln);						
				}
			}

			//Close the connection with the server
			dataInputStream.close();
			dataOutputStream.close();
			sc.close();

			switch (ans) {
				case '0': 						
					return 0;
				case '1':
					System.out.println("LIST_CONTENT FAIL, USER DOES NOT EXIST");
					return 1;
				case '2':
					System.out.println("LIST_CONTENT FAIL, USER NOT CONNECTED");
					return 2;
				case '3':
					System.out.println("LIST_CONTENT FAIL, REMOTE USER DOES NOT EXIST");
					return 3;					
				default:
					System.out.println("LIST_CONTENT FAIL");
					return 4;
			}

		} catch(Exception e){
			System.err.println("Communication error with the server");
			e.printStackTrace();
			return 4;
		}
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
		
		char ans = '2';

		if (_user == null){
			System.out.println("GET_FILE FAIL");
			return 2;
		}
		/*
		if (user_name.equals(_user)){
			System.out.println("GET_FILE FAIL");
			return 0;
		}
		*/

		try{
		
			User remoteUser = User.findUserByName(userList, user_name);
			//If the user could not be found means that the user is not connected, thus it cannot send the file
			if(remoteUser == null){
				System.out.println("GET_FILE FAIL: USER DOES NOT EXIST OR NOT CONNECTED. UPDATE STATUS CALLING LIST_USERS");
				return 0;
			}

			String op = "GET_FILE";
			Socket sc_p2p = null;
			try {
				sc_p2p = new Socket(remoteUser.get_ip(), remoteUser.get_port());
			} catch (Exception e) {
				System.out.println("GET_FILE FAIL");
				return 2;
			}
			DataOutputStream dataOutputStream = new DataOutputStream(sc_p2p.getOutputStream());
			DataInputStream dataInputStream = new DataInputStream(sc_p2p.getInputStream());
			
			write(dataOutputStream, op);
			write(dataOutputStream, remote_file_name);

			//Read answer from the other client
			ans = read(dataInputStream);

			if(ans == '0'){ //The file is going to be transmited
				//System.out.println("The file is being downloaded");
				File download = new File(_downloads_path + local_file_name);

				//CreateNewFile returns false if the file already exists, true if it has been created
				if (!download.createNewFile()){
					//Error file already exists
					ans = 2;
				} else {
					//Locate the local file to output the contents
					FileOutputStream fileOut = new FileOutputStream(_downloads_path + local_file_name);
					int fileSize = dataInputStream.readInt();
					byte[] fileBytes = new byte[fileSize];
					int readed = dataInputStream.read(fileBytes, 0, fileSize);
                    while (readed != 0 && readed != -1){
                        fileOut.write(fileBytes, 0, readed);
                        readed = dataInputStream.read(fileBytes, 0, fileSize);

                    }
					fileOut.close();
				}
			}
			//Close the connection with the other client
			dataInputStream.close();
			dataOutputStream.close();
			sc_p2p.close();

			switch (ans) {
				case '0':
					System.out.println("GET_FILE OK");
					return 0;
				case '1':
					System.out.println("GET_FILE FAIL / FILE NOT EXIST");
					return 1;				
				default:
					System.out.println("GET_FILE FAIL");
					return 2;
			}
		}catch(Exception e){
			/* Possible exceptions:
				IOException: I/O errors witht the input or output streams
				SecurityException: access to the file denied
			*/
			System.err.println(e);
			e.printStackTrace();
			return 2;
		}
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
