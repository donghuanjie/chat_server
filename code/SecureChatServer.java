import java.util.*;
import java.io.*;
import java.net.*;
import java.math.*;

public class SecureChatServer {

	public static final int PORT = 8888;

	private int MaxUsers;
	private Socket [] users;         // Need array of Sockets and Threads
	private UserThread [] threads;   
	private int numUsers;            
	private String StringE, StringD, StringN;
	private BigInteger E, D, N;
	Random R;

	public SecureChatServer(int MaxU) throws IOException
	{
		MaxUsers = MaxU;
		users = new Socket[MaxUsers];
		threads = new UserThread[MaxUsers];   // Set things up and start
		numUsers = 0;                         

		Scanner inScan = new Scanner(new File("keys.txt"));
		StringE = inScan.nextLine();
		StringD = inScan.nextLine();
		StringN = inScan.nextLine();
		E = new BigInteger(StringE);
		D = new BigInteger(StringD);
		N = new BigInteger(StringN);
		R = new Random();
		// System.out.println("My E: " + E);
		// System.out.println("My D: " + D);
		// System.out.println("My N: " + N);
		try
		{
			System.out.println("Server starting up...");
			runServer();
		}
		catch (Exception e)
		{
			System.out.println("Problem starting the server");
		}
	}


	public synchronized void SendMsg(String msg)
	{
		System.out.println("Message received by server.");
		for (int i = 0; i < numUsers; i++)
		{
			ObjectOutputStream currWriter = threads[i].getWriter();
			SymCipher currCipher = threads[i].getCipher();
			byte [] sendmsg = currCipher.encode(msg);
			try
			{
				currWriter.writeObject(sendmsg);
			}
			catch (IOException e)
			{
				System.out.println("Output error " + e);
			}
		}
	}

	public synchronized void removeClient(int id, String name)
	{
		try 
		{                            
			users[id].close();       
		}                            
		catch (IOException e)
		{
			System.out.println("Already closed");
		}
		users[id] = null;
		threads[id] = null;
		for (int i = id; i < numUsers-1; i++)   // Shift remaining clients to 1 place left
		{                                       
			users[i] = users[i+1];
			threads[i] = threads[i+1];
			threads[i].setId(i);	// id will change when a previous chatter quits, id is its index number
		}
		numUsers--;
		SendMsg(name + " has logged off");    
	}

