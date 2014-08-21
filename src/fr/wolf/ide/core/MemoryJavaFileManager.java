package fr.wolf.ide.core;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.tools.FileObject;
import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.JavaFileObject.Kind;

public class MemoryJavaFileManager extends ForwardingJavaFileManager<JavaFileManager>
{
    private final Map<String, MemoryJavaFileObject> map = new HashMap<String, MemoryJavaFileObject>();

    private final ClassLoader loader = new ClassLoader()
    {
        @Override
        public Class<?> loadClass(String name) throws ClassNotFoundException
        {
            return findClass(name);
        }

        @Override
        protected Class<?> findClass(String name) throws ClassNotFoundException
        {
            MemoryJavaFileObject javaObject = map.get(name);
            if(javaObject != null)
            {
                byte[] bytes = javaObject.getByteCode();
                return defineClass(name, bytes, 0, bytes.length);
            }
            else
            {
                return Thread.currentThread().getContextClassLoader().loadClass(name);
            }
        }
    };

    public MemoryJavaFileManager(JavaFileManager manager)
    {
        super(manager);
    }

    @Override
    public JavaFileObject getJavaFileForOutput(Location location, String className, Kind kind, FileObject sibling) throws IOException
    {
        JavaFileObject javaObject = super.getJavaFileForOutput(location, className, kind, sibling);
        MemoryJavaFileObject myJavaObject = new MemoryJavaFileObject(javaObject);
        map.put(className, myJavaObject);
        return myJavaObject;
    }

    public ClassLoader getClassLoader()
    {
        return loader;
    }
}