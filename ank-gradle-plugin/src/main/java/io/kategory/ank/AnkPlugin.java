package io.kategory.ank;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.DependencyResolutionListener;
import org.gradle.api.artifacts.DependencySet;
import org.gradle.api.artifacts.ResolvableDependencies;
import org.gradle.api.tasks.JavaExec;

public class AnkPlugin implements Plugin<Project> {
  private static final String EXTENSION_NAME = "ank";
  private static final String TASK_NAME = "runAnk";
  private static final String ANK_CORE_DEPENDENCY = "io.kategory:ank-core:0.1.0";

  @Override
  public void apply(Project target) {
    AnkExtension extension = new AnkExtension();
    target.getExtensions().add(EXTENSION_NAME, extension);
    DependencySet compileDeps = target.getConfigurations().getByName("compile").getDependencies();
    target.getGradle().addListener(new DependencyResolutionListener() {
      @Override
      public void beforeResolve(ResolvableDependencies resolvableDependencies) {
        compileDeps.add(target.getDependencies().create(ANK_CORE_DEPENDENCY));
        target.getGradle().removeListener(this);
      }

      @Override
      public void afterResolve(ResolvableDependencies resolvableDependencies) {}
    });
    target.afterEvaluate(project -> {
      JavaExec task = target.getTasks().create(TASK_NAME, JavaExec.class);
      task.setClasspath(extension.classpath);
      task.setMain("io.kategory.ank.main");
      task.setArgs(extension.arguments);
    });
  }
}