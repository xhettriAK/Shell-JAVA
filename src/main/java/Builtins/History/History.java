package Builtins.History;

import Builtins.BuiltInCommands;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class History implements BuiltInCommands {
    public record historyEntries(String cmd, long when, String cwd, List<String> paths){ }
    static Path historyDir = Paths.get(System.getProperty("user.home"), ".local", "share", "Basic_shell");
    static Path historyFile = historyDir.resolve("history.yaml");


    public static void writeHistory(historyEntries historyList) throws IOException {
        try{
            if(!Files.isDirectory( historyDir)){
                Files.createDirectories(historyDir);
            }
            if(Files.notExists(historyFile)){
                Files.createFile(historyFile);
            }
        } catch (IOException e) {
            System.err.println("Can't Create History File");
        }
        String cmd = getCmd(historyList);

        try(BufferedWriter write = new BufferedWriter(new FileWriter(historyFile.toFile(),true))){
            write.write("- cmd: "+"\""+cmd+"\"");
            write.newLine();
            write.write("  when: "+historyList.when());
            write.newLine();
            write.write("  cwd: "+historyList.cwd());
            write.newLine();
            if(historyList.paths.isEmpty()){
                write.write("  paths: null");
                write.newLine();
            }else{
                write.write("  paths: ");
                for( int i = 0; i<historyList.paths().size();i++){
                    write.write("  - "+"\""+historyList.paths.get(i)+"\"");
                    write.newLine();
                }
            }
        }

    }
    private static String getCmd(historyEntries history) {
        StringBuilder builder = new StringBuilder();
        String s = history.cmd();
        for(int i = 0 ; i<s.length(); i++) {
            char c = s.charAt(i);
            if (c == '\"' || c == '\\') {
                builder.append('\\');
            }
            if(c=='\t'){
                builder.append('\\');
                builder.append('t');
                continue;
            }
            if(c=='\n'){
                builder.append('\\');
                builder.append('n');
                 continue;
            }
            builder.append(c);
        }
        return builder.toString();
    }

    public static void Read()   {
        File file = new File(historyFile.toUri());
        if(file.length()==0){
            System.out.println("History is Empty, Type Something :)");
        }else {
            ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
            try {

                List<historyEntries> historyList = mapper.readValue(new File(historyFile.toUri()), new TypeReference<>() {
                });
                for (var readCurrentHistoryLine : historyList) {
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss a");
                    LocalDateTime timeFormat = LocalDateTime.ofInstant(Instant.ofEpochMilli(readCurrentHistoryLine.when), ZoneOffset.UTC);
                    String time = timeFormat.format(formatter);
                    System.out.println(time + " " + readCurrentHistoryLine.cmd() + " ");
                    if (readCurrentHistoryLine.paths() != null) {
                        for (var p : readCurrentHistoryLine.paths()) {
                            System.out.print(p + " ");
                        }
                    }
                }
            } catch (IOException e) {

                System.out.println("Something went wrong");
            }
        }

    }

    public static void run(String command){
        Read();
    }
    public static void create(String Value) {
        CircularBuffer.Add(Value);
    }
    public static void print() {
        int size = CircularBuffer.getCurrentSize();
        for( int i = 0 ; i<size; i++){
            String value = CircularBuffer.getValue(i);
            if(value==null){
                System.out.println("something went wrong");
                break;
            }
            System.out.println(i+1+" "+ value);

        }
    }
    public static void print(int entries) {
        int size = CircularBuffer.getCurrentSize();
        int front = CircularBuffer.getFront();
        for( int i = entries ; i>=1; i++){
            int recent = size-i;
            int index = (front+recent)%size;
            String value = CircularBuffer.getValue(index);
            if(value==null){
                System.out.println("something went wrong");
                break;
            }
            System.out.println(i+1+" "+ value);

        }

    }
}
