package com.card.script;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class MyClass {

//    task dpsTask {
//        doLast {
//            boolean isExecute = false
//            //仅library生效，application请使用getApplicationVariants
//            android.getLibraryVariants().all {
//                if (isExecute) {
//                    return
//                }
//                isExecute = true
//                String name = it.getName(); //name为构建变体组合,根据实际情况获取
//                //debugCompileClasspath
//                Configuration dependenciesConfiguration = getProject().getConfigurations().getByName(name + "CompileClasspath");
//
//                Action<AttributeContainer> attributes = new Action<AttributeContainer>() {
//                    @Override
//                    void execute(AttributeContainer container) {
//                        //key : AndroidArtifacts.MODULE_PATH,AndroidArtifacts.ARTIFACT_TYPE
//                        //AndroidArtifacts中type类型 aar,android-classes等，会帮我们过滤
//                        // 配置有疑问 参考 VariantScopeImpl 中
//                        //container.attribute(ARTIFACT_TYPE, "android-classes");
//                    }
//                };
//
//
//                ArtifactCollection artifactCollection = dependenciesConfiguration.getIncoming()
//                        .artifactView(new Action<org.gradle.api.artifacts.ArtifactView.ViewConfiguration>() {
//                            @Override
//                            void execute(org.gradle.api.artifacts.ArtifactView.ViewConfiguration viewConfiguration) {
//                                viewConfiguration.lenient(true)
//                                viewConfiguration.attributes(attributes);
//                            }
//                        }).getArtifacts();
//
//                Set<ResolvedArtifactResult> resolvedArtifactResults = artifactCollection.getArtifacts();
//
//                String path_code = rootProject.rootDir.getAbsolutePath() + "/dependencies.csv";
//                Writer fileWriter = new FileWriter(path_code)
//                fileWriter.write("dependencies");
//                fileWriter.write(",");
//                fileWriter.write("size")
//                fileWriter.write(",")
//                fileWriter.write("path")
//                //如果觉得依赖项过多，可以过滤
//                //获取本build.gradle对应项目的去除重复项的全部依赖
////             dependenciesConfiguration.getIncoming().getResolutionResult().getAllComponents()
////             获取本build.gradle对应项目的去除不重复项的全部依赖
////             dependenciesConfiguration.getIncoming().getResolutionResult().getAllDependencies()
////             获取本build.gradle中的写入的依赖，依赖的依赖请遍历获取
////             dependenciesConfiguration.getIncoming().getResolutionResult().getRoot().getDependencies()
//
//                resolvedArtifactResults.forEach(new Consumer<ResolvedArtifactResult>() {
//                    @Override
//                    void accept(ResolvedArtifactResult resolvedArtifactResult) {
//                        fileWriter.write("\n")
//                        fileWriter.write(resolvedArtifactResult.getId().getComponentIdentifier().getDisplayName())
//                        fileWriter.write(",")
//                        fileWriter.write(resolvedArtifactResult.getFile().length() + "")
//                        fileWriter.write(",")
//                        fileWriter.write(resolvedArtifactResult.getFile().getAbsolutePath())
//                    }
//                })
//
//                fileWriter.close()
//
//                //ResolvedDependencyResult
//                //
//                Set<ResolvedDependencyResult> dependencyResults = dependenciesConfiguration.getIncoming().getResolutionResult().getRoot().getDependencies()
//                Set<String> artifactSets = new HashSet<>();
//                Map<String,Model> modelMaps = getMap("/Users/juneleo/amap/amap_android_backup/amap_android/dependencies.csv")
//                List<DiffModel> diffModels = new ArrayList<>();
//                out(0, dependencyResults, artifactSets, resolvedArtifactResults,modelMaps,diffModels);
//                outCvs(diffModels)
//            }
//        }
//    }
//
//    def outCvs(List<DiffModel> diffModels ){
//        String path_code = rootProject.rootDir.getAbsolutePath() + "/diff.csv";
//        Writer fileWriter = new FileWriter(path_code)
//        fileWriter.write("model");
//        fileWriter.write(",");
//        fileWriter.write("size")
//        fileWriter.write(",")
//        fileWriter.write("oldSize")
//        fileWriter.write(",")
//        fileWriter.write("diff")
//
//        diffModels.forEach(new Consumer<DiffModel>() {
//            @Override
//            void accept(DiffModel diffModel) {
//                fileWriter.write("\n")
//                fileWriter.write(diffModel.module)
//                fileWriter.write(",")
//                fileWriter.write(diffModel.size)
//                fileWriter.write(",")
//                fileWriter.write(diffModel.oldSize)
//                fileWriter.write(",")
//                fileWriter.write(diffModel.diff)
//
//            }
//        })
//
//        fileWriter.close()
//    }
//
//    def out(int index, Set<ResolvedDependencyResult> dependencyResults, Set<String> artifactSets,
//            Set<ResolvedArtifactResult> resolvedArtifactResults,Map<String,Model> modelMaps,List<DiffModel> diffModels) {
//
//        if (dependencyResults == null || dependencyResults.isEmpty()) {
//            return
//        }
//        for (ResolvedDependencyResult dependencyResult : dependencyResults) {
//            ResolvedComponentResult componentResult = dependencyResult.getSelected()
//            boolean isLast = false;
//            if (dependencyResult == dependencyResults.last() && dependencyResults.size() > 1) {
//                isLast = true;
//            }
//            if (artifactSets.contains(componentResult.getModuleVersion().getModule().toString())) {
//                continue
//            }
//            if (componentResult.getModuleVersion().toString().contains("unspecified")) {
//                continue
//            }
//            long size = getSize(resolvedArtifactResults, componentResult.getModuleVersion());
//            String displayName = getTag(index, isLast) + componentResult.getModuleVersion().toString()
//
//            displayName = getSpace(100,displayName) + "new -|:" + size
//            if(modelMaps != null) {
//                long oldSize = getOldSize(modelMaps, componentResult.getModuleVersion());
//
//                long diff = size - oldSize
//
//                if(diff > 0) {
//                    DiffModel diffModel = new DiffModel();
//                    diffModel.module = componentResult.getModuleVersion().toString()
//                    diffModel.size = size
//                    diffModel.oldSize = oldSize
//                    diffModel.diff = diff
//                    diffModels.add(diffModel)
//                }
//                displayName = getSpace(120, displayName) + " old -|:" + oldSize
//                displayName = getSpace(140, displayName) + " diff -|:" + diff
//                println(displayName)
//
//            } else {
//                println(displayName)
//            }
//
//            artifactSets.add(componentResult.getModuleVersion().getModule().toString())
//            Set<ResolvedDependencyResult> sets = componentResult.getDependencies()
//            out(index + 1, sets, artifactSets, resolvedArtifactResults,modelMaps,diffModels)
//        }
//    }
//
//
//    long getOldSize(Map<String,Model> modelMaps,ModuleVersionIdentifier artifact){
//
//        for(Map.Entry<String,Model> entry : modelMaps.entrySet()){
//            if(entry.key.contains(artifact.getModule().toString())){
//                try {
//                    return Long.parseLong(entry.value.size);
//                }catch(Exception e){
//
//                }
//            }
//        }
//        return 0;
//    }
//
//    long getSize(Set<ResolvedArtifactResult> resolvedArtifactResults, ModuleVersionIdentifier artifact) {
//        for (ResolvedArtifactResult result : resolvedArtifactResults) {
//            ComponentIdentifier identifier = result.getId().getComponentIdentifier()
//            if (identifier.getDisplayName().equals(artifact.toString())) {
//                return result.getFile().length()
//            }
//        }
//        return 0;
//    }
//
//    String getSpace(int totalSpace,String displayName) {
//        StringBuilder stringBuilder = new StringBuilder();
//        int displayTotal = totalSpace;
//        stringBuilder.append(displayName)
//        int gap = displayTotal - displayName.length();
//        if (gap > 0) {
//            for (i in 0..<gap) {
//                stringBuilder.append(" ")
//            }
//        }
//        return stringBuilder.toString()
//    }
//
//
//    String getTag(int index, boolean isLast) {
//        StringBuilder stringBuilder = new StringBuilder();
//        for (i in 0..<index) {
//            stringBuilder.append("|    ")
//        }
//        if (isLast) {
//            stringBuilder.append("\\---")
//        } else {
//            stringBuilder.append("+---")
//        }
//
//    }
//
//    class Model {
//        String module;
//        String size;
//        String path;
//    }
//
//    class DiffModel {
//        String module
//        String size
//        String oldSize
//        String diff
//    }
//
//    Map<String, Model> getMap(String path) {
//        File file = new File(path);
//        if (!file.exists()) {
//            return null
//        }
//        Map<String,Model> map = new HashMap<>();
//        FileReader fileReader = new FileReader(file)
//        List<String> strings = fileReader.readLines();
//        for (String str : strings) {
//            String[] arrays = str.split(",")
//            if (arrays.length == 3) {
//                Model model = new Model()
//                model.module = arrays[0]
//                model.size = arrays[1]
//                model.path = arrays[2]
//                map.put(model.module,model)
//            }
//        }
//        return map
//    }
//
//
//    task dpsCul {
//        doLast {
//            Map<String,Model> modelMaps = getMap("/Users/juneleo/amap/amap_android_backup/amap_android/dependencies.csv")
//            modelMaps.entrySet().forEach(new Consumer<Map.Entry<String, Model>>() {
//                @Override
//                void accept(Map.Entry<String, Model> stringModelEntry) {
//                    println("-----: " + stringModelEntry.key)
//                }
//            })
//        }
//    }
}