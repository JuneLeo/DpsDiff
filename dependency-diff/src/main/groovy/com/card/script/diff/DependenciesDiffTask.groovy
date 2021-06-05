package com.card.script.diff

import com.card.script.diff.model.DependenciesDiffFileModel
import com.card.script.diff.model.DependenciesDiffModel
import com.card.script.diff.model.DependenciesModel
import com.card.script.diff.printer.ConsoleDependenciesDiffPrinter
import com.card.script.diff.printer.HtmlDependenciesDiffPrinter
import com.card.script.diff.printer.IDependenciesDiffPrinter
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction

class DependenciesDiffTask extends DefaultTask {

    @Input
    public String firstPath;

    @Input
    public String secondPath;

    public static final String BUILD_DIR = 'diff';
    //html,console
    @Input
    public String outType = "html";

    public String[] excludeModule
    public String[] excludeModuleFile

    public int[] excludeModuleStatus = [5, 6]


    @TaskAction
    def action() {
        File firstFilePath = project.file(firstPath)
        File secondFilePath = project.file(secondPath)

        if (!firstFilePath.exists() || !secondFilePath.exists()) {
            throw new RuntimeException("firstPath or secondPath not exists")
        }

        File buildDir = project.file(project.buildDir.getAbsolutePath() + File.separator + BUILD_DIR)
        if (buildDir.exists()) {
            buildDir.deleteDir()
        }

        List<DependenciesModel> firstDependenciesModels = getDependenciesModels(firstFilePath.getAbsolutePath())
        List<DependenciesModel> secondDependenciesModels = getDependenciesModels(secondFilePath.getAbsolutePath())


        List<DependenciesDiffModel> diffModels = new ArrayList<>();
        for (DependenciesModel firstDependencyModel : firstDependenciesModels) {
            println("正在扫描 ：" + firstDependencyModel.group + ":" + firstDependencyModel.artifact)
            if (!project.file(firstDependencyModel.path).exists()) {
                println("    [" + firstDependencyModel.module + "] not exists")
                continue
            }
            DependenciesModel secondDependencyModel = getSameModel(secondDependenciesModels, firstDependencyModel)
            //新添加的依赖
            if (secondDependencyModel == null) {
                DependenciesDiffModel newCreateModel = handleNewCreateModel(firstDependencyModel)
                diffModels.add(newCreateModel)
                println("    [" + firstDependencyModel.group + ":" + firstDependencyModel.artifact + "] is new")
                continue
            }
            //依赖版本相同
            if (secondDependencyModel.version == firstDependencyModel.version) {
                DependenciesDiffModel sameModel = new DependenciesDiffModel()
                sameModel.firstDependenciesModel = firstDependencyModel
                sameModel.secondDependenciesModel = secondDependencyModel
                sameModel.status = DependenciesDiffModel.STATUS_SAME_VERSION
                sameModel.handle()
                diffModels.add(sameModel)
                println("    [" + firstDependencyModel.module + "] not change")
                continue
            }
            println("    [" + firstDependencyModel.group + ":" + firstDependencyModel.artifact + "] start compare ...")
            //比较依赖
            DependenciesDiffModel diffModel = handleDiffModel(firstDependencyModel, secondDependencyModel)
            diffModels.add(diffModel)
        }


        for (DependenciesModel secondDependencyModel : secondDependenciesModels) {
            if (!project.file(secondDependencyModel.path).exists()) {
                continue
            }

            DependenciesModel firstDependencyModel = getSameModel(firstDependenciesModels, secondDependencyModel)
            //删除的依赖
            if (firstDependencyModel == null) {
                println("正在扫描 ：" + secondDependencyModel.group + ":" + secondDependencyModel.artifact)
                DependenciesDiffModel deleteModel = new DependenciesDiffModel();
                deleteModel.secondDependenciesModel = secondDependencyModel
                deleteModel.status = DependenciesDiffModel.STATUS_DELETE
                deleteModel.handle()
                diffModels.add(deleteModel)
                println("    [" + secondDependencyModel.module + "] 被删除")
            }
        }
        diffModels.sort()


        handleExclude(diffModels)


        if (outType == 'console') {
            IDependenciesDiffPrinter printer = new ConsoleDependenciesDiffPrinter(project)
            printer.printer(diffModels)
        } else {
            IDependenciesDiffPrinter printer = new HtmlDependenciesDiffPrinter(project, buildDir.getAbsolutePath())
            printer.printer(diffModels)
        }
    }

