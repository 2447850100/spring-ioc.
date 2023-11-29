package org.springframework.container;

import lombok.SneakyThrows;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;
import org.springframework.xml.XmlParser;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * @Version 1.0
 * @Author huqiang
 * @Description ClassPathXmlApplicationContext
 * @Date 2023/11/29 11:27
 **/
public class ClassPathXmlApplicationContext {

    private static final Logger logger = LogManager.getLogger(ClassPathXmlApplicationContext.class);

    /**
     * spring的ioc名字作为key
     */
    private final Map<String, Object> iocNameContainer = new ConcurrentHashMap<>();
    /**
     * spring的class作为key
     */
    private final Map<Class<?>, Object> iocClassContainer = new ConcurrentHashMap<>();

    /**
     * 根据接口，获取接口下的实现类
     * 类似 context.getBean(UserService.class)
     */
    private final Map<Class<?>, List<Object>> iocInterfacesContainer = new ConcurrentHashMap<>();

    private final Set<String> classFiles = new HashSet<>();

    private final String xmlPath;

    public ClassPathXmlApplicationContext(String xmlPath) {
        this.xmlPath = xmlPath;
        refresh();
    }

    @SneakyThrows
    private void refresh() {
        //解析componentScanPath 包扫描路径
        String componentScanPath = XmlParser.parse(xmlPath);

        //获取包扫描路径的class文件路径
        File file = findClassPath(componentScanPath);

        //获取.class文件结尾的包全路径名
        findClassFiles(file, componentScanPath, classFiles);

        //反射
        newInstance(classFiles);

        //实现对象的属性的依赖注入
        doDI();

        logger.fatal("iocNameContainer {}", iocNameContainer);
        logger.fatal("iocClassContainer {}", iocClassContainer);
        logger.fatal("iocInterfacesContainer {}", iocInterfacesContainer);
    }

    private void doDI() {
        Set<Map.Entry<Class<?>, Object>> entries = iocClassContainer.entrySet();
        entries.forEach(it -> {
            Class<?> aClass = it.getKey();
            Field[] declaredFields = aClass.getDeclaredFields();
            Set<Field> hasAutowiredField = Arrays.stream(declaredFields).filter(field -> field.isAnnotationPresent(Autowired.class)).collect(Collectors.toSet());
            hasAutowiredField.forEach(field -> {
                //依赖注入属性
                Autowired annotation = field.getAnnotation(Autowired.class);
                String value = annotation.value();
                Object bean;
                if ("".equals(value)) {
                    //默认按类型获取
                    Class<?> type = field.getType();
                    bean = getBean(type);
                    if (Objects.isNull(bean)) {
                        throw new IllegalStateException("获取不到 bean: " + type.getName());
                    }
                } else {
                    //按用户填写的beanName获取
                    bean = iocNameContainer.getOrDefault(value, new IllegalArgumentException("找不到beanName: " + value));
                }
                try {
                    field.setAccessible(true);
                    field.set(iocClassContainer.get(aClass), bean);
                } catch (IllegalAccessException e) {
                    logger.error("属性注入失败 {}", e.getMessage());
                }
            });
        });
    }

    private static File findClassPath(String componentScanPath) {
        String path = Objects.requireNonNull(Thread.currentThread().getContextClassLoader().getResource("")).getPath();
        String url = path + componentScanPath.replace(".", File.separator);
        // windows环境去除路径前面的 '/'
        if (System.getProperty("os.name").toLowerCase().contains("win")) {
            url = url.replaceFirst("/", "");
        }
        if (url.contains("test-classes")) {
            url = url.replace("test-classes", "classes");
        }
        return new File(url);
    }

    private void newInstance(Set<String> classFiles) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        for (String classFile : classFiles) {
            try {
                classFile = classFile.replace(File.separator, ".").replace(".class", "");
                Class<?> c = Class.forName(classFile);
                if (c.isAnnotationPresent(Component.class) || c.isAnnotationPresent(Service.class) || c.isAnnotationPresent(Controller.class)) {
                    String beanName = getBeanName(c);
                    Object instance = c.newInstance();
                    Class<?>[] interfaces = c.getInterfaces();
                    for (Class<?> anInterface : interfaces) {
                        iocInterfacesContainer.computeIfAbsent(anInterface, k -> new ArrayList<>()).add(instance);
                    }
                    iocNameContainer.compute(beanName, (key, value) -> {
                        if (value != null) {
                            throw new IllegalStateException("Bean with name '" + beanName + "' already exists.");
                        }
                        return instance;
                    });
                    iocClassContainer.compute(c, (key, value) -> {
                        if (value != null) {
                            throw new IllegalStateException("Bean with class name '" + c.getSimpleName() + "' already exists.");
                        }
                        return instance;
                    });
                }
            } catch (Exception e) {
                logger.error("构造bean失败 失败原因 {}", e.getMessage());
                throw e;
            }
        }
    }

    public Object getBean(String beanName) {
        return iocNameContainer.getOrDefault(beanName, null);
    }

    public <T> T getBean(Class<T> clazz) {
        //首先根据class获取，获取不到再通过接口获取
        if (iocClassContainer.containsKey(clazz)) {
            return (T) iocClassContainer.get(clazz);
        }
        List<Object> computed = iocInterfacesContainer.compute(clazz, (key, value) -> {
            if (value == null || value.isEmpty()) {
                return null;
            }
            if (value.size() > 1) {
                throw new IllegalArgumentException("只能获取到一个bean 但是获取到了 " + value.size() + "个相同类型的bean");
            }
            return value;
        });
        return computed == null ? null : (T) computed.get(0);
    }

    public static String getBeanName(Class<?> c) {
        Annotation[] annotations = new Annotation[]{c.getAnnotation(Component.class), c.getAnnotation(Controller.class), c.getAnnotation(Service.class)};
        try {
            Annotation annotation = Arrays.stream(annotations).filter(Objects::nonNull).findFirst().orElseThrow(() -> new IllegalArgumentException("未找到注解类型"));
            Method valueMethod = annotation.annotationType().getDeclaredMethod("value");
            String value = (String) valueMethod.invoke(annotation);
            if (value != null && !value.isEmpty()) {
                return value;
            }
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            // 处理异常: 可能是注解没有value()方法，或者其他反射调用错误
            logger.error("获取beanName 失败 {}", e.getMessage());
        }
        //没指定beanName 默认用类型首字母小写
        return Character.toLowerCase(c.getSimpleName().charAt(0)) + c.getSimpleName().substring(1);
    }

    private void findClassFiles(File classFiles, String componentScanPath, Set<String> classNameList) {
        File[] files = classFiles.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isFile() && file.getName().endsWith(".class")) {
                    // 如果是.class文件，添加到列表
                    String fullPath = file.getAbsolutePath();
                    int index = fullPath.indexOf(componentScanPath.replace(".", File.separator));
                    if (index != -1) {
                        String filePath = fullPath.substring(index);
                        classNameList.add(filePath);
                    }
                } else if (file.isDirectory()) {
                    // 如果是目录，递归调用
                    findClassFiles(file, componentScanPath, classNameList);
                }
            }
        }
    }
}