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

    public byte [] getKey()
    {
        return key;
    } 

    public byte [] encode(String S)
    {
        byte [] by = S.getBytes();
        for (int i = 0; i < S.length(); i++)
            by[i] = (byte) (by[i] + key[i % 128]);
        return by;
    }

    public String decode(byte [] bytes)
    {
        for (int i = 0; i < bytes.length; i++)
            bytes[i] = (byte) (bytes[i] - key[i % 128]);
        String string = new String(bytes);
        return string;
    }
}