    String createDiffDir(DependenciesModel firstDependencyModel) {
        String moduleShotName = firstDependencyModel.group + ":" + firstDependencyModel.artifact

        File moduleRootDir = new File(project.buildDir.getAbsolutePath() + File.separator + BUILD_DIR + File.separator + moduleShotName)

        if (moduleRootDir.exists()) {
            moduleRootDir.deleteDir()
        }
        moduleRootDir.mkdirs()
        return moduleRootDir.getAbsolutePath()
    }

    DependenciesDiffModel handleNewCreateModel(DependenciesModel firstDependencyModel) {
        DependenciesDiffModel newCreateModel = new DependenciesDiffModel()

        //创建文件夹  group:artifact
        String moduleDir = createDiffDir(firstDependencyModel)

        //解压first
        String firstDependencyModulePath = moduleDir + File.separator + firstDependencyModel.module
        unzip(firstDependencyModel.path, firstDependencyModulePath)

        List<DependenciesDiffFileModel> diffDependenciesModelList = new ArrayList<>()
        int firstDependencyPathEndIndex = firstDependencyModulePath.length()
        for (File firstChildFile : project.fileTree(firstDependencyModulePath)) {
            DependenciesDiffFileModel newCreateFileDependenciesModel = new DependenciesDiffFileModel();
            newCreateFileDependenciesModel.firstFileSize = firstChildFile.length()
            newCreateFileDependenciesModel.fileChange = DependenciesDiffFileModel.CHANGE_NEW
            newCreateFileDependenciesModel.fileName = firstChildFile.getName()
            newCreateFileDependenciesModel.filePath = firstChildFile.getAbsolutePath()
            newCreateFileDependenciesModel.fileShortPath = firstChildFile.getAbsolutePath().substring(firstDependencyPathEndIndex)
            newCreateFileDependenciesModel.handle()
            diffDependenciesModelList.add(newCreateFileDependenciesModel)
        }

        newCreateModel.firstDependenciesModel = firstDependencyModel
        newCreateModel.dependenciesDiffFileModels = diffDependenciesModelList
        newCreateModel.status = DependenciesDiffModel.STATUS_NEW
        newCreateModel.handle()
        return newCreateModel
    }


    DependenciesDiffModel handleDiffModel(DependenciesModel firstDependencyModel, DependenciesModel secondDependencyModel) {
        DependenciesDiffModel diffModel = new DependenciesDiffModel();
        //创建文件夹  group:artifact
        String moduleDir = createDiffDir(firstDependencyModel)

        //解压first
        String firstDependencyModulePath = moduleDir + File.separator + firstDependencyModel.module
        unzip(firstDependencyModel.path, firstDependencyModulePath)
        //解压second
        String secondDependencyModulePath = moduleDir + File.separator + secondDependencyModel.module
        unzip(secondDependencyModel.path, secondDependencyModulePath)

        // compare
        List<DependenciesDiffFileModel> diffDependenciesModelList = new ArrayList<>();
        int firstDependencyPathEndIndex = firstDependencyModulePath.length()

        for (File firstChildFile : project.fileTree(firstDependencyModulePath)) {
            String firstChildPath = firstChildFile.getAbsolutePath()
            File secondChildFile = project.file(firstChildPath.replace(firstDependencyModel.module, secondDependencyModel.module))
            DependenciesDiffFileModel diffFileDependenciesModel = new DependenciesDiffFileModel();

            long firstChildFileSize = firstChildFile.length()
            diffFileDependenciesModel.firstFileSize = firstChildFileSize
            if (!secondChildFile.exists()) {
                diffFileDependenciesModel.fileChange = DependenciesDiffFileModel.CHANGE_NEW
            } else {
                long secondChildFileSize = secondChildFile.size()
                diffFileDependenciesModel.secondFileSize = secondChildFileSize
                if (firstChildFileSize > secondChildFileSize) {
                    diffFileDependenciesModel.fileChange = DependenciesDiffFileModel.CHANGE_HIGH
                } else if (firstChildFileSize < secondChildFileSize) {
                    diffFileDependenciesModel.fileChange = DependenciesDiffFileModel.CHANGE_LOW
                } else {
                    diffFileDependenciesModel.fileChange = DependenciesDiffFileModel.CHANGE_EQ
                }
            }
            diffFileDependenciesModel.filePath = firstChildPath
            diffFileDependenciesModel.fileShortPath = firstChildPath.substring(firstDependencyPathEndIndex)
            diffFileDependenciesModel.fileName = firstChildFile.getName()
            diffFileDependenciesModel.handle()
            diffDependenciesModelList.add(diffFileDependenciesModel)
        }


        int secondDependencyPathEndIndex = secondDependencyModulePath.length()

        for (File secondChildFile : project.fileTree(secondDependencyModulePath)) {
            String secondChildPath = secondChildFile.getAbsolutePath()
            File firstChildFile = project.file(secondChildPath.replace(secondDependencyModel.module, firstDependencyModel.module))
            if (!firstChildFile.exists()) {

                DependenciesDiffFileModel diffFileModel = new DependenciesDiffFileModel()
                diffFileModel.secondFileSize = secondChildFile.length()
                diffFileModel.filePath = secondChildPath
                diffFileModel.fileShortPath = secondChildPath.substring(secondDependencyPathEndIndex)
                diffFileModel.fileChange = DependenciesDiffFileModel.CHANGE_DEL
                diffFileModel.fileName = secondChildFile.getName()

                diffFileModel.handle()
                diffDependenciesModelList.add(diffFileModel)
            }
        }

        diffDependenciesModelList.sort()

        diffModel.firstDependenciesModel = firstDependencyModel
        diffModel.secondDependenciesModel = secondDependencyModel
        diffModel.dependenciesDiffFileModels = diffDependenciesModelList
        diffModel.status = DependenciesDiffModel.STATUS_CHANGE
        diffModel.handle()
        return diffModel
    }

