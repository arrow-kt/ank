package com.kategory.gradle.plugin.ank;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.tasks.SourceSetContainer;

public class AnkPlugin implements Plugin<Project> {
  static final String TASK_NAME = "ank";

  @Override
  public void apply(Project target) {
    // TODO This is a test!
    SourceSetContainer sourceSets = (SourceSetContainer)
      target.getProperties().get("sourceSets");
    sourceSets.getByName("main").getRuntimeClasspath();

    target.getTasks().create(TASK_NAME, AnkTask.class);
  }
}