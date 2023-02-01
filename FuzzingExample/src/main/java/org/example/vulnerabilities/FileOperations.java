package org.example.vulnerabilities;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

public class FileOperations {

    /**
     * Deletes the specified file from the specified user's directory.
     * @param username the name of the user
     * @param filename the name of the file to be deleted
     * @return the status whether the operation was successful or not as a string.
     */
    public String deleteFile(String username, String filename){
        String path = username+File.separatorChar+filename;
        File f = new File(path);
        if(f.exists()){
            f.delete();
            while((f.getParentFile() != null) &&(f.getParentFile().isDirectory()) && (f.getParentFile().list().length == 0)){
                f = f.getParentFile();
                f.delete();
            }
           return "file deleted";
        }else{
            return "File does not exist.";
        }


    }

    /**
     * Creates a new file with the specified name in the specified user's directory.
     *
     * @param username the name of the user
     * @param filename the name of the file to be created
     * @return the path of the newly created file, an empty string otherwise.
     */
    public String createFile(String username, String filename){
        File f = null;
        //Verify directory starts with the username (the 'safe' directory)
        String filePath =username+File.separatorChar+filename+".txt";

            try {
                Files.createDirectories(Paths.get(username));
                f = new File(filePath);
                //Path parent = Paths.get(filePath).getParent();
                //Files.createDirectories(parent);
                if(!f.createNewFile()){
                    return "";
                }
            } catch (IOException e) {
                return "";
            }
        return filePath;
    }

    /**
     * Writes the specified text to a specified file.
     * @param path the path of the file to be written
     * @param text the text to be written to the file
     * @return the status whether the operation was successful or not as a string.
     */
    public String writeToFile(String path, String text){

        try {
            FileWriter myWriter = new FileWriter(path);
            myWriter.write(text);
            myWriter.close();
            return "writing to file was successful\n";
        } catch (IOException e) {
            //e.printStackTrace();
        }
        return "writing to file gone wrong\n";
    }

    /**
     * Returns an array of filenames in the specified user's directory.
     * @param username the name of the user
     * @return an array of filenames in the user's directory, null otherwise.
     */
    public String[] getFiles(String username){
        // Array to store the filenames
        File[] filelist;
        String[] filenames = null;
        // Creates a new File instance by converting the given pathname string
        // into an abstract pathname
        File f = new File(username);

        // Populates the array with names of files and directories
        filelist = f.listFiles();
        if (filelist != null){
            filenames= new String[filelist.length];
            for (int i = 0; i < filelist.length; i++) {
                if (filelist[i].isFile()) {
                    filenames[i] = "File: "+filelist[i].getName();
                } else if (filelist[i].isDirectory()) {
                    filenames[i] = "Directory: "+filelist[i].getName();
                }
            }
        }
        return filenames;
    }

    /**
     * Reads the content of the specified file in the specified user's directory.
     * @param username the name of the user
     * @param filename the name of the file to be read
     * @return the content of the file, an empty string otherwise.
     */
    public String readFile(String username, String filename){
        String line;
        String content="";
        String path = username+File.separatorChar+filename;
        File file = new File(path);

        try (BufferedReader br = new BufferedReader(new FileReader(file)))
        {
            while ((line = br.readLine()) != null) {
                content += (line+"\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return content;
    }

}