    def unzip(String fromPath, String intoPath) {
        project.copy {
            from(project.zipTree(fromPath))
            into(intoPath)
        }

        File unzipDir = project.file(intoPath)
        if (unzipDir.exists() && unzipDir.isDirectory()) {
            File[] dirFiles = unzipDir.listFiles()
            for (File childFile : dirFiles) {
                if (childFile.isFile() && childFile.getName().contains("classes.jar")) {
                    unzip(childFile.getAbsolutePath(), childFile.getAbsolutePath() + "_dir")
                    childFile.delete()
                }
            }
        }
    }


    static DependenciesModel getSameModel(List<DependenciesModel> dependenciesModels, model) {
        for (DependenciesModel m : dependenciesModels) {
            if (m.group == model.group && m.artifact == model.artifact) {
                return m
            }
        }
        return null
    }


    List<DependenciesModel> getDependenciesModels(String path) {
        File file = new File(path);
        if (!file.exists()) {
            return null
        }
        List<DependenciesModel> models = new ArrayList<>()
        FileReader fileReader = new FileReader(file)
        try {
            List<String> strings = fileReader.readLines();
            for (String str : strings) {
                String[] arrays = str.split(",")

                if (arrays.length == 5) {
                    if (!project.file(arrays[4]).exists()) {
                        continue
                    }
                    DependenciesModel model = new DependenciesModel()
                    model.module = arrays[0]
                    model.group = arrays[1]
                    model.artifact = arrays[2]
                    model.version = arrays[3]
                    model.path = arrays[4]
                    model.size = project.file(arrays[4]).length()
                    models.add(model)
                }
            }
        } finally {
            fileReader.close()
        }

        return models
    }

    def handleExclude(List<DependenciesDiffModel> dependenciesModelList) {
        Iterator<DependenciesDiffModel> iterator = dependenciesModelList.iterator()

        while (iterator.hasNext()) {
            DependenciesDiffModel model = iterator.next()
            boolean excludeModule = isExcludeModule(model)
            // 移除module
            if (excludeModule) {
                iterator.remove()
                continue
            }
            if (model.dependenciesDiffFileModels != null) {
                handleChildExclude(model.dependenciesDiffFileModels)
            }
        }
    }

    def handleChildExclude(List<DependenciesDiffFileModel> childModelLists) {
        Iterator<DependenciesDiffFileModel> childIterable = childModelLists.iterator()
        while (childIterable.hasNext()) {
            DependenciesDiffFileModel childModel = childIterable.next()
            boolean excludeFile = isExcludeModuleFile(childModel)
            if (excludeFile) {
                childIterable.remove()
            }
        }
    }

    boolean isExcludeModuleFile(DependenciesDiffFileModel childModel) {
        if (excludeModuleFile != null) {
            for (String exclude : excludeModuleFile) {
                if (childModel.filePath.contains(exclude)) {
                    return true
                }
            }
        }
        return false
    }


    boolean isExcludeModule(DependenciesDiffModel model) {
        //过滤module
        if (excludeModule != null) {
            for (String exclude : excludeModule) {
                if (model.module.contains(exclude)) {
                    return true
                }
            }
        }
        //过滤module 状态
        if (excludeModuleStatus != null) {
            for (int exclude : excludeModuleStatus) {
                if (model.status == exclude) {
                    return true
                }
            }
        }
        return false
    }


}
