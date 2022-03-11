import java.util.*;
import java.io.*;
import java.math.BigInteger;
import java.net.*;
import javax.swing.*;
import java.awt.event.*;
import java.awt.*;
import java.nio.charset.StandardCharsets;


public class SecureChatClient extends JFrame implements Runnable, ActionListener {

    public static final int PORT = 8888;

    ObjectInputStream myReader;
    ObjectOutputStream myWriter;
    JTextArea outputArea;
    JLabel prompt;
    JTextField inputField;
    String myName, serverName;
    Socket connection;
    
    BigInteger E, N;
    String encType;
    SymCipher cipher;

    public SecureChatClient()
    {
        try 
        {
            myName = JOptionPane.showInputDialog(this, "Enter your user name: ");
            serverName = JOptionPane.showInputDialog(this, "Enter the server name: ");
            InetAddress addr = InetAddress.getByName(serverName);
            connection = new Socket(addr, PORT);   // Connect to server with new Socket
            
            //! why writer prior to reader? no difference in rec
            myWriter = new ObjectOutputStream(connection.getOutputStream());
            myWriter.flush();
            myReader = new ObjectInputStream(connection.getInputStream());

            E = (BigInteger) myReader.readObject();
            // System.out.println("E is: " + E); 
            N = (BigInteger) myReader.readObject();
            // System.out.println("N is: " + N);
            encType = (String) myReader.readObject();
            System.out.println();
            
            //! create Cipher object
            if (encType.equals("Add"))
            {
                cipher = new Add128();  
                System.out.println("Add128 cipher type has been used.");
                System.out.println();
            }
            else if (encType.equals("Sub"))
            {
                cipher = new Substitute();
                System.out.println("Substitute cipher type has been used.");
                System.out.println();
            }
            else
            {
                System.out.println("Invalid cipher type!");
                System.out.println();
                System.exit(1);
            }

            byte [] cipherKey = cipher.getKey();
            // System.out.print("Cipher Key generated: ");
            // for (int i = 0; i < cipherKey.length; i++)
            //     System.out.print(cipherKey[i] + " ");
            
            System.out.println();
            BigInteger bigKey = new BigInteger(1, cipherKey);
            bigKey = bigKey.modPow(E, N);
            myWriter.writeObject(bigKey);
            myWriter.flush();


            //------------------------------------------- Window ----------------------------------------------------------
            this.setTitle(myName + "'s chatbox");      // Set title to identify chatter
            myWriter.writeObject(cipher.encode(myName));   
            myWriter.flush();

            Box b = Box.createHorizontalBox();  // Set up graphical environment for
            outputArea = new JTextArea(14, 30);  // user
            outputArea.setEditable(false);
            b.add(new JScrollPane(outputArea));

            outputArea.append("Welcome to ECS 251 Chat Group, " + myName + "\n");

            inputField = new JTextField("");  // user will type input
            inputField.addActionListener(this);

            prompt = new JLabel("Type your messages below:");
            Container c = getContentPane();

            c.add(b, BorderLayout.NORTH);
            c.add(prompt, BorderLayout.CENTER);
            c.add(inputField, BorderLayout.SOUTH);

            Thread outputThread = new Thread(this);  // Thread is to receive strings from Server
            outputThread.start();                    

            addWindowListener(
                new WindowAdapter()
                {
                    public void windowClosing(WindowEvent e)
                    { 
                        try
                        {
                            myWriter.writeObject(cipher.encode("CLIENT CLOSING"));
                            myWriter.flush();
                            System.out.println("CLIENT CLOSING has been sent to server. ");
                            System.out.println();
                        }
                        catch(IOException exception)
                        {
                            System.out.println("Error: CLIENT CLOSING");
                        } 
                        finally {System.exit(0);}    
                    }
                }
            );

            setSize(600, 300);
            setVisible(true);
            
        }
        catch (Exception e)
        {
            System.out.println("Problem starting client!");
        }

    }

    public void run()
    {
        while (true)
        {
            try {
             	byte[] enBytes = (byte[]) myReader.readObject(); //The array of bytes received
                byte[] bytes = new byte[enBytes.length];  
                System.arraycopy(enBytes, 0, bytes, 0, enBytes.length);        		
                String currMsg = cipher.decode(bytes); 
                byte[] deBytes = currMsg.getBytes(StandardCharsets.UTF_8); 
			    outputArea.append(currMsg+"\n");
			    
                System.out.println("message received");
                // printDeMessage(enBytes, deBytes, currMsg);  //For each message that is decrypted, output the information to the console.
            }
            catch (Exception e)
            {
                System.out.println("Client closing successfully!");
                break;
            }
        }
        System.exit(0);
    }

    public void actionPerformed(ActionEvent e)
    {
        try
        {
            String currMsg = e.getActionCommand();      // Get input value
            if (currMsg.equals("") == false)
            {
            	inputField.setText("");
                String finalMsg = new String(myName + ":" + currMsg);   //combine string    
                byte[] msgBytes = finalMsg.getBytes(StandardCharsets.UTF_8); // The corresponding array of bytes
                byte[] enBytes = cipher.encode(finalMsg);  // The encrypted array of bytes
                myWriter.writeObject(enBytes);     // Add name and send it to Server
                myWriter.flush();

                System.out.println("message sent");
                // printEnMessage(enBytes, msgBytes, finalMsg);
            }
        }
        catch(IOException exception)
        {
        	outputArea.append("Error: MESSAGE SENDING!");
        }
                
    }

    // public void printDeMessage(byte[] enBytes, byte[] deBytes, String message)
    // {
    //     try{
    //         Thread.sleep(100);       
    //     }catch(InterruptedException ex){
    //         ex.printStackTrace();
    //     }
    //     System.out.println();
    //     System.out.println("------------------------------Decryption Information----------------------------------");
    //     System.out.println("The array of bytes received: ");  
    //     System.out.print("    ");     
    //     for (int i = 0; i < enBytes.length; i++)
    //         System.out.print(enBytes[i] + " ");
    //     System.out.println("\n");

    //     System.out.println("The decrypted array of bytes: "); 
    //     System.out.print("    ");     
    //     for (int i = 0; i < deBytes.length; i++)
    //         System.out.print(deBytes[i] + " ");
    //     System.out.println("\n");

    //     System.out.println("The corresponding String: ");
    //     System.out.print("    "); 
    //     System.out.println(message);
    //     System.out.println("--------------------------------------------------------------------------------------");
    //     System.out.println();  
    // } 

    // public void printEnMessage(byte[] enBytes, byte[] msgBytes, String message)
    // {
    //     System.out.println();
    //     System.out.println("------------------------------Encryption Information----------------------------------");
    //     System.out.println("The original String message: ");
    //     System.out.print("    ");      
    //     System.out.println(message);
    //     System.out.println();

	//     System.out.println("The corresponding array of bytes: "); 
    //     System.out.print("    ");      
    //     for (int i = 0; i < msgBytes.length; i++)
	// 	    System.out.print(msgBytes[i] + " ");
	//     System.out.println("\n");

	//     System.out.println("The encrypted array of bytes: "); 
    //     System.out.print("    ");    
	//     for (int i = 0; i < enBytes.length; i++)
	// 	    System.out.print(enBytes[i] + " ");
    //     System.out.println();  
	//     System.out.println("--------------------------------------------------------------------------------------");
    // } 

    public static void main(String [] args)
    {
        SecureChatClient JR = new SecureChatClient();
        JR.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
    }
}
