import java.util.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class Substitute implements SymCipher
{
    public byte [] key;

    public Substitute()
    {
        Random ran = new Random();
        key = new byte[256];
        int mask = 129; //! used for initiate the array
        
        for (int i = 0; i < key.length; i++)
            key[i] = (byte) mask;
        
        for (int i = -128; i < 128; i++)
        {
            byte b = (byte) i;
            int a = ran.nextInt(256);
            while (key[a] != (byte) mask)
                a = ran.nextInt(256);
            key[a] = b;
        }
    }

    public Substitute(byte [] byteArray)
    {
		key = byteArray;
	}

	public byte [] getKey()
    {
        return key;
    } 

    public byte [] encode(String S)
    {
        byte [] string = S.getBytes(StandardCharsets.UTF_8);		
		byte [] encodeArr = new byte[string.length];
		for (int i = 0; i < string.length; i++)
            encodeArr[i] = key[(int)(string[i] & 0xFF)];
            
		return encodeArr;
    }

    public String decode(byte [] bytes)
    {
        StringBuilder string = new StringBuilder();
        byte [] dec = new byte [256];        
		for (int i = 0; i < 256; i++)
			dec[(int)(key[i] & 0xFF)] = (byte) i;

		for (int i = 0; i < bytes.length; i++)		
            string.append((char) dec[(int)(bytes[i] & 0xFF)]);
            
		return string.toString();
    }


}