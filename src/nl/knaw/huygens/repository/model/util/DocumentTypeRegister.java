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
import nl.knaw.huygens.repository.storage.mongo.MongoUtils;

@Singleton
public class DocumentTypeRegister {
  private Map<String, Class<? extends Document>> stringToTypeMap;
  private Map<Class<? extends Document>, String> typeToStringMap;
  private List<String> unreadablePackages;
  private ClassPath classPath = null;
  private Map<Class<? extends Document>, String> typeToCollectionIdMap;

  // TODO Make DocumentTypeRegister less dependent of MongoUtils.
  public DocumentTypeRegister() {
    stringToTypeMap = Maps.newHashMap();
    typeToStringMap = Maps.newHashMap();
    typeToCollectionIdMap = Maps.newHashMap();
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
    return MongoUtils.getCollectionName(docCls);
  }

  public Class<? extends Document> getClassFromTypeString(String id) {
    // NB: in the DB, package names will be prefixed to class names with a dash
    // (-) suffix.
    // These need to be removed in order to find the classes again:
    String normalizedId = id.replaceFirst("[a-z]*-", "");
    if (stringToTypeMap.containsKey(normalizedId)) {
      return stringToTypeMap.get(normalizedId);
    }
    String className = StringUtils.capitalize(normalizedId);
    for (String packageName : unreadablePackages) {
      try {
        @SuppressWarnings("unchecked")
        Class<? extends Document> cls = (Class<? extends Document>) Class.forName(packageName + "." + className);
        return cls;
      } catch (Exception ex) {

      }
    }
    return null;
  }

  public String getCollectionId(Class<? extends Document> docCls) {
    if (typeToCollectionIdMap.containsKey(docCls)) {
      return typeToCollectionIdMap.get(docCls);
    }
    String collectionId = MongoUtils.getCollectionName(getBaseClass(docCls));
    typeToCollectionIdMap.put(docCls, collectionId);
    return collectionId;
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
        String typeId = docCls.getSimpleName().toLowerCase();
        stringToTypeMap.put(typeId, docCls);
        typeToStringMap.put(docCls, typeId);
        Class<? extends Document> baseCls = getBaseClass(docCls);
        String baseTypeId = MongoUtils.getCollectionName(baseCls);
        typeToCollectionIdMap.put(docCls, baseTypeId);
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

  @SuppressWarnings("unchecked")
  private Class<? extends Document> getBaseClass(Class<? extends Document> cls) {
    Class<? extends Document> lastCls = cls;
    while (cls != null && !cls.equals(Document.class)) {
      lastCls = cls;
      cls = (Class<? extends Document>) cls.getSuperclass();
    }
    return lastCls;
  }

}
