package com.card.script.diff

import com.card.script.Utils
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.jetbrains.annotations.NotNull

public class DependenciesDiffTask extends DefaultTask {

    @Input
    public String firstPath;

    @Input
    public String secondPath;

    public static final String BUILD_DIR = 'diff';
    //html,console
    @Input
    public String outType = "html";


    @TaskAction
    def action() {
        File firstFilePath = project.file(firstPath)
        File secondFilePath = project.file(secondPath)

        if (!firstFilePath.exists() || !secondFilePath.exists()) {
            throw new RuntimeException("newVersionPath or oldVersionPath not exists")
        }

        File buildDir = project.file(project.buildDir.getAbsolutePath() + File.separator + BUILD_DIR)
        if (buildDir.exists()) {
            buildDir.deleteDir()
        }

        List<DependenciesModel> firstDependenciesModels = getDependenciesModels(firstFilePath.getAbsolutePath())
        List<DependenciesModel> secondDependenciesModels = getDependenciesModels(secondFilePath.getAbsolutePath())

        List<DependenciesDiffModel> diffModels = new ArrayList<>();
        for (DependenciesModel firstDependencyModel : firstDependenciesModels) {
            if (!project.file(firstDependencyModel.path).exists()) {
                continue
            }

            DependenciesModel secondDependencyModel = getSameModel(secondDependenciesModels, firstDependencyModel)
            if (secondDependencyModel == null) {
                DependenciesDiffModel newCreateModel = new DependenciesDiffModel()
                newCreateModel.firstDependenciesModel = firstDependencyModel
                newCreateModel.status = DependenciesDiffModel.STATUS_NEW
                newCreateModel.handle()
                diffModels.add(newCreateModel)
                continue
            }
            println("正在扫描 ：" + firstDependencyModel.group + ":" + firstDependencyModel.artifact)
            DependenciesDiffModel diffModel = handleDiffModel(firstDependencyModel, secondDependencyModel)
            diffModels.add(diffModel)
        }


        for (DependenciesModel secondDependencyModel : secondDependenciesModels) {
            if (!project.file(secondDependencyModel.path).exists()) {
                continue
            }

            DependenciesModel firstDependencyModel = getSameModel(firstDependenciesModels, secondDependencyModel)

            if (firstDependencyModel == null) {
                DependenciesDiffModel deleteModel = new DependenciesDiffModel();
                deleteModel.secondDependenciesModel = secondDependencyModel
                deleteModel.status = DependenciesDiffModel.STATUS_DELETE
                deleteModel.handle()
                diffModels.add(deleteModel)
            }
        }
        diffModels.sort()


        if (outType == 'console') {
            IDependenciesDiffPrinter printer = new ConsoleDependenciesDiffPrinter(project)
            printer.printer(diffModels)
        } else {
            IDependenciesDiffPrinter printer = new HtmlDependenciesDiffPrinter(project)
            printer.printer(diffModels)
        }
    }

    static class DependenciesDiffFileGroupModel {
        List<DependenciesDiffFileModel> diffFileModelList = new ArrayList<>()
        long diff = 0
        int type

        def add(DependenciesDiffFileModel dependenciesDiffFileModel) {
            diff += dependenciesDiffFileModel.diffFileSize
            diffFileModelList.add(dependenciesDiffFileModel)
        }

        List<DependenciesDiffFileModel> getList() {
            return diffFileModelList
        }

        String getType() {
            return DependenciesDiffFileModel.getType(type)
        }

    }

    static class DependenciesDiffModel implements Comparable<DependenciesDiffModel> {
        public static final STATUS_NORMAL = 0
        public static final STATUS_NEW = 1
        public static final STATUS_DELETE = 2
        public int status = STATUS_NORMAL
        public List<DependenciesDiffFileModel> dependenciesDiffFileModels = new ArrayList<>()
        public DependenciesModel firstDependenciesModel
        public DependenciesModel secondDependenciesModel
        public Map<Integer, DependenciesDiffFileGroupModel> diffFileModelMap = new HashMap<>();

        public long diff

        def handle() {
            if (firstDependenciesModel != null && secondDependenciesModel != null) {
                diff = firstDependenciesModel.size - secondDependenciesModel.size
            } else if (firstDependenciesModel != null) {
                diff = firstDependenciesModel.size
            } else if (secondDependenciesModel != null) {
                diff = -secondDependenciesModel.size
            }
            doHandleGroup()
        }

        def doHandleGroup() {
            for (DependenciesDiffFileModel dependenciesDiffFileModel : dependenciesDiffFileModels) {
                int fileType = dependenciesDiffFileModel.fileType
                DependenciesDiffFileGroupModel groupModel = diffFileModelMap.get(fileType)
                if (groupModel == null) {
                    groupModel = new DependenciesDiffFileGroupModel()
                    groupModel.type = fileType
                    diffFileModelMap.put(fileType, groupModel)
                }
                groupModel.add(dependenciesDiffFileModel)
            }
        }

        long getModelSize() {
            if (firstDependenciesModel != null) {
                return firstDependenciesModel.size
            }
            return 0
        }

        long getOldModelSize() {
            if (firstDependenciesModel != null) {
                return firstDependenciesModel.size
            }
            return 0
        }

        String getModule() {
            if (firstDependenciesModel != null) {
                return firstDependenciesModel.group + ":" + firstDependenciesModel.artifact
            } else if (secondDependenciesModel != null) {
                return secondDependenciesModel.group + ":" + secondDependenciesModel.artifact
            } else {
                return "unknow"
            }
        }

