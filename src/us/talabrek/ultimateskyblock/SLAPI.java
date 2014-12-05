package us.talabrek.ultimateskyblock;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class SLAPI
{
  public SLAPI() {}
  
  public static void save(Object obj, File path)
    throws Exception
  {
    ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(path));
    oos.writeObject(obj);
    oos.flush();
    oos.close();
  }
  
  public static Object load(File path)
    throws Exception
  {
    ObjectInputStream ois = new ObjectInputStream(new FileInputStream(path));
    Object result = ois.readObject();
    ois.close();
    return result;
  }
}
