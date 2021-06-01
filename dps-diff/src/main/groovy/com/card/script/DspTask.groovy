package com.card.script

import com.android.build.gradle.AppExtension;
import com.android.build.gradle.AppPlugin
import com.android.build.gradle.LibraryExtension;
import com.android.build.gradle.LibraryPlugin
import com.android.build.gradle.api.ApplicationVariant
import com.android.build.gradle.api.LibraryVariant
import org.gradle.api.Action;
import org.gradle.api.DefaultTask
import org.gradle.api.artifacts.ArtifactCollection
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.component.ComponentIdentifier
import org.gradle.api.artifacts.result.ResolvedArtifactResult
import org.gradle.api.artifacts.result.ResolvedComponentResult
import org.gradle.api.artifacts.result.ResolvedDependencyResult
import org.gradle.api.attributes.AttributeContainer
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.TaskAction;

public class DspTask extends DefaultTask {


    public String outType = 'cvs'

    @Input
    public String[] exclude;

    @Input
    public String collectType = 'compile'   // compile,runtime,all

    @TaskAction
    public void action() {
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
        //CompileClasspath 为编译阶段的classpath 包含 compileOnly ,只包含此项目中的依赖，子项目的不包含
        //RuntimeClasspath 为运行阶段的classpath 不包含 compileOnly

        Set<ResolvedComponentResult> dependencyResults
        Set<ResolvedArtifactResult> resolvedArtifactResults

        Configuration compileClassPathConfiguration = getProject().getConfigurations().getByName(variantName + "CompileClasspath")

        Configuration runtimeClassPathConfiguration = getProject().getConfigurations().getByName(variantName + "RuntimeClasspath")

        Action<AttributeContainer> attributes = new Action<AttributeContainer>() {
            @Override
            void execute(AttributeContainer container) {
                //key : AndroidArtifacts.MODULE_PATH,AndroidArtifacts.ARTIFACT_TYPE
                //AndroidArtifacts中type类型 aar,android-classes等，会帮我们过滤
                // 配置有疑问 参考 VariantScopeImpl 中
                //container.attribute(ARTIFACT_TYPE, "android-classes");
            }
        }

        ArtifactCollection compileArtifactCollection = compileClassPathConfiguration.getIncoming()
                .artifactView(new Action<org.gradle.api.artifacts.ArtifactView.ViewConfiguration>() {
                    @Override
                    void execute(org.gradle.api.artifacts.ArtifactView.ViewConfiguration viewConfiguration) {
                        viewConfiguration.lenient(true)
                        viewConfiguration.attributes(attributes)
                    }
                }).getArtifacts();

        ArtifactCollection runtimeArtifactCollection = runtimeClassPathConfiguration.getIncoming()
                .artifactView(new Action<org.gradle.api.artifacts.ArtifactView.ViewConfiguration>() {
                    @Override
                    void execute(org.gradle.api.artifacts.ArtifactView.ViewConfiguration viewConfiguration) {
                        viewConfiguration.lenient(true)
                        viewConfiguration.attributes(attributes)
                    }
                }).getArtifacts();


        if (collectType == 'compile') {
            resolvedArtifactResults = compileArtifactCollection.getArtifacts()
            dependencyResults = compileClassPathConfiguration.getIncoming().getResolutionResult().getAllComponents()
        } else if (collectType == 'runtime') {
            resolvedArtifactResults = runtimeArtifactCollection.getArtifacts()
            dependencyResults = runtimeClassPathConfiguration.getIncoming().getResolutionResult().getAllComponents()
        } else {
            Set<ResolvedComponentResult> compileComponentResults = compileClassPathConfiguration.getIncoming().getResolutionResult().getAllComponents()
            Set<ResolvedArtifactResult> compileArtifactResults = compileArtifactCollection.getArtifacts()

            Set<ResolvedComponentResult> runtimeComponentResults = runtimeClassPathConfiguration.getIncoming().getResolutionResult().getAllComponents()
            Set<ResolvedArtifactResult> runtimeArtifactResults = runtimeArtifactCollection.getArtifacts()


            dependencyResults = new HashSet<>()
            dependencyResults.addAll(compileComponentResults)

            for (ResolvedComponentResult runtimeResult : runtimeComponentResults) {
                boolean isHas = false
                for (ResolvedComponentResult result : dependencyResults) {
                    if (result.getModuleVersion().getModule().getGroup() == runtimeResult.getModuleVersion().getModule().getGroup() &&
                            result.getModuleVersion().getModule().getName() == runtimeResult.getModuleVersion().getModule().getName()) {
                        isHas = true
                        break
                    }
                }
                if (!isHas) {
                    dependencyResults.add(runtimeResult)
                }
            }

            resolvedArtifactResults = new HashSet<>()
            resolvedArtifactResults.addAll(compileArtifactResults)

            for (ResolvedArtifactResult runtimeResult : runtimeArtifactResults) {
                boolean isHas = false
                for (ResolvedArtifactResult result : resolvedArtifactResults) {
                    if (runtimeResult.getId().getDisplayName() == result.getId().getDisplayName()) {
                        isHas = true
                        break
                    }
                }
                if (!isHas) {
                    resolvedArtifactResults.add(runtimeResult)
                }
            }


        }
        if (outType == 'cvs') {
            String path_code = getProject().rootProject.rootDir.getAbsolutePath() + "/dependencies.csv";
            DspCvsPrinter printer = new DspCvsPrinter(path_code)

            Iterator<ResolvedComponentResult> iterator = dependencyResults.iterator()
            if (exclude != null && exclude.length > 0)
                while (iterator.hasNext()) {
                    ResolvedComponentResult result = iterator.next()
                    String module = result.getModuleVersion().toString()
                    for (String excl : exclude) {
                        if (module.contains(excl)) {
                            iterator.remove()
                        }
                    }
                }
            printer.print(resolvedArtifactResults, dependencyResults)
        }


    }


}
