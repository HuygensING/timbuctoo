package nl.knaw.huygens.repository.model.util;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.reflect.ClassPath;
import com.google.common.reflect.ClassPath.ClassInfo;
import com.google.inject.Singleton;

import nl.knaw.huygens.repository.model.Document;
import nl.knaw.huygens.repository.variation.VariationUtils;

@Singleton
public class DocumentTypeRegister {
  private Map<String, Class<? extends Document>> stringToTypeMap;
  private Map<Class<? extends Document>, String> typeToStringMap;
  private List<String> unreadablePackages;
  private ClassPath classPath = null;
  
  public DocumentTypeRegister() {
    stringToTypeMap = Maps.newHashMap();
    typeToStringMap = Maps.newHashMap();
    unreadablePackages = Lists.newArrayList();
    try {
      classPath = ClassPath.from(this.getClass().getClassLoader());
    } catch (IOException e) {
      e.printStackTrace();
    }
    registerPackageFromClass(Document.class);
  }

  public String getTypeString(Class<? extends Document> docCls) {
    if (typeToStringMap.containsKey(docCls)) {
      return typeToStringMap.get(docCls);
    }
    return (VariationUtils.getBaseClass(docCls).getSimpleName()).toLowerCase();
  }

  public Class<? extends Document> getClassFromTypeString(String id) {
    if (stringToTypeMap.containsKey(id)) {
      return stringToTypeMap.get(id);
    }
    String className = StringUtils.capitalize(id);
    for (String packageName : unreadablePackages) {
      try {
        @SuppressWarnings("unchecked")
        Class<? extends Document> cls = (Class<? extends Document>) Class.forName(packageName + "." + className );
        return cls;
      } catch (Exception ex) {
        
      }
    }
    return null;
  }
  
  public void registerPackageFromClass(Class<?> cls) {
    registerPackage(cls.getPackage().getName());
  }
  
  @SuppressWarnings("unchecked")
  public void registerPackage(String packageId) {
    ImmutableSet<ClassInfo> classes = classPath.getTopLevelClasses(packageId);
    int classesDetected = 0;
    for (ClassInfo info : classes) {
      Class<?> cls = info.load();
      if (Document.class.isAssignableFrom(cls)) {
        Class<? extends Document> docCls = (Class<? extends Document>) cls;
        Class<? extends Document> baseClass = VariationUtils.getBaseClass(docCls);
        String typeId = baseClass.getSimpleName().toLowerCase();
        stringToTypeMap.put(typeId, docCls);
        typeToStringMap.put(docCls, typeId);
        classesDetected++;
      }
    }
    if (classesDetected > 0) {
      System.err.println("Dynamically identified " + classesDetected + " document types.");
    } else {
      System.err.println("No classes detected, adding package for runtime checking");
      unreadablePackages.add(packageId);
    }
  }

}
