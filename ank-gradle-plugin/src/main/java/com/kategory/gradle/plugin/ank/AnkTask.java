package com.kategory.gradle.plugin.ank;

import org.gradle.api.file.FileCollection;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.JavaExec;
import org.gradle.api.tasks.TaskAction;

import java.util.List;

public class AnkTask extends JavaExec {

  @InputFiles
  FileCollection classpath;
  @Input
  List<String> args;


  @TaskAction
  public void ank() throws Exception {
    System.out.println("PGS classpath: " + classpath);
    System.out.println("PGS args: " + args);
  }
}
