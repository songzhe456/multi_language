package com.ml.jpart;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 使用java来运行python的工具
 */
public class JavaPart {
    public static void main(){
        System.out.println("java部分已启动");
        try (InputStream inputStream = JavaPart.class.getClassLoader().getResourceAsStream("py_part.py")){
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            ArrayList<String> content = new ArrayList<>();
            String line;
            while((line = reader.readLine()) != null) {
                content.add(line);
            }
            StringBuilder fullCmd = new StringBuilder();
            for(String c : content) {
                fullCmd.append(c);
                if(c.endsWith(":") || c.endsWith(")") || c.startsWith("import")) {
                    fullCmd.append("\n");
                }
            }
            System.out.println("完整的：\n" + fullCmd);
            File temp = new File("./temp.py");
            FileWriter fw = new FileWriter(temp.getPath());
            try(BufferedWriter writer = new BufferedWriter(fw)) {
                writer.write(String.valueOf(fullCmd));
                writer.flush();
                runPythonPart(temp.getAbsolutePath());
            }
        } catch (IOException | InterruptedException e) {
            System.err.println("python部分启动失败：" + e);
        }
    }

    synchronized public static ArrayList<String> runPythonPart(String path) throws IOException, InterruptedException {
        /*
        通过ProcessBuilder运行python程序
         */
        List<String> cmd = new ArrayList<>();
        cmd.add("python");
        cmd.add(path);
        ProcessBuilder pd = new ProcessBuilder(cmd);
        pd.redirectErrorStream(true);
        Process pythonProcess = pd.start();
        /*
        一旦reader读到下文就继续读，如果无下文就停止
         */
        BufferedReader reader = new BufferedReader(new InputStreamReader(pythonProcess.getInputStream(), StandardCharsets.UTF_8));
        String line;
        ArrayList<String> output = new ArrayList<>();
        Pattern errPattern = Pattern.compile(".*(Error:|Exception:).*");
        while((line = reader.readLine()) != null){
            System.out.println(line);
            output.add(line);
            Matcher matcher = errPattern.matcher(line);
            if(matcher.matches()){
                throw new IOException("python执行异常");
            }
        }
        return output;
    }
}
