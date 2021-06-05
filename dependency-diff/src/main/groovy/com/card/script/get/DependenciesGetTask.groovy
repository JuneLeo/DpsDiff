package com.card.script.get

import com.android.build.gradle.AppExtension
import com.android.build.gradle.AppPlugin
import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.LibraryPlugin
import com.android.build.gradle.api.ApplicationVariant
import com.android.build.gradle.api.LibraryVariant
import com.card.script.get.generator.DependenciesGetComponentGeneratorImpl
import com.card.script.get.generator.IDependenciesGetGenerator
import com.card.script.get.model.DependenciesGetModel
import com.card.script.get.printer.DependenciesGetCsvPrinter
import com.card.script.get.printer.DependenciesGetPrinter
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction

class DependenciesGetTask extends DefaultTask {


    public String outType = 'csv'

    @Input
    public String[] exclude

    @Input
    public String classpath = 'compile'   // compile,runtime,all


    @TaskAction
    void action() {
        def androidPlugin = [AppPlugin, LibraryPlugin]
                .collect { project.plugins.findPlugin(it) }
                .find()
        String variantName
        if (androidPlugin instanceof AppPlugin) {
            ApplicationVariant applicationVariant = ((AppExtension) androidPlugin.getExtension()).getApplicationVariants().find()
            variantName = applicationVariant.name
        } else if (androidPlugin instanceof LibraryPlugin) {
            LibraryVariant libraryVariant = ((LibraryExtension) androidPlugin.getExtension()).getLibraryVariants().find()
            variantName = libraryVariant.name
        } else {
            throw new RuntimeException("please use task in AppPlugin or LibraryPlugin")
        }

        IDependenciesGetGenerator generator = new DependenciesGetComponentGeneratorImpl(project, variantName)
        List<DependenciesGetModel> dependenciesGetModels = generator.generator(classpath)
        handleExclude(dependenciesGetModels)


        if (outType == 'csv') {
            String outputFile = project.buildDir.getAbsolutePath() + '/dependencies/dependencies.csv'
            DependenciesGetPrinter printer = new DependenciesGetCsvPrinter(outputFile)
            printer.printer(dependenciesGetModels)
        }
    }

    def handleExclude(List<DependenciesGetModel> dependenciesGetModels) {
        Iterator<DependenciesGetModel> iterator = dependenciesGetModels.iterator()
        if (exclude != null && exclude.length > 0) {
            while (iterator.hasNext()) {
                DependenciesGetModel model = iterator.next()
                for (String excl : exclude) {
                    if (model.module.contains(excl)) {
                        iterator.remove()
                    }
                }
            }
        }
    }

}
