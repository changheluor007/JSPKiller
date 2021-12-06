package org.sec;

import com.beust.jcommander.JCommander;
import com.google.googlejavaformat.java.Formatter;
import org.apache.log4j.Logger;
import org.objectweb.asm.ClassReader;
import org.sec.config.Command;
import org.sec.config.Logo;
import org.sec.service.ReflectionShellClassVisitor;

import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class Main {
    private static final Logger logger = Logger.getLogger(Main.class);

    public static void main(String[] args) {
        Logo.PrintLogo();
        logger.info("start code inspector");
        Command command = new Command();
        JCommander jc = JCommander.newBuilder().addObject(command).build();
        jc.parse(args);
        if (command.help) {
            jc.usage();
            return;
        }
        if (command.file == null || command.file.equals("")) {
            logger.error("file is null");
            return;
        }
        start(command);
    }

    private static void start(Command command) {
        try {
            Path path = Paths.get(command.file);
            if (!Files.exists(path)) {
                logger.error("webshell file not exits");
                return;
            }
            byte[] jspBytes = Files.readAllBytes(path);
            String jspCode = new String(jspBytes);
            jspCode = jspCode.replace("<%@", "");

            String tempCode = jspCode.split("<%")[1];
            String finalJspCode = tempCode.split("%>")[0];
            InputStream inputStream = Main.class.getClassLoader().getResourceAsStream("Webshell.java");
            if (inputStream == null) {
                logger.error("read template error");
                return;
            }
            StringBuilder resultBuilder = new StringBuilder();
            InputStreamReader ir = new InputStreamReader(inputStream);
            BufferedReader reader = new BufferedReader(ir);
            String lineTxt = null;
            while ((lineTxt = reader.readLine()) != null) {
                resultBuilder.append(lineTxt).append("\n");
            }
            ir.close();
            reader.close();

            String templateCode = resultBuilder.toString();
            String finalCode = templateCode.replace("__WEBSHELL__", finalJspCode);

            String formattedCode = new Formatter().formatSource(finalCode);
            Files.write(Paths.get("Webshell.java"), formattedCode.getBytes(StandardCharsets.UTF_8));

            File toolsPath = new File(
                    System.getProperty("java.home")
                            .replace("jre", "lib") +
                            File.separator + "tools.jar");
            URL url = toolsPath.toURI().toURL();
            URLClassLoader classLoader = new URLClassLoader(new java.net.URL[]{url});
            Class<?> toolClazz = classLoader.loadClass("javax.tools.ToolProvider");
            Method method = toolClazz.getDeclaredMethod("getSystemJavaCompiler");
            JavaCompiler compiler = (JavaCompiler) method.invoke(null);
            StandardJavaFileManager fileManager = compiler.getStandardFileManager(
                    null, null, null);
            Iterable<? extends JavaFileObject> compilationUnits = fileManager.getJavaFileObjects(
                    new File("Webshell.java"));
            List<String> optionList = new ArrayList<>();
            optionList.add("-classpath");
            optionList.add("lib.jar");
            optionList.add("-nowarn");
            JavaCompiler.CompilationTask task = compiler.getTask(null, fileManager,
                    null, optionList, null, compilationUnits);
            task.call();

            byte[] classData = Files.readAllBytes(Paths.get("Webshell.class"));
            Files.delete(Paths.get("Webshell.class"));
            Files.delete(Paths.get("Webshell.java"));

            try {
                ClassReader cr = new ClassReader(classData);
                ReflectionShellClassVisitor cv = new ReflectionShellClassVisitor();
                cr.accept(cv, ClassReader.EXPAND_FRAMES);
            } catch (Exception e) {
                logger.info("no reflection webshell");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
