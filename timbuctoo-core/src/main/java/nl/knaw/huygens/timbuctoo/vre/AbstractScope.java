package nl.knaw.huygens.timbuctoo.vre;

import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.Set;

import nl.knaw.huygens.timbuctoo.model.DomainEntity;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet.Builder;
import com.google.common.reflect.ClassPath;
import com.google.common.reflect.ClassPath.ClassInfo;

public abstract class AbstractScope implements Scope {

  private Builder<Class<? extends DomainEntity>> builder;
  private ClassPath classPath;

  private Set<Class<? extends DomainEntity>> baseTypes;
  private Set<Class<? extends DomainEntity>> allTypes;

  public AbstractScope() throws IOException {
    builder = new Builder<Class<? extends DomainEntity>>();
    classPath = ClassPath.from(AbstractScope.class.getClassLoader());
  }

  protected void addPackage(String name) throws IOException {
    Preconditions.checkState(builder != null);
    String packageName = name.replaceFirst("^timbuctoo", "nl.knaw.huygens.timbuctoo");
    for (ClassInfo info : classPath.getTopLevelClasses(packageName)) {
      addClass(info.load());
    }
  }

  protected void addClass(Class<?> cls) {
    Preconditions.checkState(builder != null);
    if (DomainEntity.class.isAssignableFrom(cls) && !Modifier.isAbstract(cls.getModifiers())) {
      @SuppressWarnings("unchecked")
      Class<? extends DomainEntity> type = (Class<? extends DomainEntity>) cls;
      builder.add(type);
    }
  }

  protected void fixBaseTypes() {
    Preconditions.checkState(builder != null);
    baseTypes = builder.build();
  }

  protected void fixAllTypes() {
    Preconditions.checkState(builder != null);
    allTypes = builder.build();
    builder = null;
    classPath = null;
  }

  @Override
  public Set<Class<? extends DomainEntity>> getBaseEntityTypes() {
    return baseTypes;
  }

  @Override
  public Set<Class<? extends DomainEntity>> getAllEntityTypes() {
    return allTypes;
  }

}