        String getModelInfo() {
            if (firstDependenciesModel != null) {
                return Utils.getSpace(30, "version : " + firstDependenciesModel.version) + ", size " + firstDependenciesModel.size
            }
            return "";
        }

        String getModelInfoByHtml() {
            if (firstDependenciesModel != null) {
                return "version : " + firstDependenciesModel.version + "<br />size : " + firstDependenciesModel.size
            }
            return "";
        }

        String getOldModelInfo() {
            if (secondDependenciesModel != null) {
                return Utils.getSpace(30, "version : " + secondDependenciesModel.version) + ", size " + secondDependenciesModel.size
            }
            return "";
        }

        String getOldModelInfoByHtml() {
            if (secondDependenciesModel != null) {
                return "version : " + secondDependenciesModel.version + "<br />size : " + secondDependenciesModel.size
            }
            return "";
        }


        @Override
        int compareTo(@NotNull DependenciesDiffModel o) {
            return Long.compare(o.diff, diff)
        }
    }

    static class DependenciesDiffFileModel implements Comparable<DependenciesDiffFileModel> {
        public static final int CHANGE_NEW = 1;//新文件
        public static final int CHANGE_HIGH = 2;//增大
        public static final int CHANGE_EQ = 3;//相等
        public static final int CHANGE_LOW = 4;//减小
        public static final int CHANGE_DEL = 5;//删除
        public int fileChange = 0;
        public long firstFileSize = 0;
        public long secondFileSize = 0;
        public long diffFileSize = 0;

        public static final int TYPE_CLASS = 1;
        public static final int TYPE_SO = 2;
        public static final int TYPE_RES = 3;
        public static final int TYPE_ASSETS = 4;
        public static final int TYPE_UNKNOW = 5;

        public int fileType = TYPE_UNKNOW;

        public String filePath
        public String fileShortPath


        @Override
        int compareTo(@NotNull DependenciesDiffFileModel o) {
            return o.diffFileSize - diffFileSize
        }

        void handle() {
            diffFileSize = firstFileSize - secondFileSize
        }

        static def handleFileType(File file, DependenciesDiffFileModel diffFileModel) {
            String fileName = file.getName()
            String filePath = file.getAbsolutePath()
            if (fileName.contains(".class")) {
                diffFileModel.fileType = TYPE_CLASS
            } else if (fileName.contains(".so")) {
                diffFileModel.fileType = TYPE_SO
            } else if (filePath.contains("/res/")) {
                diffFileModel.fileType = TYPE_RES
            } else if (filePath.contains("/assets/")) {
                diffFileModel.fileType = TYPE_ASSETS
            } else {
                diffFileModel.fileType = TYPE_UNKNOW
            }
        }

        static String getType(int type) {
            if (type == TYPE_CLASS) {
                return 'class'
            } else if (type == TYPE_SO) {
                return 'so'
            } else if (type == TYPE_RES) {
                return 'res'
            } else if (type == TYPE_ASSETS) {
                return 'assets'
            } else {
                return 'unknow'
            }
        }


        String getChange() {
            if (fileChange == CHANGE_NEW) {
                return "新文件"
            } else if (fileChange == CHANGE_HIGH) {
                return "增  大"
            } else if (fileChange == CHANGE_EQ) {
                return "相  等"
            } else if (fileChange == CHANGE_LOW) {
                return "减  小"
            } else if (fileChange == CHANGE_DEL) {
                return "删  除"
            }
        }
    }


    DependenciesDiffModel handleDiffModel(DependenciesModel firstDependencyModel, DependenciesModel secondDependencyModel) {
        DependenciesDiffModel diffModel = new DependenciesDiffModel();
        String moduleShotName = firstDependencyModel.group + ":" + firstDependencyModel.artifact

        File moduleRootDir = new File(project.buildDir.getAbsolutePath() + File.separator + BUILD_DIR + File.separator + moduleShotName)

        if (moduleRootDir.exists()) {
            moduleRootDir.deleteDir()
        }
        moduleRootDir.mkdirs()
        String firstDependencyModulePath = moduleRootDir.getAbsolutePath() + File.separator + firstDependencyModel.module
        String secondDependencyModulePath = moduleRootDir.getAbsolutePath() + File.separator + secondDependencyModel.module

        unzip(firstDependencyModel.path, firstDependencyModulePath)
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
            diffFileDependenciesModel.handleFileType(firstChildFile, diffFileDependenciesModel)
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
                DependenciesDiffFileModel.handleFileType(secondChildFile, diffFileModel)
                diffFileModel.handle()
                diffDependenciesModelList.add(diffFileModel)
            }
        }

        diffDependenciesModelList.sort()

        diffModel.firstDependenciesModel = firstDependencyModel
        diffModel.secondDependenciesModel = secondDependencyModel
        diffModel.dependenciesDiffFileModels = diffDependenciesModelList
        diffModel.handle()
        return diffModel
    }

    def unzip(String from_path, String into_path) {
        project.copy {
            from(project.zipTree(from_path))
            into(into_path)
        }

        File unzip_dir = project.file(into_path)
        if (unzip_dir.exists() && unzip_dir.isDirectory()) {
            File[] dir_files = unzip_dir.listFiles()
            for (File child_file : dir_files) {
                if (child_file.isFile() && child_file.getName().contains("classes.jar")) {
                    unzip(child_file.getAbsolutePath(), child_file.getAbsolutePath() + "_dir")
                    child_file.delete()
                }
            }
        }
    }


    class DependenciesModel {
        //module,group,artifact,version,path
        public String module
        public String group
        public String artifact
        public String version
        public String path
        public long size
    }

    DependenciesModel getSameModel(List<DependenciesModel> dependenciesModels, model) {
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


}
