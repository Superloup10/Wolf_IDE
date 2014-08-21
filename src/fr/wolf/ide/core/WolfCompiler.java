package fr.wolf.ide.core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Arrays;
import java.util.Locale;
import java.util.logging.Logger;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaCompiler.CompilationTask;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

public class WolfCompiler
{
    private static final Logger logger = Logger.getLogger(WolfCompiler.class.getName());

    private static final JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();

    private static DiagnosticCollector<JavaFileObject> diagnostics;

    private static String className;

    private static boolean doCompilation(String sourceCode, JavaFileManager stdFileManager, Iterable<String> compilationOptions) throws Exception
    {
        String classShortName = "DynamicCompilation";
        String packages = "wolf.generated";
        className = packages + "." + classShortName;

        sourceCode = "package " + packages + ";" + "public class " + classShortName + " {" + "public static void main(String args){" + sourceCode + "}" + "}";

        SimpleJavaFileObject fileObject = new DynamicJavaSourceCodeObject(className, sourceCode);

        JavaFileObject[] javaFileObjects = new JavaFileObject[] {fileObject};

        diagnostics = new DiagnosticCollector<JavaFileObject>();

        Iterable<? extends JavaFileObject> compilationUnits = Arrays.asList(javaFileObjects);

        CompilationTask compilerTask = compiler.getTask(null, stdFileManager, diagnostics, compilationOptions, null, compilationUnits);

        stdFileManager.close();

        return compilerTask.call();

    }

    public static void doPhysicalCompilation(String sourceCode) throws Exception
    {
        StandardJavaFileManager stdFileManager = compiler.getStandardFileManager(null, Locale.getDefault(), null);

        String[] compileOptions = new String[] {"-d", "build/classes"};
        Iterable<String> compilationOptions = Arrays.asList(compileOptions);

        boolean status = doCompilation(sourceCode, stdFileManager, compilationOptions);
        if(!status)
        {
            for(Diagnostic diagnostic : diagnostics.getDiagnostics())
            {
                System.out.format("Error on line %d in %s", diagnostic.getLineNumber(), diagnostic);
            }
        }
        else
        {
            if(className != null)
                executeClass(className);
        }
    }

    public static void doMemoryCompilation(String sourceCode) throws Exception
    {
        JavaFileManager mFileManager = new MemoryJavaFileManager(compiler.getStandardFileManager(null, Locale.getDefault(), null));

        String[] compileOptions = new String[0];
        Iterable<String> compilationOptions = Arrays.asList(compileOptions);

        boolean status = doCompilation(sourceCode, mFileManager, compilationOptions);

        if(!status)
        {
            for(Diagnostic diagnostic : diagnostics.getDiagnostics())
            {
                System.out.format("Error on line %d in %s", diagnostic.getLineNumber(), diagnostic);
            }
        }
        else
        {
            if(className != null)
            {
                executeClassFromMemory(mFileManager);
            }
        }
    }

    private static void executeClass(String className) throws IOException
    {
        URL classpathURL = WolfCompiler.class.getResource("/");
        String classPath = classpathURL.getPath().replaceAll("%20", " ");
        classPath = classPath.substring(1);
        String cmd = "java -classpath \"" + classPath + "\" " + className;
        String line;
        Process p = Runtime.getRuntime().exec(cmd);
        InputStream stout = p.getInputStream();
        BufferedReader brCleanUp = new BufferedReader(new InputStreamReader(stout));
        while((line = brCleanUp.readLine()) != null)
        {
            System.out.println(line);
        }
        brCleanUp.close();
    }

    private static void executeClassFromMemory(JavaFileManager mFileManager) throws Exception
    {
        ClassLoader loader = ((MemoryJavaFileManager)mFileManager).getClassLoader();

        Class<?> clazz = Class.forName(className, true, loader);

        Method m = clazz.getMethod("main", new Class[] {String[].class});
        Object[] _args = new Object[] {new String[0]};
        m.invoke(null, _args);
    }
}