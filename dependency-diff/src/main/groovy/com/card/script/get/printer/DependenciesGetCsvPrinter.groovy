package com.card.script.get.printer

import com.card.script.get.model.DependenciesGetModel

class DependenciesGetCsvPrinter implements DependenciesGetPrinter<DependenciesGetModel> {
    String outputPath

    DependenciesGetCsvPrinter(String path) {
        this.outputPath = path
    }

    @Override
    void printer(List<DependenciesGetModel> dependenciesGetModels) {
        File file = new File(outputPath)
        if(!file.getParentFile().exists()){
            file.getParentFile().mkdirs()
        }
        Writer fileWriter = new FileWriter(outputPath)

        fileWriter.write("module")
        fileWriter.write(",");
        fileWriter.write("group")
        fileWriter.write(",")
        fileWriter.write("artifact")
        fileWriter.write(",")
        fileWriter.write("version")
        fileWriter.write(",")
        fileWriter.write("path")

        for (DependenciesGetModel model : dependenciesGetModels) {
            fileWriter.write("\n")
            fileWriter.write(model.module)
            fileWriter.write(",")
            fileWriter.write(model.group)
            fileWriter.write(",")
            fileWriter.write(model.artifact)
            fileWriter.write(",")
            fileWriter.write(model.version)
            fileWriter.write(",")
            fileWriter.write(model.path)
        }
        fileWriter.close()
    }
}