package us.talabrek.ultimateskyblock;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class SLAPI {
	public static void save(Object obj, File path) throws Exception {
		/* 17 */ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(path));
		/* 18 */oos.writeObject(obj);
		/* 19 */oos.flush();
		/* 20 */oos.close();
	}

	public static Object load(File path) throws Exception {
		/* 24 */ObjectInputStream ois = new ObjectInputStream(new FileInputStream(path));
		/* 25 */Object result = ois.readObject();
		/* 26 */ois.close();
		/* 27 */return result;
	}
}