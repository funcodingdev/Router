package cn.funcoding.router.processor;

import com.google.auto.service.AutoService;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Collections;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.JavaFileObject;

import cn.funcoding.router.annotations.Destination;

@AutoService(Processor.class)
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class DestinationProcessor extends AbstractProcessor {
    private static final String TAG = "DestinationProcessor ";

    /**
     * 告诉编译器，当前处理器支持的注解类型
     */
    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return Collections.singleton(
                Destination.class.getCanonicalName()
        );
    }

    /**
     * 编译器找到我们关心的注解之后，会回调这个方法
     *
     * @param set
     * @param roundEnvironment
     * @return
     */
    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        // 避免多次调用
        if (roundEnvironment.processingOver()) {
            return false;
        }
        System.out.println(TAG + ">>> process start.");

        String rootDir = processingEnv.getOptions().get("root_project_dir");
        System.out.println(TAG + ">>> rootDir=" + rootDir);
        File rootDirFile = new File(rootDir);
        if (!rootDirFile.exists()) {
            throw new RuntimeException("root_project_dir not exist");
        }

        // 获取所有标记了@Destination注解的类的信息
        Set<? extends Element> allDestinationElements = roundEnvironment.getElementsAnnotatedWith(Destination.class);
        // 当未收集到@Destination注解时，直接跳过
        if (allDestinationElements.size() < 1) {
            return false;
        }

        final JsonArray destinationJsonArray = new JsonArray();
        // 将要自动生成的类的类名
        final String className = "RouterMapping_" + System.currentTimeMillis();

        StringBuilder builder = new StringBuilder();
        builder.append("package cn.funcoding.router.mapping;\n\n");
        builder.append("import java.util.HashMap;\n");
        builder.append("import java.util.Map;\n\n");
        builder.append("public class ").append(className).append(" {\n\n");
        builder.append("    public static Map<String, String> get() {\n\n");
        builder.append("        Map<String, String> mapping = new HashMap<>();\n\n");
        // 遍历所有的@Destination注解信息，获取详细信息
        for (Element element : allDestinationElements) {
            final TypeElement typeElement = (TypeElement) element;
            final Destination destination = typeElement.getAnnotation(Destination.class);
            if (destination == null) {
                continue;
            }
            final String url = destination.url();
            final String description = destination.description();
            final String realPath = typeElement.getQualifiedName().toString();

            System.out.println(TAG + ">>> url= " + url);
            System.out.println(TAG + ">>> description= " + description);
            System.out.println(TAG + ">>> realPath= " + realPath);

            builder.append("        ")
                    .append("mapping.put(").append("\"").append(url).append("\"")
                    .append(", ").append("\"").append(realPath).append("\"")
                    .append(");\n");


            JsonObject item = new JsonObject();
            item.addProperty("url", url);
            item.addProperty("description", description);
            item.addProperty("realPath", realPath);
            destinationJsonArray.add(item);
        }
        builder.append("        return mapping;\n");
        builder.append("    }\n");
        builder.append("}\n");

        String mappingFullClassName = "com.funcoding.router.mapping." + className;

        System.out.println(TAG + " >>> mappingFullClassName = " + mappingFullClassName);
        System.out.println(TAG + " >>> class content = \n" + builder);

        try {
            JavaFileObject source = processingEnv.getFiler().createSourceFile(mappingFullClassName);
            Writer writer = source.openWriter();
            writer.write(builder.toString());
            writer.flush();
            writer.close();
        } catch (IOException e) {
            throw new RuntimeException("Error while create file", e);
        }
        // 创建router_mapping子目录
        File routerFileDir = new File(rootDirFile, "router_mapping");
        if (!routerFileDir.exists()) {
            routerFileDir.mkdir();
        }
        File mappingFile = new File(routerFileDir, "mapping_" + System.currentTimeMillis() + ".json");
        try {
            BufferedWriter out = new BufferedWriter(new FileWriter(mappingFile));
            String jsonStr = destinationJsonArray.toString();
            out.write(jsonStr);
            out.flush();
            out.close();
        } catch (Throwable e) {
            throw new RuntimeException("Error while write json", e);
        }

        System.out.println(TAG + ">>> process finish.");
        return false;
    }
}