	private void runServer() throws IOException
	{
		ServerSocket s = new ServerSocket(PORT);
		System.out.println("Started: " + s);
		Socket newSocket = null;
		try
		{
			while (true)
			{
				if (numUsers < MaxUsers)
				{
					try
					{
						newSocket = s.accept();    // get next client
						newSocket.setSoTimeout(20000);  // timeout if client does not respond
						// within 20 seconds.  This keeps
						// server from getting hung up by an unresponsive client
						// during the handshaking phase
						ObjectOutputStream tempWriter =
								new ObjectOutputStream(newSocket.getOutputStream());
						tempWriter.flush();
						ObjectInputStream tempReader =
								new ObjectInputStream(newSocket.getInputStream());

						System.out.println("Sending E");
						tempWriter.writeObject(E); tempWriter.flush();
						System.out.println("Sending N");
						tempWriter.writeObject(N); tempWriter.flush();
						double test = R.nextDouble();
						String encType = null;
						// Randomly determine which cipher will be used and send the
						// appropriate string to the client.
						if (test > 0.5)
							encType = new String("Sub");
						else
							encType = new String("Add");
						tempWriter.writeObject(encType);
						tempWriter.flush();
						//String addKey = tempReader.readLine();
						BigInteger bigKey = (BigInteger) tempReader.readObject();
						System.out.println("Encrypted key: " + bigKey);
						bigKey = bigKey.modPow(D, N);
						System.out.println("Decrypted key: " + bigKey);
						byte [] byteKey = bigKey.toByteArray();
						// System.out.println("Byte array length: " + byteKey.length);
						SymCipher cipher = null;

						// Since the key is sent from the client as a positive BigInteger,
						// when converted back the result could have an extra byte.  This
						// code tests for that byte and removes it if necessary.
						if (encType.equals("Add") && byteKey.length > 128)
						{
							byte [] temp = new byte[128];
							System.arraycopy(byteKey, 1, temp, 0, 128);
							byteKey = temp;
						}
						else if (encType.equals("Sub") && byteKey.length > 256)
						{
							byte [] temp = new byte[256];
							System.arraycopy(byteKey, 1, temp, 0, 256);
							byteKey = temp;
						}

						if (encType.equals("Add"))
						{
							System.out.println("Chosen Cipher is Add128");
							cipher = new Add128(byteKey);
						}
						else
						{
							System.out.println("Chosen Cipher is Substitute");
							cipher = new Substitute(byteKey);
						}

						// System.out.println("Key: ");
						// for (int i = 0; i < byteKey.length; i++)
						// 	System.out.print(byteKey[i] + " ");
						System.out.println();

						// Get the Client's name (note that this is encoded).  Then
						// pass it on the all of the current chatters before adding
						// the new chatter to the list.
						byte [] newBytes = (byte []) tempReader.readObject();
						String newName = cipher.decode(newBytes);

						newSocket.setSoTimeout(0);  

						synchronized (this)
						{
							users[numUsers] = newSocket;
							SendMsg(newName + " has just joined the chat group");
							// Above the server gets the new chatter's and announces
							// to the rest of the group

							threads[numUsers] = new UserThread(newSocket, numUsers,
									newName, tempReader, tempWriter, cipher);
							threads[numUsers].start();
							// Above a new thread is created and started for the
							// new user

							System.out.println("Connection " + numUsers + users[numUsers]);
							numUsers++;
						}
					}
					catch (java.net.SocketTimeoutException e1)
					{
						System.out.println("Client timed out during login " + e1);
						newSocket.close();
					}
					catch (Exception e2)
					{
						System.out.println("Problem with connection " + e2);
						e2.printStackTrace();
						newSocket.close();
					}
				}  // if
				else
				{
					Thread.sleep(1000);
				}

			}  // while
		}   // try
		catch (Exception e)
		{
			System.out.println("Something wrong starting server: " + e);
		}
		finally
		{
			System.out.println("Server shutting down!");

		}
	}  

	// UserThread class is used by the server to keep track of the clients.  
	// The cipher being used is encapsulated within the UserThread, each client can have a different encryption method.

	private class UserThread extends Thread
	{
		private Socket mySocket;
		private ObjectInputStream myReader;
		private ObjectOutputStream myWriter;
		private SymCipher myCipher;
		private int myId;
		private String myName;

		private UserThread(Socket newSocket, int id, String newName,
				ObjectInputStream newReader,
				ObjectOutputStream newWriter, SymCipher c) throws IOException
		{
			mySocket = newSocket;
			myId = id;
			myName = newName;
			myReader = newReader;
			myWriter = newWriter;
			myCipher = c;
		}

		public ObjectInputStream getReader()
		{
			return myReader;
		}

		public ObjectOutputStream getWriter()
		{
			return myWriter;
		}

		public SymCipher getCipher()
		{
			return myCipher;
		}

		public synchronized void setId(int newId)
		{
			myId = newId;   // id is the array index number, will change if somebody leaves the server
		}

		// While running, each UserThread will get the next message from its
		// corresponding client, and then send it to the other clients (through
		// the Server).  A departing client is detected by an IOException in
		// trying to read, which causes the removeClient method to be executed.

		public void run()
		{
			boolean ok = true;
			while (ok)
			{
				String newMsg = null;
				byte[] newBytes = null;
				try {
					newBytes = (byte []) myReader.readObject();
					newMsg = myCipher.decode(newBytes);
					if (newBytes == null || newMsg.equals("CLIENT CLOSING"))
						ok = false;
					else
						SecureChatServer.this.SendMsg(newMsg);
				}
				catch (Exception e)
				{
					System.out.println("Client closing!!" + e);
					ok = false;
				}
			}
			removeClient(myId, myName);
		}
	}

	public static void main(String [] args) throws IOException
	{
		SecureChatServer Secure = new SecureChatServer(30);
	}
}
