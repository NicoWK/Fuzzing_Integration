package org.example.vulnerabilities;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

public class FileOperations {

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

    public String writeToFile(String path, String text){
        try {
            FileWriter myWriter = new FileWriter(path);
            myWriter.write(text);
            myWriter.close();
            return "writing to file was successful\n";
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "writing to file gone wrong\n";
    }

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
