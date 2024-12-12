import java.io.*;
import java.nio.file.Files;
import java.util.zip.InflaterInputStream;

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
      default -> System.out.println("Unknown command: " + command);
    }
  }
}
