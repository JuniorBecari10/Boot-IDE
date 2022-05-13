package ide.util;

import java.io.*;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

public final class Serialization {
	
	private Serialization() {}
	
	/** Write the object to a Base64 string. */
	public static String objectToString(Serializable o) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(baos);
		oos.writeObject(o);
		oos.close();
		return Base64.getEncoder().encodeToString(baos.toByteArray()); 
	}
	
	/** Read the object from Base64 string. */
	public static Object objectFromString(String s) throws IOException, ClassNotFoundException {
		byte[] data = Base64.getDecoder().decode(s);
		ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data));
		Object o = ois.readObject();
		ois.close();
		return o;
	}

	public static String serializeList(List<String> dataList) {
		StringBuilder builder = new StringBuilder();

		dataList.forEach((data) -> {
			builder.append(" ").append(data);
		});

		return builder.toString();
	}
	
	public static List<String> deserializeList(String encoded) {
        String[] arr = encoded.substring(1).split(" ");
        
        return Arrays.asList(arr);
    }
}
