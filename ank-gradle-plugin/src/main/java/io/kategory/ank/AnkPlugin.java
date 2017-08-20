package io.kategory.ank;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.tasks.JavaExec;

public class AnkPlugin implements Plugin<Project> {
  private static final String EXTENSION_NAME = "ank";
  private static final String TASK_NAME = "runAnk";

  @Override
  public void apply(Project target) {
    AnkExtension extension = new AnkExtension();
    target.getExtensions().add(EXTENSION_NAME, extension);
    target.afterEvaluate(project -> {
      JavaExec task = target.getTasks().create(TASK_NAME, JavaExec.class);
      task.setClasspath(extension.classpath);
      task.setMain("io.kategory.ank.main");
      task.setArgs(extension.arguments);
    });
  }
}