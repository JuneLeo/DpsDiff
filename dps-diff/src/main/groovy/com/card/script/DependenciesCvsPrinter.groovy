package com.card.script


import org.gradle.api.artifacts.component.ComponentIdentifier
import org.gradle.api.artifacts.result.ResolvedArtifactResult
import org.gradle.api.artifacts.result.ResolvedComponentResult

public class DependenciesCvsPrinter {

    public String dir;

    DependenciesCvsPrinter(String path) {
        this.dir = path
    }

    String getOutputFile() {
        File file = new File(dir + File.separator + 'dependencies.csv')
        int index = 1;
        while (file.exists()) {
            file = new File(dir + File.separator + String.format('dependencies(%d).csv', index))
            index++
        }
        if(!file.getParentFile().exists()){
            file.getParentFile().mkdirs()
        }
        return file.getAbsolutePath()
    }

    void print(Set<ResolvedArtifactResult> resolvedArtifactResults, Set<ResolvedComponentResult> dependencyResults) {
        String outputFilePath = getOutputFile()
        Writer fileWriter = new FileWriter(outputFilePath)

        fileWriter.write("module");
        fileWriter.write(",");
        fileWriter.write("group")
        fileWriter.write(",")
        fileWriter.write("artifact")
        fileWriter.write(",")
        fileWriter.write("version")
        fileWriter.write(",")
        fileWriter.write("path")


        // 如果觉得依赖项过多，可以过滤
        // 获取本build.gradle对应项目的去除重复项的全部依赖
        // dependenciesConfiguration.getIncoming().getResolutionResult().getAllComponents()
        // 获取本build.gradle对应项目的去除不重复项的全部依赖
        // dependenciesConfiguration.getIncoming().getResolutionResult().getAllDependencies()
        // 获取本build.gradle中的写入的依赖，依赖的依赖请遍历获取
        // dependenciesConfiguration.getIncoming().getResolutionResult().getRoot().getDependencies()

        for (ResolvedComponentResult dependencyResult : dependencyResults) {

            String version = dependencyResult.getModuleVersion().getVersion()
            if (version == 'unspecified') {
                continue
            }
            String moduleAndVersion = dependencyResult.getModuleVersion().toString();
            String module = dependencyResult.getModuleVersion().getModule().toString()
            String group = dependencyResult.getModuleVersion().getModule().getGroup()
            String artifact = dependencyResult.getModuleVersion().getModule().getName()

            ResolvedArtifactResult resolvedArtifactResult = getArtifactResult(resolvedArtifactResults, moduleAndVersion)
            if (resolvedArtifactResult == null) {
                throw new RuntimeException("not find ResolvedArtifactResult")
            }
            fileWriter.write("\n")
            fileWriter.write(moduleAndVersion)
            fileWriter.write(",")
            fileWriter.write(group)
            fileWriter.write(",")
            fileWriter.write(artifact)
            fileWriter.write(",")
            fileWriter.write(version)
            fileWriter.write(",")
            fileWriter.write(resolvedArtifactResult.getFile().getAbsolutePath())


        }

        fileWriter.close()
    }

    ResolvedArtifactResult getArtifactResult(Set<ResolvedArtifactResult> resolvedArtifactResults, String module) {
        for (ResolvedArtifactResult result : resolvedArtifactResults) {
            ComponentIdentifier identifier = result.getId().getComponentIdentifier()
            if (identifier.getDisplayName() == module) {
                return result
            }
        }
        return null
    }
}