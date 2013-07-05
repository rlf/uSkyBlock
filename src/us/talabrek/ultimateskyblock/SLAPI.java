package us.talabrek.ultimateskyblock;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class SLAPI {
	public static void save(Object obj, File path) throws Exception {
		final ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(path));
		oos.writeObject(obj);
		oos.flush();
		oos.close();
	}

	public static Object load(File path) throws Exception {
		final ObjectInputStream ois = new ObjectInputStream(new FileInputStream(path));
		final Object result = ois.readObject();
		ois.close();
		return result;
	}
}