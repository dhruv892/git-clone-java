import java.io.*;
import java.nio.file.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.zip.InflaterInputStream;
import java.util.zip.DeflaterOutputStream;

public class Main {
  public static void main(String[] args){
    // You can use print statements as follows for debugging, they'll be visible when running tests.
//    System.err.println("Logs from your program will appear here!");

    // Uncomment this block to pass the first stage
    
    final String command = args[0];
    
    switch (command) {
      case "init" -> {
        final File root = new File(".git");
        new File(root, "objects").mkdirs();
        new File(root, "refs").mkdirs();
        final File head = new File(root, "HEAD");
    
        try {
          head.createNewFile();
          Files.write(head.toPath(), "ref: refs/heads/main\n".getBytes());
          System.out.println("Initialized git directory");
        } catch (IOException e) {
          throw new RuntimeException(e);
        }
      }
      case "cat-file" -> {
        if ( args.length != 3 ) System.out.println("wrong usage of cat-file");
        else if (args[1].equals("-p")) {
          String hash = args[2];
          String dirName = hash.substring(0, 2);
          String fileName = hash.substring(2);
          File blobFile = new File("./.git/objects/" + dirName + "/" + fileName);
          try{
            // inflater has been used here because Git uses Zlib to compress objects. :)
            String blob = new BufferedReader(new InputStreamReader(new InflaterInputStream(new FileInputStream(blobFile)))).readLine();
            String content = blob.substring(blob.indexOf("\0")+1);
            System.out.print(content);
          } catch (IOException e) {
            throw new RuntimeException(e);
          }

        }
      }
      case "hash-object" -> {
        String filePath = args[2];
        try {
          // Step 1: Read file content
          byte[] fileContent = Files.readAllBytes(Paths.get(filePath));

          // Step 2: Create the Git object header
          String header = "blob " + fileContent.length + "\0";
          byte[] fullContent = concatenate(header.getBytes(), fileContent);

          // Step 3: Compute SHA-1 hash
          MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
          byte[] sha1Hash = sha1.digest(fullContent);

          // Step 4: Convert hash to hex
          String hashHex = bytesToHex(sha1Hash);

          // Step 5: Write to .git/objects
          File gitDir = new File(".git/objects/" + hashHex.substring(0, 2));
          gitDir.mkdirs();
          File objectFile = new File(gitDir, hashHex.substring(2));

          try (FileOutputStream fos = new FileOutputStream(objectFile);
               DeflaterOutputStream dos = new DeflaterOutputStream(fos)) {
            dos.write(fullContent);
          }

          // Output the hash
          System.out.println(hashHex);
        } catch (Exception e){
          throw new RuntimeException(e);
        }


      }

      default -> System.out.println("Unknown command: " + command);
    }
  }
  // Helper function to concatenate byte arrays
  private static byte[] concatenate(byte[] a, byte[] b) {
    byte[] result = new byte[a.length + b.length];
    System.arraycopy(a, 0, result, 0, a.length);
    System.arraycopy(b, 0, result, a.length, b.length);
    return result;
  }

  // Helper function to convert bytes to hex
  private static String bytesToHex(byte[] bytes) {
    StringBuilder hexString = new StringBuilder();
    for (byte b : bytes) {
      String hex = Integer.toHexString(0xff & b);
      if (hex.length() == 1) hexString.append('0');
      hexString.append(hex);
    }
    return hexString.toString();
  }
}
