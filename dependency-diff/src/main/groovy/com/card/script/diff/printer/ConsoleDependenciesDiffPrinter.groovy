package com.card.script.diff.printer

import com.card.script.diff.DependenciesDiffTask
import com.card.script.diff.model.DependenciesDiffFileModel
import com.card.script.diff.model.DependenciesDiffModel
import org.gradle.api.Project

import java.util.function.Consumer


class ConsoleDependenciesDiffPrinter implements IDependenciesDiffPrinter{
    Project mProject

    ConsoleDependenciesDiffPrinter(Project project){
        this.mProject = project
    }

    @Override
    void printer(List<DependenciesDiffModel> diffModels) {
        diffModels.forEach(new Consumer<DependenciesDiffModel>() {
            @Override
            void accept(DependenciesDiffModel diffModel) {
                println('------------------------------------------------------------------------')
                println(String.format("module : %s", diffModel.getModule()))
                if (diffModel.status == DependenciesDiffModel.STATUS_NEW) {
                    println(diffModel.getModelInfo())
                    println("添加")
                } else if (diffModel.status == DependenciesDiffModel.STATUS_DELETE) {
                    println(diffModel.getOldModelInfo())
                    println("删除")
                } else {
                    println(diffModel.getModelInfo())
                    println(diffModel.getOldModelInfo())
                }
                println("diff size : " + diffModel.diff)
                println('------------------------------------------------------------------------')
                if (diffModel.dependenciesDiffFileModels != null) {
                    diffModel.dependenciesDiffFileModels.forEach(new Consumer<DependenciesDiffFileModel>() {
                        @Override
                        void accept(DependenciesDiffFileModel diffFileModel) {
                            if (diffFileModel.diffFileSize != 0) {
                                println("    " + diffFileModel.getChange() + " : " + diffFileModel.fileShortPath + ", " + diffFileModel.diffFileSize)
                            }
                        }
                    })
                }

            }
        })
    }
}