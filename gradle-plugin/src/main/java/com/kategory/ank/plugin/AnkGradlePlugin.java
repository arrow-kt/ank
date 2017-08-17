package com.kategory.ank.plugin;


import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.tasks.SourceSetContainer;

public class AnkGradlePlugin implements Plugin<Project> {
  static final String TASK_NAME = "ank";

  @Override
  public void apply(Project target) {
    SourceSetContainer sourceSets = (SourceSetContainer)
      target.getProperties().get("sourceSets");
    sourceSets.getByName("main").getRuntimeClasspath();
    target.getTasks().create(TASK_NAME, AnkTask.class);
  }
}