package com.card.script.diff


import org.gradle.api.Project

import java.util.function.Consumer


class ConsoleDependenciesDiffPrinter implements IDependenciesDiffPrinter{
    Project mProject

    ConsoleDependenciesDiffPrinter(Project project){
        this.mProject = project
    }

    @Override
    void printer(List<DependenciesDiffTask.DependenciesDiffModel> diffModels) {
        diffModels.forEach(new Consumer<DependenciesDiffTask.DependenciesDiffModel>() {
            @Override
            void accept(DependenciesDiffTask.DependenciesDiffModel diffModel) {
                println('------------------------------------------------------------------------')
                println(String.format("module : %s", diffModel.getModule()))
                if (diffModel.status == DependenciesDiffTask.DependenciesDiffModel.STATUS_NEW) {
                    println(diffModel.getModelInfo())
                    println("添加")
                } else if (diffModel.status == DependenciesDiffTask.DependenciesDiffModel.STATUS_DELETE) {
                    println(diffModel.getOldModelInfo())
                    println("删除")
                } else {
                    println(diffModel.getModelInfo())
                    println(diffModel.getOldModelInfo())
                }
                println("diff size : " + diffModel.diff)
                println('------------------------------------------------------------------------')
                if (diffModel.dependenciesDiffFileModels != null) {
                    diffModel.dependenciesDiffFileModels.forEach(new Consumer<DependenciesDiffTask.DependenciesDiffFileModel>() {
                        @Override
                        void accept(DependenciesDiffTask.DependenciesDiffFileModel diffFileModel) {
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