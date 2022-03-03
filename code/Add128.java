import java.util.*;

public class Add128 implements SymCipher
{
    public byte [] key;

    public Add128()
    {
        key = new byte[128];
        new Random().nextBytes(key);
    }

    public Add128(byte [] byteArray)
    {
        key = byteArray;
    }

    // Return an array of bytes that represent the key for the cipher
    public byte [] getKey()
    {
        return key;
    } 

	// Encode the string using the key and return the result as an array of
	// bytes.  Note that you will need to convert the String to an array of bytes
	// prior to encrypting it.  Also note that String S could have an arbitrary
	// length, so your cipher may have to "wrap" when encrypting.
    public byte [] encode(String S)
    {
        byte [] by = S.getBytes();
        for (int i = 0; i < S.length(); i++)
            by[i] = (byte) (by[i] + key[i % 128]);
        return by;
    }

	// Decrypt the array of bytes and generate and return the corresponding String.
    public String decode(byte [] bytes)
    {
        for (int i = 0; i < bytes.length; i++)
            bytes[i] = (byte) (bytes[i] - key[i % 128]);
        String string = new String(bytes);
        return string;
    }
}