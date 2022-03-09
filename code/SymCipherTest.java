import java.math.*;
import java.util.*;

public class SymCipherTest
{
	public static void main (String [] args)
	{
		System.out.println("Testing Substitution Cipher\n");
		String test1 = new String("abcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuzwxyzABCDEFGHIJKLMNOPQRSTUVWXYZABCDEFGHIJKLMNOPQRSTUVWXYZ012345678901234567890123");
		String test2 = new String("+-)(*&^%$#@!");
		
		SymCipher sim = new Substitute();
		
		System.out.println("Original Text (before encryption): \n" + test1 + "\n");
		
		// Get encoded bytes and show then (for testing purposes)
		byte [] bytes1 = sim.encode(test1);
		System.out.println("Encoded bytes: ");
		for (int i = 0; i < bytes1.length; i++)
		{
			System.out.print(bytes1[i] + " ");
		}
		System.out.println("\n");
		
		byte [] subKey = sim.getKey();  // get the key from the cipher
		System.out.println("Substitution Cipher Key (as bytes): ");
		for (int i = 0; i < subKey.length; i++)
			System.out.print(subKey[i] + " ");
		System.out.println("\n");
		
		SymCipher sim2 = new Substitute(subKey);
		
		String restore1 = sim2.decode(bytes1);
		System.out.println("Restored String: \n" + restore1 + "\n");
		
		System.out.println("Original Text (before encryption): \n" + test2);
		System.out.println("Original Text contains " + test2.length() + " characters\n");
		
		byte [] origBytes = test2.getBytes();
		System.out.println("Original bytes: ");
		for (int i = 0; i < origBytes.length; i++)
		{
			System.out.print(origBytes[i] + " ");
		}
		System.out.println();
		System.out.println("There are " + origBytes.length + " bytes\n");
		
		byte [] bytes2 = sim.encode(test2);
		System.out.println("Encoded bytes: ");
		for (int i = 0; i < bytes2.length; i++)
		{
			System.out.print(bytes2[i] + " ");
		}
		System.out.println("\n");
		String restore2 = sim2.decode(bytes2);
		System.out.println("Restored String: " + restore2 + "\n");
		
		System.out.println("Testing Add128 Cipher\n");
		sim = new Add128();
		bytes1 = sim.encode(test1);
		
		byte [] addKey = sim.getKey();
		System.out.println("Add128 Cipher Key (as bytes): ");
		for (int i = 0; i < addKey.length; i++)
			System.out.print(addKey[i] + " ");
		System.out.println("\n");
		
		sim2 = new Add128(addKey);
		
		restore1 = sim2.decode(bytes1);
		System.out.println("Restored String (add128): " + restore1);
		System.out.println();
		
		bytes2 = sim.encode(test2);
		restore2 = sim2.decode(bytes2);
		System.out.println("Restored String (add128): " + restore2);
		
		
		StringBuilder longString = new StringBuilder("");
		for (int i = 0; i < 500; i++)
		{
			char val = (char) ('A' + i % 26);
			longString.append(val);
		}
		String test3 = longString.toString();
		
		System.out.println("\nTesting block handling of Add128");
		System.out.println("Original String:");
		System.out.println(test3);
		
		bytes1 = sim.encode(test3);
		restore1 = sim.decode(bytes1);
		System.out.println("Restored String:");
		System.out.println(restore1);
	}
}
